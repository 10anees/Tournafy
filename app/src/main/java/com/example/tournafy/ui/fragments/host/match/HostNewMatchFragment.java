package com.example.tournafy.ui.fragments.host.match;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.tournafy.R;
import com.example.tournafy.domain.models.match.cricket.CricketMatch;
import com.example.tournafy.domain.models.match.football.FootballMatch;
import com.example.tournafy.domain.models.sport.SportTypeEnum;
import com.example.tournafy.ui.viewmodels.AuthViewModel;
import com.example.tournafy.ui.viewmodels.HostViewModel;
import com.google.android.material.button.MaterialButton;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HostNewMatchFragment extends Fragment {

    private HostViewModel hostViewModel;
    private AuthViewModel authViewModel; // Need this for User ID

    private ViewPager2 viewPager;
    private MaterialButton btnBack, btnNext;
    private TextView tvStepIndicator;
    private FrameLayout loadingOverlay;

    private SportTypeEnum selectedSport = SportTypeEnum.CRICKET;
    private String currentUserId;

    public HostNewMatchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_host_new_match, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        hostViewModel = new ViewModelProvider(requireActivity()).get(HostViewModel.class);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        initViews(view);
        setupViewPager();
        setupButtons();
        observeViewModel();
    }

    private void initViews(View view) {
        viewPager = view.findViewById(R.id.pagerHostMatch);
        btnBack = view.findViewById(R.id.btnBack);
        btnNext = view.findViewById(R.id.btnNext);
        tvStepIndicator = view.findViewById(R.id.tvStepIndicator);
        loadingOverlay = view.findViewById(R.id.loadingOverlay);
    }

    private void setupViewPager() {
        viewPager.setUserInputEnabled(false);
        viewPager.setAdapter(new MatchWizardAdapter(this));

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateUIForStep(position);
            }
        });
    }

    private void updateUIForStep(int position) {
        if (position == 0) {
            tvStepIndicator.setText("Step 1 of 2: Details");
            btnBack.setEnabled(false);
            btnNext.setText("Next");
        } else {
            tvStepIndicator.setText("Step 2 of 2: Teams");
            btnBack.setEnabled(true);
            btnNext.setText("Create Match");
        }
    }

    private void setupButtons() {
        btnBack.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() > 0) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
            }
        });

        btnNext.setOnClickListener(v -> {
            int current = viewPager.getCurrentItem();
            if (current == 0) {
                viewPager.setCurrentItem(1);
            } else {
                finalizeCreation();
            }
        });
    }

    private void finalizeCreation() {
        if (currentUserId == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // In a real app, we would get the 'name' from the ViewModel shared with child fragments.
        // For now, we use a placeholder name to satisfy the Builder constructor.
        String matchName = "New Match";

        if (selectedSport == SportTypeEnum.CRICKET) {
            // FIX: Pass required arguments (Name, HostUserId)
            hostViewModel.createCricketMatch(new CricketMatch.Builder(matchName, currentUserId));
        } else {
            // FIX: Pass required arguments (Name, HostUserId)
            hostViewModel.createFootballMatch(new FootballMatch.Builder(matchName, currentUserId));
        }
    }

    private void observeViewModel() {
        // Get User ID
        authViewModel.user.observe(getViewLifecycleOwner(), user -> {
            if (user != null) currentUserId = user.getUserId();
        });

        hostViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading ->
                loadingOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE));

        hostViewModel.creationSuccess.observe(getViewLifecycleOwner(), entity -> {
            if (entity != null) {
                Toast.makeText(getContext(), "Match Created!", Toast.LENGTH_SHORT).show();
                hostViewModel.clearSuccessEvent();
                Navigation.findNavController(requireView()).navigateUp();
            }
        });

        hostViewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                hostViewModel.clearErrorMessage();
            }
        });
    }

    public void setSelectedSport(SportTypeEnum sport) {
        this.selectedSport = sport;
    }

    private static class MatchWizardAdapter extends FragmentStateAdapter {
        public MatchWizardAdapter(Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return new AddMatchDetailsFragment();
            } else {
                return new AddMatchTeamsFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}