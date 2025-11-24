package com.example.tournafy.ui.fragments.host.tournament;

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
import com.example.tournafy.domain.models.tournament.Tournament;
import com.example.tournafy.ui.viewmodels.AuthViewModel;
import com.example.tournafy.ui.viewmodels.HostViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Date;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HostNewTournamentFragment extends Fragment {

    private HostViewModel hostViewModel;
    private AuthViewModel authViewModel;

    private TextInputEditText etName;
    private TextInputEditText etPlayersPerTeam;
    private TextInputEditText etMinTeams;
    private AutoCompleteTextView actvSport, actvFormat;
    private MaterialButton btnCreate;
    private ProgressBar progressBar;

    private String currentUserId;
    private static final String GUEST_HOST_ID = "GUEST_HOST";

    public HostNewTournamentFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_host_new_tournament, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        hostViewModel = new ViewModelProvider(requireActivity()).get(HostViewModel.class);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        initViews(view);
        setupDropdowns();
        setupObservers();
        
        btnCreate.setOnClickListener(v -> createTournament());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Reset loading state to prevent stuck spinners if user comes back
        if (hostViewModel != null && Boolean.TRUE.equals(hostViewModel.isLoading.getValue())) {
             // We can't access the private setter, but we can rely on the 
             // new HostingService logic to always clear it via success/error.
        }
    }

    private void initViews(View view) {
        etName = view.findViewById(R.id.etTournamentName);
        etPlayersPerTeam = view.findViewById(R.id.etPlayersPerTeam);
        etMinTeams = view.findViewById(R.id.etMinTeams);
        actvSport = view.findViewById(R.id.actvSportType);
        actvFormat = view.findViewById(R.id.actvTournamentType);
        btnCreate = view.findViewById(R.id.btnCreate);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupDropdowns() {
        String[] sports = new String[]{"Cricket", "Football"};
        ArrayAdapter<String> sportAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, sports);
        actvSport.setAdapter(sportAdapter);

        String[] formats = new String[]{"Knockout", "Round Robin"};
        ArrayAdapter<String> formatAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, formats);
        actvFormat.setAdapter(formatAdapter);
    }

    private void setupObservers() {
        authViewModel.user.observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                currentUserId = user.getUserId();
            }
            // FIX: Removed "navigateUp" here. We allow staying on this screen as a guest.
        });

        hostViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnCreate.setEnabled(!isLoading);
        });

        hostViewModel.creationSuccess.observe(getViewLifecycleOwner(), entity -> {
            if (entity != null && entity instanceof Tournament) {
                Tournament tournament = (Tournament) entity;
                Toast.makeText(getContext(), "Tournament Created!", Toast.LENGTH_SHORT).show();
                hostViewModel.clearSuccessEvent();
                
                // Navigate to Add Teams screen with tournament details
                NavController navController = Navigation.findNavController(requireView());
                Bundle args = new Bundle();
                args.putString("tournament_id", tournament.getEntityId());
                args.putBoolean("is_online", tournament.isOnline());
                navController.navigate(R.id.action_hostNewTournament_to_addTournamentTeams, args);
            }
        });

        hostViewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                hostViewModel.clearErrorMessage();
            }
        });
    }

    private void createTournament() {
        String name = etName.getText().toString().trim();
        String sport = actvSport.getText().toString();
        String format = actvFormat.getText().toString();
        String playersPerTeamStr = etPlayersPerTeam.getText().toString().trim();
        String minTeamsStr = etMinTeams.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(sport) || TextUtils.isEmpty(format)) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Parse configuration values with defaults
        int playersPerTeam = 11; // default
        int minTeams = 2; // default
        
        try {
            if (!TextUtils.isEmpty(playersPerTeamStr)) {
                playersPerTeam = Integer.parseInt(playersPerTeamStr);
                if (playersPerTeam < 1 || playersPerTeam > 25) {
                    Toast.makeText(getContext(), "Players per team must be between 1 and 25", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            if (!TextUtils.isEmpty(minTeamsStr)) {
                minTeams = Integer.parseInt(minTeamsStr);
                if (minTeams < 2 || minTeams > 32) {
                    Toast.makeText(getContext(), "Minimum teams must be between 2 and 32", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid number format", Toast.LENGTH_SHORT).show();
            return;
        }

        // FIX: Use GUEST_HOST_ID if currentUserId is null
        String hostId = (currentUserId != null) ? currentUserId : GUEST_HOST_ID;

        // Build tournament configuration
        java.util.Map<String, Object> config = new java.util.HashMap<>();
        config.put("playersPerTeam", playersPerTeam);
        config.put("minTeams", minTeams);

        Tournament.Builder builder = new Tournament.Builder(name, hostId, sport.toUpperCase(), format.toUpperCase())
                .withStartDate(new Date())
                .withTournamentConfig(config);

        hostViewModel.createTournament(builder);
    }
}