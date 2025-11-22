package com.example.tournafy.ui.fragments.match.cricket;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.example.tournafy.R;
import com.example.tournafy.domain.models.match.cricket.CricketMatch;
import com.example.tournafy.domain.enums.TossDecision;
import com.example.tournafy.ui.viewmodels.MatchViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CricketTossFragment extends Fragment {

    private MatchViewModel matchViewModel;
    
    private TextView tvTossDecisionLabel;
    private ChipGroup chipGroupTossWinner;
    private ChipGroup chipGroupTossDecision;
    private Chip chipTeamA;
    private Chip chipTeamB;
    private Chip chipBat;
    private Chip chipBowl;
    private MaterialButton btnContinueToLiveScore;
    
    private String matchId;
    private String teamAName;
    private String teamBName;
    private String selectedTossWinner;
    private String selectedTossDecision;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        matchViewModel = new ViewModelProvider(requireActivity()).get(MatchViewModel.class);
        
        // Get match_id from arguments
        if (getArguments() != null) {
            matchId = getArguments().getString("match_id");
            if (matchId != null) {
                matchViewModel.loadMatchById(matchId);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cricket_toss, container, false);
        
        initViews(view);
        setupTeamNames();
        setupListeners();
        
        return view;
    }

    private void initViews(View view) {
        tvTossDecisionLabel = view.findViewById(R.id.tvTossDecisionLabel);
        chipGroupTossWinner = view.findViewById(R.id.chipGroupTossWinner);
        chipGroupTossDecision = view.findViewById(R.id.chipGroupTossDecision);
        chipTeamA = view.findViewById(R.id.chipTeamA);
        chipTeamB = view.findViewById(R.id.chipTeamB);
        chipBat = view.findViewById(R.id.chipBat);
        chipBowl = view.findViewById(R.id.chipBowl);
        btnContinueToLiveScore = view.findViewById(R.id.btnContinueToLiveScore);
    }

    private void setupTeamNames() {
        // Observe the current match to get team names
        matchViewModel.getCurrentMatch().observe(getViewLifecycleOwner(), match -> {
            if (match instanceof CricketMatch) {
                CricketMatch cricketMatch = (CricketMatch) match;
                if (cricketMatch.getTeams() != null && cricketMatch.getTeams().size() >= 2) {
                    teamAName = cricketMatch.getTeams().get(0).getTeamName();
                    teamBName = cricketMatch.getTeams().get(1).getTeamName();
                    
                    chipTeamA.setText(teamAName);
                    chipTeamB.setText(teamBName);
                    
                    // Enable the chip group once teams are loaded
                    chipGroupTossWinner.setEnabled(true);
                    chipTeamA.setEnabled(true);
                    chipTeamB.setEnabled(true);
                } else {
                    // Disable chip group if teams not loaded yet
                    chipGroupTossWinner.setEnabled(false);
                    chipTeamA.setEnabled(false);
                    chipTeamB.setEnabled(false);
                    
                    // Show error if teams are null after a delay (data should have loaded)
                    if (cricketMatch.getTeams() == null || cricketMatch.getTeams().isEmpty()) {
                        Toast.makeText(requireContext(), 
                            "Error: Match teams not found. Please ensure teams are properly configured.", 
                            Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private void setupListeners() {
        // Toss winner selection listener
        chipGroupTossWinner.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);
                if (checkedId == R.id.chipTeamA) {
                    selectedTossWinner = teamAName;
                } else if (checkedId == R.id.chipTeamB) {
                    selectedTossWinner = teamBName;
                }
                
                // Validate that team names are loaded before proceeding
                if (selectedTossWinner == null || selectedTossWinner.isEmpty()) {
                    Toast.makeText(requireContext(), 
                        "Team names are still loading. Please wait.", 
                        Toast.LENGTH_SHORT).show();
                    chipGroupTossWinner.clearCheck();
                    return;
                }
                
                // Show decision section after winner is selected
                tvTossDecisionLabel.setVisibility(View.VISIBLE);
                chipGroupTossDecision.setVisibility(View.VISIBLE);
                
                // Reset decision if previously selected
                chipGroupTossDecision.clearCheck();
                selectedTossDecision = null;
                btnContinueToLiveScore.setEnabled(false);
            } else {
                // Hide decision section if no winner selected
                tvTossDecisionLabel.setVisibility(View.GONE);
                chipGroupTossDecision.setVisibility(View.GONE);
                selectedTossWinner = null;
                selectedTossDecision = null;
                btnContinueToLiveScore.setEnabled(false);
            }
        });

        // Toss decision listener
        chipGroupTossDecision.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);
                if (checkedId == R.id.chipBat) {
                    selectedTossDecision = TossDecision.BAT.name();
                } else if (checkedId == R.id.chipBowl) {
                    selectedTossDecision = TossDecision.BOWL.name();
                }
                
                // Enable continue button only when both selections are made
                btnContinueToLiveScore.setEnabled(true);
            } else {
                selectedTossDecision = null;
                btnContinueToLiveScore.setEnabled(false);
            }
        });

        // Continue button listener
        btnContinueToLiveScore.setOnClickListener(v -> saveTossResultAndNavigate());
    }

    private void saveTossResultAndNavigate() {
        if (selectedTossWinner == null || selectedTossDecision == null) {
            Toast.makeText(requireContext(), "Please complete the toss selection", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get current match value directly
        if (matchViewModel.getCurrentMatch().getValue() instanceof CricketMatch) {
            CricketMatch cricketMatch = (CricketMatch) matchViewModel.getCurrentMatch().getValue();
            cricketMatch.setTossWinner(selectedTossWinner);
            cricketMatch.setTossDecision(selectedTossDecision);
            
            // Save to Firestore
            matchViewModel.updateMatch(cricketMatch);
            
            // Navigate to live score with match_id
            Bundle args = new Bundle();
            args.putString("match_id", matchId);
            
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_cricketTossFragment_to_cricketLiveScoreFragment, args);
        } else {
            Toast.makeText(requireContext(), "Match not loaded", Toast.LENGTH_SHORT).show();
        }
    }
}
