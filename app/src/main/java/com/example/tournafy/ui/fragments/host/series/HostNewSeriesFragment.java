package com.example.tournafy.ui.fragments.host.series;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.tournafy.R;
import com.example.tournafy.domain.models.series.Series;
import com.example.tournafy.ui.viewmodels.AuthViewModel;
import com.example.tournafy.ui.viewmodels.HostViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HostNewSeriesFragment extends Fragment {

    private HostViewModel hostViewModel;
    private AuthViewModel authViewModel;

    private TextInputEditText etName, etMatchCount;
    private AutoCompleteTextView actvSport;
    private MaterialButton btnCreate;
    private ProgressBar progressBar;

    private String currentUserId;
    private static final String GUEST_HOST_ID = "GUEST_HOST";

    public HostNewSeriesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_host_new_series, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        hostViewModel = new ViewModelProvider(requireActivity()).get(HostViewModel.class);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        initViews(view);
        setupDropdowns();
        setupObservers();

        btnCreate.setOnClickListener(v -> createSeries());
    }

    private void initViews(View view) {
        etName = view.findViewById(R.id.etSeriesName);
        etMatchCount = view.findViewById(R.id.etMatchCount);
        actvSport = view.findViewById(R.id.actvSportType);
        btnCreate = view.findViewById(R.id.btnCreate);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupDropdowns() {
        String[] sports = new String[]{"Cricket", "Football"};
        ArrayAdapter<String> sportAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, sports);
        actvSport.setAdapter(sportAdapter);
    }

    private void setupObservers() {
        authViewModel.user.observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                currentUserId = user.getUserId();
            }
            // FIX: Removed "navigateUp" here to allow guest access
        });

        hostViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnCreate.setEnabled(!isLoading);
        });

        hostViewModel.creationSuccess.observe(getViewLifecycleOwner(), entity -> {
            if (entity != null && entity instanceof Series) {
                Toast.makeText(getContext(), "Series Created!", Toast.LENGTH_SHORT).show();
                hostViewModel.clearSuccessEvent();
                NavController navController = Navigation.findNavController(requireView());
                navController.navigateUp();
            }
        });

        hostViewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                hostViewModel.clearErrorMessage();
            }
        });
    }

    private void createSeries() {
        String name = etName.getText().toString().trim();
        String sport = actvSport.getText().toString();
        String matchCountStr = etMatchCount.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(sport) || TextUtils.isEmpty(matchCountStr)) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int totalMatches;
        try {
            totalMatches = Integer.parseInt(matchCountStr);
        } catch (NumberFormatException e) {
            etMatchCount.setError("Invalid number");
            return;
        }

        // FIX: Use GUEST_HOST_ID if currentUserId is null
        String hostId = (currentUserId != null) ? currentUserId : GUEST_HOST_ID;

        // Passing null for team IDs as they might be added later
        Series.Builder builder = new Series.Builder(name, hostId, sport.toUpperCase(), null, null)
                .withTotalMatches(totalMatches);

        hostViewModel.createSeries(builder);
    }
}