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
    private TextInputEditText etMatchName, etVenue, etOvers, etDuration, etPlayersPerSide, etCricketPlayersPerSide;

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
        etCricketPlayersPerSide = view.findViewById(R.id.etCricketPlayersPerSide);
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
        
        // Listen for football players per side changes
        etPlayersPerSide.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int players = Integer.parseInt(s.toString());
                    hostViewModel.playersPerSide.setValue(players);
                } catch (NumberFormatException e) {
                    hostViewModel.playersPerSide.setValue(11); // Default
                }
            }
        });
        
        // Listen for cricket players per side changes
        etCricketPlayersPerSide.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int players = Integer.parseInt(s.toString());
                    hostViewModel.playersPerSide.setValue(players);
                } catch (NumberFormatException e) {
                    hostViewModel.playersPerSide.setValue(11); // Default
                }
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
    
    /**
     * Gets the cricket configuration from the UI inputs
     */
    public com.example.tournafy.domain.models.match.cricket.CricketMatchConfig getCricketConfig() {
        com.example.tournafy.domain.models.match.cricket.CricketMatchConfig config = 
            new com.example.tournafy.domain.models.match.cricket.CricketMatchConfig();
        
        // Get number of overs
        String oversText = etOvers.getText() != null ? etOvers.getText().toString() : "20";
        try {
            int overs = Integer.parseInt(oversText);
            config.setNumberOfOvers(overs);
        } catch (NumberFormatException e) {
            config.setNumberOfOvers(20); // Default
        }
        
        // Get players per side
        String playersText = etCricketPlayersPerSide.getText() != null ? etCricketPlayersPerSide.getText().toString() : "11";
        try {
            int players = Integer.parseInt(playersText);
            config.setPlayersPerSide(players);
            android.util.Log.d("CricketConfig", "Players per side set to: " + players);
        } catch (NumberFormatException e) {
            config.setPlayersPerSide(11); // Default to 11 players
            android.util.Log.w("CricketConfig", "Invalid players count, using default: 11");
        }
        
        // Get wide/no ball setting
        com.google.android.material.materialswitch.MaterialSwitch switchWideBall = 
            getView().findViewById(R.id.switchWideBall);
        if (switchWideBall != null) {
            config.setWideOn(switchWideBall.isChecked());
        }
        
        // Get last man standing setting
        com.google.android.material.materialswitch.MaterialSwitch switchLastManStanding = 
            getView().findViewById(R.id.switchLastManStanding);
        if (switchLastManStanding != null) {
            config.setLastManStanding(switchLastManStanding.isChecked());
            android.util.Log.d("CricketConfig", "Last Man Standing set to: " + switchLastManStanding.isChecked());
        }
        
        return config;
    }
    
    /**
     * Gets the football configuration from the UI inputs
     */
    public com.example.tournafy.domain.models.match.football.FootballMatchConfig getFootballConfig() {
        com.example.tournafy.domain.models.match.football.FootballMatchConfig config = 
            new com.example.tournafy.domain.models.match.football.FootballMatchConfig();
        
        // Get match duration
        String durationText = etDuration.getText() != null ? etDuration.getText().toString() : "90";
        try {
            int duration = Integer.parseInt(durationText);
            config.setMatchDuration(duration);
            android.util.Log.d("FootballConfig", "Match duration set to: " + duration);
        } catch (NumberFormatException e) {
            config.setMatchDuration(90); // Default to 90 minutes
            android.util.Log.w("FootballConfig", "Invalid duration, using default: 90");
        }
        
        // Get players per side
        String playersText = etPlayersPerSide.getText() != null ? etPlayersPerSide.getText().toString() : "11";
        try {
            int players = Integer.parseInt(playersText);
            config.setPlayersPerSide(players);
            android.util.Log.d("FootballConfig", "Players per side set to: " + players);
        } catch (NumberFormatException e) {
            config.setPlayersPerSide(11); // Default to 11 players
            android.util.Log.w("FootballConfig", "Invalid players count, using default: 11");
        }
        
        // Get offside setting
        com.google.android.material.materialswitch.MaterialSwitch switchOffside = 
            getView().findViewById(R.id.switchOffside);
        if (switchOffside != null) {
            config.setOffsideOn(switchOffside.isChecked());
            android.util.Log.d("FootballConfig", "Offside set to: " + switchOffside.isChecked());
        }
        
        android.util.Log.d("FootballConfig", "Final config - Duration: " + config.getMatchDuration() + 
                          ", Players: " + config.getPlayersPerSide() + ", Offside: " + config.isOffsideOn());
        
        return config;
    }
}