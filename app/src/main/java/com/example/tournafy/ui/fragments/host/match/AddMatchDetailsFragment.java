package com.example.tournafy.ui.fragments.host.match;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

        // Use requireActivity() to share the ViewModel with the parent Fragment
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
        // FIX: Listen for text changes and update the shared ViewModel
        etMatchName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                hostViewModel.matchNameInput.setValue(s.toString());
            }
        });
        
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
    
    public boolean validate() {
        // Implement validation if needed
        return true;
    }
}