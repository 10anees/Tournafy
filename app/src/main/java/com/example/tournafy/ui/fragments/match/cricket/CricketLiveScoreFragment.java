package com.example.tournafy.ui.fragments.match.cricket;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.tournafy.R;
import com.example.tournafy.domain.models.match.cricket.Ball;
import com.example.tournafy.domain.models.match.cricket.BatsmanStats;
import com.example.tournafy.domain.models.match.cricket.BowlerStats;
import com.example.tournafy.domain.models.match.cricket.CricketMatch;
import com.example.tournafy.domain.models.match.cricket.Innings;
import com.example.tournafy.domain.models.match.cricket.Over;
import com.example.tournafy.domain.enums.MatchStatus;
import com.example.tournafy.domain.models.team.MatchTeam;
import com.example.tournafy.domain.models.team.Player;
import com.example.tournafy.ui.components.ScoreboardView;
import com.example.tournafy.ui.viewmodels.MatchViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.example.tournafy.ui.adapters.PlayingXIAdapter;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CricketLiveScoreFragment extends Fragment {

    private MatchViewModel matchViewModel;
    private String matchId;

    // UI Components from new Layout
    private MaterialButton btnStartMatch;
    private MaterialButton btnSelectNextBatsman;
    private MaterialButton btnSelectNextBowler;
    private ScoreboardView scoreboardView;
    private TextView tvStriker, tvNonStriker, tvBowler;
    private LinearLayout llRecentBalls;

    // Keypad Buttons
    private Button btnZero, btnOne, btnTwo, btnThree, btnFour, btnFive, btnSix;
    private Button btnWide, btnNoBall, btnBye, btnLegBye;
    private Button btnWicket, btnUndo, btnRetire, btnSwap;
    
    // Playing XI Components
    private MaterialCardView cardBattingTeam, cardBowlingTeam;
    private LinearLayout headerBattingTeam, headerBowlingTeam;
    private TextView tvBattingTeamName, tvBowlingTeamName;
    private TextView tvBattingTeamExpand, tvBowlingTeamExpand;
    private RecyclerView recyclerBattingTeam, recyclerBowlingTeam;
    private PlayingXIAdapter battingTeamAdapter, bowlingTeamAdapter;
    
    private boolean isBattingTeamExpanded = false;
    private boolean isBowlingTeamExpanded = false;
    
    private CricketMatch currentMatch;
    private Map<String, BatsmanStats> batsmanStatsMap = new HashMap<>();
    private Map<String, BowlerStats> bowlerStatsMap = new HashMap<>();

    public CricketLiveScoreFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            matchId = getArguments().getString("match_id");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cricket_live_score, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        matchViewModel = new ViewModelProvider(requireActivity()).get(MatchViewModel.class);

        initViews(view);
        setupListeners();

        if (matchId != null) {
            android.util.Log.d("CricketLiveScoreFragment", "Loading match with ID: " + matchId);
            matchViewModel.loadOfflineMatch(matchId);
            observeViewModel();
        }
    }

    private void initViews(View view) {
        // Main Views
        btnStartMatch = view.findViewById(R.id.btnStartMatch);
        btnSelectNextBatsman = view.findViewById(R.id.btnSelectNextBatsman);
        btnSelectNextBowler = view.findViewById(R.id.btnSelectNextBowler);
        scoreboardView = view.findViewById(R.id.scoreboardView);
        tvStriker = view.findViewById(R.id.tvStriker);
        tvNonStriker = view.findViewById(R.id.tvNonStriker);
        tvBowler = view.findViewById(R.id.tvBowler);
        llRecentBalls = view.findViewById(R.id.llRecentBalls);

        // Keypad - Runs
        btnZero = view.findViewById(R.id.btnZero);
        btnOne = view.findViewById(R.id.btnOne);
        btnTwo = view.findViewById(R.id.btnTwo);
        btnThree = view.findViewById(R.id.btnThree);
        btnFour = view.findViewById(R.id.btnFour);
        btnFive = view.findViewById(R.id.btnFive);
        btnSix = view.findViewById(R.id.btnSix);

        // Keypad - Extras & Actions
        btnWide = view.findViewById(R.id.btnWide);
        btnNoBall = view.findViewById(R.id.btnNoBall);
        btnBye = view.findViewById(R.id.btnBye);
        btnLegBye = view.findViewById(R.id.btnLegBye);

        btnWicket = view.findViewById(R.id.btnWicket);
        btnUndo = view.findViewById(R.id.btnUndo);
        btnRetire = view.findViewById(R.id.btnRetire);
        btnSwap = view.findViewById(R.id.btnSwap);
        
        // Playing XI Components
        cardBattingTeam = view.findViewById(R.id.cardBattingTeam);
        cardBowlingTeam = view.findViewById(R.id.cardBowlingTeam);
        headerBattingTeam = view.findViewById(R.id.headerBattingTeam);
        headerBowlingTeam = view.findViewById(R.id.headerBowlingTeam);
        tvBattingTeamName = view.findViewById(R.id.tvBattingTeamName);
        tvBowlingTeamName = view.findViewById(R.id.tvBowlingTeamName);
        tvBattingTeamExpand = view.findViewById(R.id.tvBattingTeamExpand);
        tvBowlingTeamExpand = view.findViewById(R.id.tvBowlingTeamExpand);
        recyclerBattingTeam = view.findViewById(R.id.recyclerBattingTeam);
        recyclerBowlingTeam = view.findViewById(R.id.recyclerBowlingTeam);
        
        // Setup RecyclerViews
        recyclerBattingTeam.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerBowlingTeam.setLayoutManager(new LinearLayoutManager(getContext()));
        
        battingTeamAdapter = new PlayingXIAdapter(true);
        bowlingTeamAdapter = new PlayingXIAdapter(false);
        
        recyclerBattingTeam.setAdapter(battingTeamAdapter);
        recyclerBowlingTeam.setAdapter(bowlingTeamAdapter);
    }

    private void setupListeners() {
        // Start Match Button
        btnStartMatch.setOnClickListener(v -> {
            // Let the ViewModel handle all validation and show specific error messages
            matchViewModel.startMatch();
        });

        // Runs
        btnZero.setOnClickListener(v -> matchViewModel.addCricketBall(0));
        btnOne.setOnClickListener(v -> matchViewModel.addCricketBall(1));
        btnTwo.setOnClickListener(v -> matchViewModel.addCricketBall(2));
        btnThree.setOnClickListener(v -> matchViewModel.addCricketBall(3));
        btnFour.setOnClickListener(v -> matchViewModel.addCricketBall(4));
        btnFive.setOnClickListener(v -> matchViewModel.addCricketBall(5));
        btnSix.setOnClickListener(v -> matchViewModel.addCricketBall(6));

        // Extras
        btnWide.setOnClickListener(v -> matchViewModel.addCricketExtra("WIDE"));
        btnNoBall.setOnClickListener(v -> matchViewModel.addCricketExtra("NO_BALL"));
        btnBye.setOnClickListener(v -> matchViewModel.addCricketExtra("BYE"));
        btnLegBye.setOnClickListener(v -> matchViewModel.addCricketExtra("LEG_BYE"));

        // Wicket
        btnWicket.setOnClickListener(v -> {
            // Ideally opens a dialog to select wicket type
            matchViewModel.addCricketWicket("BOWLED");
            Toast.makeText(getContext(), "Wicket!", Toast.LENGTH_SHORT).show();
        });

        // Actions
        btnUndo.setOnClickListener(v -> {
            if (matchViewModel.canUndo()) {
                matchViewModel.undoLastEvent();
                Toast.makeText(getContext(), "Undo", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Nothing to undo", Toast.LENGTH_SHORT).show();
            }
        });

        btnRetire.setOnClickListener(v -> Toast.makeText(getContext(), "Retire Player", Toast.LENGTH_SHORT).show());
        btnSwap.setOnClickListener(v -> Toast.makeText(getContext(), "Swap Strike", Toast.LENGTH_SHORT).show());
        
        // Player Selection Buttons
        btnSelectNextBatsman.setOnClickListener(v -> showNextBatsmanDialog());
        btnSelectNextBowler.setOnClickListener(v -> showNextBowlerDialog());
        
        // Expand/Collapse Batting Team
        headerBattingTeam.setOnClickListener(v -> {
            isBattingTeamExpanded = !isBattingTeamExpanded;
            recyclerBattingTeam.setVisibility(isBattingTeamExpanded ? View.VISIBLE : View.GONE);
            tvBattingTeamExpand.setText(isBattingTeamExpanded ? "▲" : "▼");
        });
        
        // Expand/Collapse Bowling Team
        headerBowlingTeam.setOnClickListener(v -> {
            isBowlingTeamExpanded = !isBowlingTeamExpanded;
            recyclerBowlingTeam.setVisibility(isBowlingTeamExpanded ? View.VISIBLE : View.GONE);
            tvBowlingTeamExpand.setText(isBowlingTeamExpanded ? "▲" : "▼");
        });
    }

    private void observeViewModel() {
        // Observe match updates
        matchViewModel.offlineMatch.observe(getViewLifecycleOwner(), match -> {
            if (match instanceof CricketMatch) {
                currentMatch = (CricketMatch) match;
                updateUI(currentMatch);
            }
        });
        
        // Observe errors
        matchViewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                matchViewModel.clearErrorMessage();
            }
        });
        
        // Observe wicket fall events
        matchViewModel.wicketFallEvent.observe(getViewLifecycleOwner(), wicketFall -> {
            if (wicketFall != null && wicketFall) {
                handleWicketFall();
            }
        });
        
        // Observe over completion events
        matchViewModel.overCompletionEvent.observe(getViewLifecycleOwner(), overCompleted -> {
            if (overCompleted != null && overCompleted) {
                handleOverCompletion();
            }
        });
    }

    private void updateUI(CricketMatch match) {
        // Update Start Match button visibility based on match status
        MatchStatus status;
        try {
            status = MatchStatus.valueOf(match.getMatchStatus());
        } catch (IllegalArgumentException | NullPointerException e) {
            status = MatchStatus.SCHEDULED;
        }
        
        // Disable all input buttons if match is completed
        boolean isMatchLive = status == MatchStatus.LIVE;
        setInputButtonsEnabled(isMatchLive);
        
        Innings currentInnings = match.getCurrentInnings();

        if (currentInnings != null) {
            // 1. Update Scoreboard View with proper overs format (e.g., "12.3")
            String oversText = matchViewModel.getCurrentOversText();
            float currentRunRate = matchViewModel.getCurrentRunRate();
            
            // Get completed innings score for Team B display
            String teamBScore = null;
            if (match.getCurrentInningsNumber() == 2 && match.getInnings() != null && match.getInnings().size() > 0) {
                // Second innings is active, show first innings (Team A's) completed score
                Innings firstInnings = match.getInnings().get(0);
                teamBScore = String.format(java.util.Locale.getDefault(), "%d/%d", 
                    firstInnings.getTotalRuns(), firstInnings.getWicketsFallen());
            }
            
            scoreboardView.updateCricketScore(
                    matchViewModel.getTeamAName(),
                    matchViewModel.getTeamBName(),
                    currentInnings.getTotalRuns(),
                    currentInnings.getWicketsFallen(),
                    oversText,
                    currentRunRate,
                    teamBScore
            );
            
            // Update match status on scoreboard
            scoreboardView.setMatchStatus(status.name());

            // 2. Update Players - Now shows actual player names from team roster
            tvStriker.setText(matchViewModel.getStrikerId() + " *");
            tvNonStriker.setText(matchViewModel.getNonStrikerId());
            tvBowler.setText(matchViewModel.getCurrentBowlerId());
        }

        // 3. Update Recent Balls (This Over)
        updateRecentBalls(match);
        
        // 4. Update button states
        btnUndo.setEnabled(isMatchLive && matchViewModel.canUndo());
        
        // 5. Update Playing XI display
        updatePlayingXI(match);
    }
    
    /**
     * Enable or disable all input buttons (runs, extras, wicket).
     */
    private void setInputButtonsEnabled(boolean enabled) {
        // Run buttons
        btnZero.setEnabled(enabled);
        btnOne.setEnabled(enabled);
        btnTwo.setEnabled(enabled);
        btnThree.setEnabled(enabled);
        btnFour.setEnabled(enabled);
        btnFive.setEnabled(enabled);
        btnSix.setEnabled(enabled);
        
        // Extras buttons
        btnWide.setEnabled(enabled);
        btnNoBall.setEnabled(enabled);
        btnBye.setEnabled(enabled);
        btnLegBye.setEnabled(enabled);
        
        // Action buttons
        btnWicket.setEnabled(enabled);
        btnRetire.setEnabled(enabled);
        btnSwap.setEnabled(enabled);
    }

    private void updateRecentBalls(CricketMatch match) {
        llRecentBalls.removeAllViews();
        Over currentOver = match.getCurrentOver();

        if (currentOver != null && currentOver.getBalls() != null) {
            for (Ball ball : currentOver.getBalls()) {
                addBallView(ball);
            }
        }
    }

    private void addBallView(Ball ball) {
        TextView ballView = new TextView(getContext());

        // Determine Text & Style
        String text;
        int backgroundRes = R.drawable.shape_circle; // Default
        int textColor = getResources().getColor(android.R.color.black, null);

        if (ball.isWicket()) {
            text = "W";
            // You can create a specific style for wickets if needed, or tint this
            // ballView.setBackgroundTintList(...);
        } else if (ball.getExtrasType() != null && !ball.getExtrasType().equals("NONE")) {
            text = ball.getExtrasType().substring(0, 1).toLowerCase(); // w, n, b, l
        } else {
            text = String.valueOf(ball.getRunsScored());
        }

        // Highlight Boundaries
        if (ball.getRunsScored() == 4 || ball.getRunsScored() == 6) {
            // Optional: Make boundaries bold or colored
            // backgroundRes = R.drawable.shape_circle_boundary;
        }

        ballView.setText(text);
        ballView.setBackgroundResource(backgroundRes);
        ballView.setTextColor(textColor);
        ballView.setGravity(android.view.Gravity.CENTER);

        // Layout Params for the circle
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(80, 80); // 32dp approx
        params.setMargins(8, 0, 8, 0);
        ballView.setLayoutParams(params);

        llRecentBalls.addView(ballView);
    }
    
    private void updatePlayingXI(CricketMatch match) {
        if (match == null || match.getTeams() == null || match.getTeams().size() < 2) {
            return;
        }

        // Get stats from match
        Map<String, BatsmanStats> matchBatsmanStats = match.getBatsmanStatsMap();
        Map<String, BowlerStats> matchBowlerStats = match.getBowlerStatsMap();
        
        android.util.Log.d("CricketLiveScore", "Batsman stats from match: " + 
            (matchBatsmanStats != null ? matchBatsmanStats.size() : "null") + " players");
        android.util.Log.d("CricketLiveScore", "Bowler stats from match: " + 
            (matchBowlerStats != null ? matchBowlerStats.size() : "null") + " players");

        // Determine batting and bowling teams
        String battingTeamId = null;
        String bowlingTeamId = null;
        
        if (match.getInnings() != null && !match.getInnings().isEmpty()) {
            Innings currentInnings = match.getCurrentInnings();
            if (currentInnings != null) {
                battingTeamId = currentInnings.getBattingTeamId();
                bowlingTeamId = currentInnings.getBowlingTeamId();
            }
        }

        MatchTeam battingTeam = null;
        MatchTeam bowlingTeam = null;
        
        for (MatchTeam team : match.getTeams()) {
            if (team.getTeamId().equals(battingTeamId)) {
                battingTeam = team;
            } else if (team.getTeamId().equals(bowlingTeamId)) {
                bowlingTeam = team;
            }
        }

        // Update batting team
        if (battingTeam != null) {
            tvBattingTeamName.setText(battingTeam.getTeamName() + " (Batting)");
            List<Player> battingPlayers = getStartingXIPlayers(battingTeam);
            battingTeamAdapter.setPlayers(battingPlayers);
            battingTeamAdapter.setBatsmanStats(matchBatsmanStats);
            battingTeamAdapter.setCurrentPlayers(
                    match.getCurrentStrikerId(),
                    match.getCurrentNonStrikerId(),
                    match.getCurrentBowlerId()
            );
        }

        // Update bowling team
        if (bowlingTeam != null) {
            tvBowlingTeamName.setText(bowlingTeam.getTeamName() + " (Bowling)");
            List<Player> bowlingPlayers = getStartingXIPlayers(bowlingTeam);
            bowlingTeamAdapter.setPlayers(bowlingPlayers);
            bowlingTeamAdapter.setBowlerStats(matchBowlerStats);
            bowlingTeamAdapter.setCurrentPlayers(
                    match.getCurrentStrikerId(),
                    match.getCurrentNonStrikerId(),
                    match.getCurrentBowlerId()
            );
        }
    }
    
    private List<Player> getStartingXIPlayers(MatchTeam team) {
        List<Player> startingXI = new ArrayList<>();
        if (team != null && team.getPlayers() != null) {
            for (Player player : team.getPlayers()) {
                if (player.isStartingXI()) {
                    startingXI.add(player);
                }
            }
            // If no starting XI is set, return all players
            if (startingXI.isEmpty()) {
                startingXI.addAll(team.getPlayers());
            }
        }
        return startingXI;
    }
    
    private void showNextBatsmanDialog() {
        showNextBatsmanDialog(false); // false = immediate replacement
    }
    
    private void showNextBatsmanDialog(boolean isForQueue) {
        if (currentMatch == null) {
            Toast.makeText(getContext(), "Match not loaded", Toast.LENGTH_SHORT).show();
            return;
        }
        
        com.example.tournafy.ui.dialogs.SelectNextBatsmanDialog dialog =
                com.example.tournafy.ui.dialogs.SelectNextBatsmanDialog.newInstance(
                        currentMatch,
                        isForQueue,
                        player -> {
                            if (isForQueue) {
                                // Add to batting order queue
                                currentMatch.addToBattingOrder(player.getPlayerId());
                                matchViewModel.updateMatch(currentMatch);
                                Toast.makeText(getContext(), 
                                        player.getPlayerName() + " added to batting order", 
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                // Immediate replacement - set as striker
                                currentMatch.setCurrentStrikerId(player.getPlayerId());
                                matchViewModel.updateMatch(currentMatch);
                                Toast.makeText(getContext(), 
                                        player.getPlayerName() + " is now batting", 
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                );
        dialog.show(getParentFragmentManager(), "SelectNextBatsmanDialog");
    }
    
    private void showNextBowlerDialog() {
        showNextBowlerDialog(false); // false = immediate replacement
    }
    
    private void showNextBowlerDialog(boolean isForQueue) {
        if (currentMatch == null) {
            Toast.makeText(getContext(), "Match not loaded", Toast.LENGTH_SHORT).show();
            return;
        }
        
        com.example.tournafy.ui.dialogs.SelectNextBowlerDialog dialog =
                com.example.tournafy.ui.dialogs.SelectNextBowlerDialog.newInstance(
                        currentMatch,
                        isForQueue,
                        player -> {
                            if (isForQueue) {
                                // Add to bowling order queue
                                currentMatch.addToBowlingOrder(player.getPlayerId());
                                matchViewModel.updateMatch(currentMatch);
                                Toast.makeText(getContext(), 
                                        player.getPlayerName() + " added to bowling order", 
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                // Immediate replacement - set as current bowler
                                currentMatch.setCurrentBowlerId(player.getPlayerId());
                                matchViewModel.updateMatch(currentMatch);
                                Toast.makeText(getContext(), 
                                        player.getPlayerName() + " is now bowling", 
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                );
        dialog.show(getParentFragmentManager(), "SelectNextBowlerDialog");
    }
    
    /**
     * Check if wicket was just taken and automatically bring next batsman from queue,
     * or show dialog if queue is empty
     */
    private void handleWicketFall() {
        if (currentMatch == null) {
            matchViewModel.clearWicketFallEvent();
            return;
        }
        
        if (currentMatch.hasBatsmanInQueue()) {
            // Get next batsman from queue
            String nextBatsmanId = currentMatch.getNextBatsmanFromQueue();
            if (nextBatsmanId != null) {
                currentMatch.setCurrentStrikerId(nextBatsmanId);
                matchViewModel.updateMatch(currentMatch);
                
                String playerName = getPlayerName(nextBatsmanId);
                Toast.makeText(getContext(), 
                        playerName + " is now batting (from queue)", 
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Queue is empty, show dialog
            showNextBatsmanDialog(false);
        }
        
        // Clear the event flag
        matchViewModel.clearWicketFallEvent();
    }
    
    /**
     * Check if over completed and automatically bring next bowler from queue,
     * or show dialog if queue is empty
     */
    private void handleOverCompletion() {
        if (currentMatch == null) {
            matchViewModel.clearOverCompletionEvent();
            return;
        }
        
        if (currentMatch.hasBowlerInQueue()) {
            // Get next bowler from queue
            String nextBowlerId = currentMatch.getNextBowlerFromQueue();
            if (nextBowlerId != null) {
                currentMatch.setCurrentBowlerId(nextBowlerId);
                matchViewModel.updateMatch(currentMatch);
                
                String playerName = getPlayerName(nextBowlerId);
                Toast.makeText(getContext(), 
                        playerName + " is now bowling (from queue)", 
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Queue is empty, show dialog
            showNextBowlerDialog(false);
        }
        
        // Clear the event flag
        matchViewModel.clearOverCompletionEvent();
    }
    
    /**
     * Helper method to get player name by ID
     */
    private String getPlayerName(String playerId) {
        if (currentMatch == null || currentMatch.getTeams() == null) {
            return "Unknown Player";
        }
        
        for (com.example.tournafy.domain.models.team.MatchTeam team : currentMatch.getTeams()) {
            if (team.getPlayers() != null) {
                for (com.example.tournafy.domain.models.team.Player player : team.getPlayers()) {
                    if (player.getPlayerId().equals(playerId)) {
                        return player.getPlayerName();
                    }
                }
            }
        }
        return "Unknown Player";
    }
}