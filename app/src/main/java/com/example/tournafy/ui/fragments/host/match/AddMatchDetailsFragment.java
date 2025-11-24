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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.tournafy.R;
import com.example.tournafy.domain.models.sport.SportTypeEnum;
import com.example.tournafy.ui.viewmodels.HostViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

public class AddMatchDetailsFragment extends Fragment {

    private HostViewModel hostViewModel;
    
    private ChipGroup chipGroupSport;
    private LinearLayout grpCricketConfig, grpFootballConfig;
    private TextInputEditText etMatchName, etVenue, etOvers, etDuration, etPlayersPerSide, etCricketPlayersPerSide;
    private MaterialButton btnNext;
    
    private String matchId;
    private String tournamentId;
    private boolean isTournamentMatch;

    public AddMatchDetailsFragment() {
        // Required empty public constructor
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            matchId = getArguments().getString("match_id");
            tournamentId = getArguments().getString("tournament_id");
            isTournamentMatch = getArguments().getBoolean("is_tournament_match", false);
        }
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
        btnNext = view.findViewById(R.id.btnNext);
        
        // If this is a tournament match, show Next button and load match details
        if (isTournamentMatch) {
            if (btnNext != null) {
                btnNext.setVisibility(View.VISIBLE);
            }
            if (matchId != null) {
                loadTournamentMatchDetails();
            }
        }
    }

    private void setupListeners() {
        // Set up Next button
        if (btnNext != null) {
            btnNext.setOnClickListener(v -> {
                if (isTournamentMatch) {
                    navigateToAddMatchTeams();
                }
            });
        }
        
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
    
    /**
     * Load tournament match details and pre-fill the form
     */
    private void loadTournamentMatchDetails() {
        if (matchId == null) {
            android.util.Log.w("AddMatchDetails", "Cannot load tournament match: matchId is null");
            return;
        }
        
        android.util.Log.d("AddMatchDetails", "Loading tournament match with ID: " + matchId);
        
        // Load the match from repository via ViewModel
        hostViewModel.loadMatchById(matchId);
        hostViewModel.currentMatch.observe(getViewLifecycleOwner(), match -> {
            if (match != null && etMatchName != null) {
                android.util.Log.d("AddMatchDetails", "Match loaded: " + match.getName() + ", Sport: " + match.getSportId());
                
                // Pre-fill match name
                etMatchName.setText(match.getName());
                etMatchName.setEnabled(false); // Don't allow changing tournament match name
                
                // Pre-select sport type based on match
                boolean isCricket = "CRICKET".equalsIgnoreCase(match.getSportId());
                if (isCricket) {
                    chipGroupSport.check(R.id.chipCricket);
                    showCricketConfig();
                } else {
                    chipGroupSport.check(R.id.chipFootball);
                    showFootballConfig();
                }
                
                // Disable sport selection for tournament matches
                chipGroupSport.setEnabled(false);
                for (int i = 0; i < chipGroupSport.getChildCount(); i++) {
                    chipGroupSport.getChildAt(i).setEnabled(false);
                }
                
                android.util.Log.d("AddMatchDetails", "Match details pre-filled successfully");
            } else {
                android.util.Log.w("AddMatchDetails", "Match is null or etMatchName is null");
            }
        });
    }
    
    /**
     * Navigate to AddMatchTeamsFragment for team selection
     */
    private void navigateToAddMatchTeams() {
        android.util.Log.d("AddMatchDetails", "Preparing to navigate to AddMatchTeams");
        
        // Validate inputs
        if (!validate()) {
            android.util.Log.w("AddMatchDetails", "Validation failed, cannot navigate");
            return;
        }
        
        // Load the match and save configuration
        hostViewModel.loadMatchById(matchId);
        hostViewModel.currentMatch.observe(getViewLifecycleOwner(), new androidx.lifecycle.Observer<com.example.tournafy.domain.models.base.Match>() {
            @Override
            public void onChanged(com.example.tournafy.domain.models.base.Match match) {
                if (match == null) {
                    android.util.Log.w("AddMatchDetails", "Match is null, cannot save config");
                    return;
                }
                
                // Remove observer to prevent multiple calls
                hostViewModel.currentMatch.removeObserver(this);
                
                android.util.Log.d("AddMatchDetails", "Match loaded: " + match.getName());
                
                // Set configuration based on sport type
                boolean isCricket = chipGroupSport.getCheckedChipId() == R.id.chipCricket;
                if (isCricket && match instanceof com.example.tournafy.domain.models.match.cricket.CricketMatch) {
                    com.example.tournafy.domain.models.match.cricket.CricketMatchConfig config = getCricketConfig();
                    ((com.example.tournafy.domain.models.match.cricket.CricketMatch) match).setMatchConfig(config);
                    hostViewModel.cricketMatchConfig.setValue(config);
                    android.util.Log.d("AddMatchDetails", "Cricket config set: " + config.getNumberOfOvers() + " overs");
                } else if (!isCricket && match instanceof com.example.tournafy.domain.models.match.football.FootballMatch) {
                    com.example.tournafy.domain.models.match.football.FootballMatchConfig config = getFootballConfig();
                    ((com.example.tournafy.domain.models.match.football.FootballMatch) match).setMatchConfig(config);
                    hostViewModel.footballMatchConfig.setValue(config);
                    android.util.Log.d("AddMatchDetails", "Football config set: " + config.getMatchDuration() + " mins");
                }
                
                // Save match to Firestore
                hostViewModel.updateMatch(match, new com.example.tournafy.service.interfaces.IHostingService.HostingCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        android.util.Log.d("AddMatchDetails", "Match config saved to Firestore successfully");
                        
                        // Navigate with match details
                        Bundle args = new Bundle();
                        args.putString("match_id", matchId);
                        args.putString("tournament_id", tournamentId);
                        args.putBoolean("is_tournament_match", true);
                        
                        android.util.Log.d("AddMatchDetails", "Navigating with args: match_id=" + matchId + 
                                          ", tournament_id=" + tournamentId + ", is_tournament_match=true");
                        
                        NavController navController = Navigation.findNavController(requireView());
                        navController.navigate(R.id.action_addMatchDetails_to_addMatchTeams, args);
                    }
                    
                    @Override
                    public void onError(Exception e) {
                        android.util.Log.e("AddMatchDetails", "Failed to save match config", e);
                        android.widget.Toast.makeText(getContext(), 
                            "Failed to save configuration: " + e.getMessage(), 
                            android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}