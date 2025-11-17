package com.example.tournafy.ui.fragments.host.match;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.tournafy.R;
import com.example.tournafy.domain.models.sport.SportTypeEnum;
import com.example.tournafy.ui.viewmodels.HostViewModel;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

public class AddMatchDetailsFragment extends Fragment {

    private HostViewModel hostViewModel;
    
    // UI Views
    private ChipGroup chipGroupSport;
    private LinearLayout grpCricketConfig, grpFootballConfig;
    private TextInputEditText etMatchName, etVenue, etOvers, etDuration, etPlayersPerSide;

    public AddMatchDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_match_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        hostViewModel = new ViewModelProvider(requireActivity()).get(HostViewModel.class);
        
        initViews(view);
        setupListeners();
    }

    private void initViews(View view) {
        chipGroupSport = view.findViewById(R.id.chipGroupSport);
        grpCricketConfig = view.findViewById(R.id.grpCricketConfig);
        grpFootballConfig = view.findViewById(R.id.grpFootballConfig);
        
        etMatchName = view.findViewById(R.id.etMatchName);
        etVenue = view.findViewById(R.id.etVenue);
        etOvers = view.findViewById(R.id.etOvers);
        etDuration = view.findViewById(R.id.etDuration);
        etPlayersPerSide = view.findViewById(R.id.etPlayersPerSide);
    }

    private void setupListeners() {
        chipGroupSport.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            
            int id = checkedIds.get(0);
            if (id == R.id.chipCricket) {
                showCricketConfig();
            } else if (id == R.id.chipFootball) {
                showFootballConfig();
            }
        });
    }

    private void showCricketConfig() {
        grpCricketConfig.setVisibility(View.VISIBLE);
        grpFootballConfig.setVisibility(View.GONE);
        
        // Notify parent fragment to update its internal state if necessary
        if (getParentFragment() instanceof HostNewMatchFragment) {
            ((HostNewMatchFragment) getParentFragment()).setSelectedSport(SportTypeEnum.CRICKET);
        }
    }

    private void showFootballConfig() {
        grpCricketConfig.setVisibility(View.GONE);
        grpFootballConfig.setVisibility(View.VISIBLE);
        
        if (getParentFragment() instanceof HostNewMatchFragment) {
            ((HostNewMatchFragment) getParentFragment()).setSelectedSport(SportTypeEnum.FOOTBALL);
        }
    }
    
    // This method can be called by the Parent Fragment before moving to next step
    public boolean validate() {
        // Implement validation logic (e.g., ensure venue isn't empty)
        return true;
    }
}