package com.example.tournafy.ui.fragments.match.cricket;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tournafy.R;
import com.example.tournafy.domain.models.match.cricket.BatsmanStats;
import com.example.tournafy.domain.models.match.cricket.BowlerStats;
import com.example.tournafy.domain.models.match.cricket.CricketMatch;
import com.example.tournafy.domain.models.match.cricket.Innings;
import com.example.tournafy.ui.adapters.BattingScorecardAdapter;
import com.example.tournafy.ui.adapters.BowlingScorecardAdapter;
import com.example.tournafy.ui.viewmodels.MatchViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CricketScorecardFragment extends Fragment {

    private MatchViewModel matchViewModel;
    
    // UI Components
    private ChipGroup chipGroupInnings;
    private Chip chipInnings1;
    private Chip chipInnings2;
    
    private TextView tvBattingTeam;
    private TextView tvBattingScore;
    private RecyclerView rvBatting;
    private TextView tvExtras;
    private TextView tvTotal;
    
    private TextView tvBowlingTeam;
    private RecyclerView rvBowling;
    
    private TextView tvFallOfWickets;
    
    private BattingScorecardAdapter battingAdapter;
    private BowlingScorecardAdapter bowlingAdapter;
    
    private CricketMatch cricketMatch;
    private int currentInningsIndex = 0;

    public CricketScorecardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cricket_scorecard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get ViewModel from activity
        matchViewModel = new ViewModelProvider(requireActivity()).get(MatchViewModel.class);
        
        initViews(view);
        setupRecyclerViews();
        setupListeners();
        observeMatch();
    }

    private void initViews(View view) {
        chipGroupInnings = view.findViewById(R.id.chipGroupInnings);
        chipInnings1 = view.findViewById(R.id.chipInnings1);
        chipInnings2 = view.findViewById(R.id.chipInnings2);
        
        tvBattingTeam = view.findViewById(R.id.tvBattingTeam);
        tvBattingScore = view.findViewById(R.id.tvBattingScore);
        rvBatting = view.findViewById(R.id.rvBatting);
        tvExtras = view.findViewById(R.id.tvExtras);
        tvTotal = view.findViewById(R.id.tvTotal);
        
        tvBowlingTeam = view.findViewById(R.id.tvBowlingTeam);
        rvBowling = view.findViewById(R.id.rvBowling);
        
        tvFallOfWickets = view.findViewById(R.id.tvFallOfWickets);
    }

    private void setupRecyclerViews() {
        // Batting RecyclerView
        battingAdapter = new BattingScorecardAdapter();
        rvBatting.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvBatting.setAdapter(battingAdapter);
        rvBatting.setNestedScrollingEnabled(false);
        
        // Bowling RecyclerView
        bowlingAdapter = new BowlingScorecardAdapter();
        rvBowling.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvBowling.setAdapter(bowlingAdapter);
        rvBowling.setNestedScrollingEnabled(false);
    }

    private void setupListeners() {
        chipGroupInnings.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);
                if (checkedId == R.id.chipInnings1) {
                    currentInningsIndex = 0;
                } else if (checkedId == R.id.chipInnings2) {
                    currentInningsIndex = 1;
                }
                updateScorecardForInnings();
            }
        });
    }

    private void observeMatch() {
        matchViewModel.getCurrentMatch().observe(getViewLifecycleOwner(), match -> {
            if (match instanceof CricketMatch) {
                this.cricketMatch = (CricketMatch) match;
                updateInningsChips();
                updateScorecardForInnings();
            }
        });
    }

    private void updateInningsChips() {
        if (cricketMatch == null) return;
        
        List<Innings> inningsList = cricketMatch.getInnings();
        
        // Enable/disable innings chips based on availability
        chipInnings1.setEnabled(inningsList != null && inningsList.size() > 0);
        chipInnings2.setEnabled(inningsList != null && inningsList.size() > 1);
        
        // Set innings labels with team names
        if (inningsList != null && !inningsList.isEmpty()) {
            Innings firstInnings = inningsList.get(0);
            String teamAName = getTeamName(firstInnings.getBattingTeamId());
            chipInnings1.setText(teamAName + " Innings");
            
            if (inningsList.size() > 1) {
                Innings secondInnings = inningsList.get(1);
                String teamBName = getTeamName(secondInnings.getBattingTeamId());
                chipInnings2.setText(teamBName + " Innings");
            }
        }
    }

    private void updateScorecardForInnings() {
        if (cricketMatch == null) return;
        
        List<Innings> inningsList = cricketMatch.getInnings();
        if (inningsList == null || currentInningsIndex >= inningsList.size()) {
            return;
        }
        
        Innings innings = inningsList.get(currentInningsIndex);
        
        // Update batting section
        String battingTeamName = getTeamName(innings.getBattingTeamId());
        tvBattingTeam.setText(battingTeamName + " Batting");
        tvBattingScore.setText(innings.getTotalRuns() + "/" + innings.getWicketsFallen());
        
        // Update bowling section
        String bowlingTeamName = getTeamName(innings.getBowlingTeamId());
        tvBowlingTeam.setText(bowlingTeamName + " Bowling");
        
        // Get batsman stats for this innings
        List<BatsmanStats> battingStats = getBatsmanStatsForInnings(innings);
        battingAdapter.setBatsmanStats(battingStats);
        
        // Get bowler stats for this innings
        List<BowlerStats> bowlingStats = getBowlerStatsForInnings(innings);
        bowlingAdapter.setBowlerStats(bowlingStats);
        
        // Update extras
        updateExtras(innings);
        
        // Update total
        double overs = innings.getTotalOvers();
        int completeOvers = (int) overs;
        int balls = (int) ((overs - completeOvers) * 10);
        String oversText = String.format(Locale.getDefault(), "%d.%d Ov", completeOvers, balls);
        tvTotal.setText(String.format(Locale.getDefault(), "%d/%d (%s)", 
            innings.getTotalRuns(), innings.getWicketsFallen(), oversText));
        
        // Update fall of wickets
        updateFallOfWickets(innings);
    }

    private List<BatsmanStats> getBatsmanStatsForInnings(Innings innings) {
        List<BatsmanStats> statsList = new ArrayList<>();
        
        if (cricketMatch == null) return statsList;
        
        Map<String, BatsmanStats> allStats = cricketMatch.getBatsmanStatsMap();
        if (allStats == null) return statsList;
        
        // Filter stats for players from batting team
        String battingTeamId = innings.getBattingTeamId();
        for (BatsmanStats stats : allStats.values()) {
            if (isPlayerInTeam(stats.getPlayerId(), battingTeamId)) {
                // Only include if they faced at least 1 ball or got out
                if (stats.getBallsFaced() > 0 || stats.isOut()) {
                    statsList.add(stats);
                }
            }
        }
        
        return statsList;
    }

    private List<BowlerStats> getBowlerStatsForInnings(Innings innings) {
        List<BowlerStats> statsList = new ArrayList<>();
        
        if (cricketMatch == null) return statsList;
        
        Map<String, BowlerStats> allStats = cricketMatch.getBowlerStatsMap();
        if (allStats == null) return statsList;
        
        // Filter stats for players from bowling team
        String bowlingTeamId = innings.getBowlingTeamId();
        for (BowlerStats stats : allStats.values()) {
            if (isPlayerInTeam(stats.getPlayerId(), bowlingTeamId)) {
                // Only include if they bowled at least 1 ball
                if (stats.getBallsBowled() > 0) {
                    statsList.add(stats);
                }
            }
        }
        
        return statsList;
    }

    private boolean isPlayerInTeam(String playerId, String teamId) {
        if (cricketMatch == null || cricketMatch.getTeams() == null) return false;
        
        return cricketMatch.getTeams().stream()
            .filter(team -> team.getTeamId().equals(teamId))
            .flatMap(team -> team.getPlayers().stream())
            .anyMatch(player -> player.getPlayerId().equals(playerId));
    }

    private String getTeamName(String teamId) {
        if (cricketMatch == null || cricketMatch.getTeams() == null) return "Team";
        
        return cricketMatch.getTeams().stream()
            .filter(team -> team.getTeamId().equals(teamId))
            .map(team -> team.getTeamName())
            .findFirst()
            .orElse("Team");
    }

    private void updateExtras(Innings innings) {
        int byes = innings.getByes();
        int legByes = innings.getLegByes();
        int wides = innings.getWides();
        int noBalls = innings.getNoBalls();
        int totalExtras = byes + legByes + wides + noBalls;
        
        String extrasText = String.format(Locale.getDefault(), 
            "%d (b %d, lb %d, w %d, nb %d)", 
            totalExtras, byes, legByes, wides, noBalls);
        tvExtras.setText(extrasText);
    }

    private void updateFallOfWickets(Innings innings) {
        // TODO: Implement fall of wickets tracking
        // For now, show placeholder
        tvFallOfWickets.setText("Fall of wickets data coming soon");
    }
}
