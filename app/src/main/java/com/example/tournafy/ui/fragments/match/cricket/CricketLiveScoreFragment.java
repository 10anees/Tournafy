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
import com.example.tournafy.utils.ShareHelper;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
    private MaterialButton btnChangeStriker;
    private MaterialButton btnChangeNonStriker;
    private MaterialButton btnChangeBowler;
    private MaterialButton btnShareMatch;
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
        btnChangeStriker = view.findViewById(R.id.btnChangeStriker);
        btnChangeNonStriker = view.findViewById(R.id.btnChangeNonStriker);
        btnChangeBowler = view.findViewById(R.id.btnChangeBowler);
        btnShareMatch = view.findViewById(R.id.btnShareMatch);
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
        // Share Match Button - Opens system share sheet
        btnShareMatch.setOnClickListener(v -> {
            // Check if user is logged in
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(requireContext(), "Please log in to share matches", Toast.LENGTH_SHORT).show();
                return;
            }
            
            android.util.Log.d("CricketLiveScore", "Share button clicked");
            android.util.Log.d("CricketLiveScore", "currentMatch: " + (currentMatch != null ? currentMatch.getName() : "null"));
            if (currentMatch != null) {
                android.util.Log.d("CricketLiveScore", "currentMatch visibilityLink: " + currentMatch.getVisibilityLink());
            }
            ShareHelper.shareMatch(requireContext(), currentMatch);
        });

        // Start Match Button
        btnStartMatch.setOnClickListener(v -> {
            android.util.Log.d("CricketLiveScore", "Start Match button clicked");
            
            // Get match to validate
            com.example.tournafy.domain.models.base.Match match = matchViewModel.offlineMatch.getValue();
            if (!(match instanceof CricketMatch)) {
                Toast.makeText(getContext(), "Invalid match type", Toast.LENGTH_SHORT).show();
                return;
            }
            
            CricketMatch cricketMatch = (CricketMatch) match;
            
            // Validate match can start
            if (cricketMatch.getTeams() == null || cricketMatch.getTeams().size() < 2) {
                Toast.makeText(getContext(), "Need at least 2 teams to start", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (!cricketMatch.getMatchStatus().equals(com.example.tournafy.domain.enums.MatchStatus.SCHEDULED.name())) {
                Toast.makeText(getContext(), "Match already started or completed", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // CRITICAL VALIDATION: Check if players are selected
            if (cricketMatch.getCurrentBowlerId() == null) {
                Toast.makeText(getContext(), "Please select a bowler before starting the match\n(Use 'Change Bowler' button)", 
                    Toast.LENGTH_LONG).show();
                return;
            }
            
            if (cricketMatch.getCurrentStrikerId() == null) {
                Toast.makeText(getContext(), "Please select a striker before starting the match\n(Use 'Change Striker' button)", 
                    Toast.LENGTH_LONG).show();
                return;
            }
            
            if (cricketMatch.getCurrentNonStrikerId() == null) {
                Toast.makeText(getContext(), "Please select a non-striker before starting the match\n(Use 'Change Non-Striker' button)", 
                    Toast.LENGTH_LONG).show();
                return;
            }
            
            // All validations passed - start the match
            android.util.Log.d("CricketLiveScore", "All players selected, starting match");
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
        btnChangeStriker.setOnClickListener(v -> showChangeStrikerDialog());
        btnChangeNonStriker.setOnClickListener(v -> showChangeNonStrikerDialog());
        btnChangeBowler.setOnClickListener(v -> showChangeBowlerDialog());
        
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

    private boolean matchCompletedDialogShown = false;
    
    private void observeViewModel() {
        // Observe match updates
        matchViewModel.offlineMatch.observe(getViewLifecycleOwner(), match -> {
            if (match instanceof CricketMatch) {
                currentMatch = (CricketMatch) match;
                String previousStatus = currentMatch != null ? currentMatch.getMatchStatus() : null;
                updateUI(currentMatch);
                
                // Check if match just completed - show option to view full scorecard
                // Only show dialog once when status changes to COMPLETED
                if ("COMPLETED".equals(currentMatch.getMatchStatus()) && !matchCompletedDialogShown) {
                    matchCompletedDialogShown = true;
                    showMatchCompletedDialog();
                }
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
        android.util.Log.d("CricketLiveScore", "showNextBatsmanDialog called, isForQueue: " + isForQueue);
        
        // Get fresh match data from ViewModel
        com.example.tournafy.domain.models.base.Match match = matchViewModel.offlineMatch.getValue();
        if (!(match instanceof CricketMatch)) {
            Toast.makeText(getContext(), "Match not loaded", Toast.LENGTH_SHORT).show();
            return;
        }
        
        CricketMatch cricketMatch = (CricketMatch) match;
        android.util.Log.d("CricketLiveScore", "Fragment isAdded: " + isAdded());
        android.util.Log.d("CricketLiveScore", "Match status: " + cricketMatch.getMatchStatus());
        
        // Check if fragment is added to activity
        if (!isAdded()) {
            android.util.Log.e("CricketLiveScore", "Fragment not added, cannot show batsman dialog");
            return;
        }
        android.util.Log.d("CricketLiveScore", "Showing next batsman dialog, isForQueue: " + isForQueue);
        
        com.example.tournafy.ui.dialogs.SelectNextBatsmanDialog dialog =
                com.example.tournafy.ui.dialogs.SelectNextBatsmanDialog.newInstance(
                        cricketMatch,
                        isForQueue,
                        player -> {
                            if (isForQueue) {
                                // Add to batting order queue
                                cricketMatch.addToBattingOrder(player.getPlayerId());
                                matchViewModel.updateMatch(cricketMatch);
                                Toast.makeText(getContext(), 
                                        player.getPlayerName() + " added to batting order", 
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                // Immediate replacement - set as striker
                                cricketMatch.setCurrentStrikerId(player.getPlayerId());
                                matchViewModel.updateMatch(cricketMatch);
                                Toast.makeText(getContext(), 
                                        player.getPlayerName() + " is now batting", 
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                );
        
        try {
            android.util.Log.d("CricketLiveScore", "About to call batsman dialog.showNow()");
            dialog.showNow(getParentFragmentManager(), "SelectNextBatsmanDialog");
            android.util.Log.d("CricketLiveScore", "Batsman dialog.showNow() completed");
        } catch (Exception e) {
            android.util.Log.e("CricketLiveScore", "Error showing batsman dialog: " + e.getMessage(), e);
        }
    }
    
    private void showNextBowlerDialog() {
        showNextBowlerDialog(false); // false = immediate replacement
    }
    
    private void showNextBowlerDialog(boolean isForQueue) {
        // Get fresh match data from ViewModel
        com.example.tournafy.domain.models.base.Match match = matchViewModel.offlineMatch.getValue();
        if (!(match instanceof CricketMatch)) {
            Toast.makeText(getContext(), "Match not loaded", Toast.LENGTH_SHORT).show();
            return;
        }
        
        CricketMatch cricketMatch = (CricketMatch) match;
        android.util.Log.d("CricketLiveScore", "Showing next bowler dialog, isForQueue: " + isForQueue);
        android.util.Log.d("CricketLiveScore", "Fragment isAdded: " + isAdded());
        android.util.Log.d("CricketLiveScore", "Match status: " + cricketMatch.getMatchStatus());
        
        // Check if fragment is added to activity
        if (!isAdded()) {
            android.util.Log.e("CricketLiveScore", "Fragment not added, cannot show dialog");
            return;
        }
        
        com.example.tournafy.ui.dialogs.SelectNextBowlerDialog dialog =
                com.example.tournafy.ui.dialogs.SelectNextBowlerDialog.newInstance(
                        cricketMatch,
                        isForQueue,
                        player -> {
                            if (isForQueue) {
                                // Add to bowling order queue
                                cricketMatch.addToBowlingOrder(player.getPlayerId());
                                matchViewModel.updateMatch(cricketMatch);
                                Toast.makeText(getContext(), 
                                        player.getPlayerName() + " added to bowling order", 
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                // Immediate replacement - set as current bowler
                                cricketMatch.setCurrentBowlerId(player.getPlayerId());
                                matchViewModel.updateMatch(cricketMatch);
                                Toast.makeText(getContext(), 
                                        player.getPlayerName() + " is now bowling", 
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                );
        
        try {
            android.util.Log.d("CricketLiveScore", "About to call dialog.showNow()");
            dialog.showNow(getParentFragmentManager(), "SelectNextBowlerDialog");
            android.util.Log.d("CricketLiveScore", "dialog.showNow() completed");
        } catch (Exception e) {
            android.util.Log.e("CricketLiveScore", "Error showing dialog: " + e.getMessage(), e);
        }
    }

    /**
     * Show dialog to change the current striker
     * Works even when match is SCHEDULED (before startMatch is called)
     */
    private void showChangeStrikerDialog() {
        android.util.Log.d("CricketLiveScore", "showChangeStrikerDialog called");
        
        // Get fresh match data from ViewModel
        com.example.tournafy.domain.models.base.Match match = matchViewModel.offlineMatch.getValue();
        if (!(match instanceof CricketMatch)) {
            Toast.makeText(getContext(), "Match not loaded", Toast.LENGTH_SHORT).show();
            return;
        }
        
        CricketMatch cricketMatch = (CricketMatch) match;
        
        // Check if fragment is added to activity
        if (!isAdded()) {
            android.util.Log.e("CricketLiveScore", "Fragment not added, cannot show dialog");
            return;
        }
        
        // Use the SelectStrikerDialog for changing striker
        com.example.tournafy.ui.dialogs.SelectStrikerDialog dialog =
                com.example.tournafy.ui.dialogs.SelectStrikerDialog.newInstance(
                        cricketMatch,
                        player -> {
                            android.util.Log.d("CricketLiveScore", "Striker changed to: " + player.getPlayerName());
                            cricketMatch.setCurrentStrikerId(player.getPlayerId());
                            matchViewModel.updateMatch(cricketMatch);
                            Toast.makeText(getContext(), 
                                    player.getPlayerName() + " is now the striker", 
                                    Toast.LENGTH_SHORT).show();
                        }
                );
        
        try {
            dialog.showNow(getParentFragmentManager(), "SelectStrikerDialog");
        } catch (Exception e) {
            android.util.Log.e("CricketLiveScore", "Error showing striker dialog: " + e.getMessage(), e);
        }
    }

    /**
     * Show dialog to change the current non-striker
     * Works even when match is SCHEDULED (before startMatch is called)
     */
    private void showChangeNonStrikerDialog() {
        android.util.Log.d("CricketLiveScore", "showChangeNonStrikerDialog called");
        
        // Get fresh match data from ViewModel
        com.example.tournafy.domain.models.base.Match match = matchViewModel.offlineMatch.getValue();
        if (!(match instanceof CricketMatch)) {
            Toast.makeText(getContext(), "Match not loaded", Toast.LENGTH_SHORT).show();
            return;
        }
        
        CricketMatch cricketMatch = (CricketMatch) match;
        
        // Check if fragment is added to activity
        if (!isAdded()) {
            android.util.Log.e("CricketLiveScore", "Fragment not added, cannot show dialog");
            return;
        }
        
        // Use the SelectNonStrikerDialog, passing current striker ID to exclude
        String strikerId = cricketMatch.getCurrentStrikerId();
        com.example.tournafy.ui.dialogs.SelectNonStrikerDialog dialog =
                com.example.tournafy.ui.dialogs.SelectNonStrikerDialog.newInstance(
                        cricketMatch,
                        strikerId,
                        player -> {
                            android.util.Log.d("CricketLiveScore", "Non-striker changed to: " + player.getPlayerName());
                            cricketMatch.setCurrentNonStrikerId(player.getPlayerId());
                            matchViewModel.updateMatch(cricketMatch);
                            Toast.makeText(getContext(), 
                                    player.getPlayerName() + " is now the non-striker", 
                                    Toast.LENGTH_SHORT).show();
                        }
                );
        
        try {
            dialog.showNow(getParentFragmentManager(), "SelectNonStrikerDialog");
        } catch (Exception e) {
            android.util.Log.e("CricketLiveScore", "Error showing non-striker dialog: " + e.getMessage(), e);
        }
    }

    /**
     * Show dialog to change the current bowler
     * Works even when match is SCHEDULED (before startMatch is called)
     */
    private void showChangeBowlerDialog() {
        android.util.Log.d("CricketLiveScore", "showChangeBowlerDialog called");
        
        // Get fresh match data from ViewModel
        com.example.tournafy.domain.models.base.Match match = matchViewModel.offlineMatch.getValue();
        if (!(match instanceof CricketMatch)) {
            Toast.makeText(getContext(), "Match not loaded", Toast.LENGTH_SHORT).show();
            return;
        }
        
        CricketMatch cricketMatch = (CricketMatch) match;
        
        // Check if fragment is added to activity
        if (!isAdded()) {
            android.util.Log.e("CricketLiveScore", "Fragment not added, cannot show dialog");
            return;
        }
        
        // Use the SelectOpeningBowlerDialog for changing bowler
        com.example.tournafy.ui.dialogs.SelectOpeningBowlerDialog dialog =
                com.example.tournafy.ui.dialogs.SelectOpeningBowlerDialog.newInstance(
                        cricketMatch,
                        player -> {
                            android.util.Log.d("CricketLiveScore", "Bowler changed to: " + player.getPlayerName());
                            cricketMatch.setCurrentBowlerId(player.getPlayerId());
                            matchViewModel.updateMatch(cricketMatch);
                            Toast.makeText(getContext(), 
                                    player.getPlayerName() + " is now bowling", 
                                    Toast.LENGTH_SHORT).show();
                        }
                );
        
        try {
            dialog.showNow(getParentFragmentManager(), "SelectOpeningBowlerDialog");
        } catch (Exception e) {
            android.util.Log.e("CricketLiveScore", "Error showing bowler dialog: " + e.getMessage(), e);
        }
    }

    /**
     * Check if wicket was just taken and automatically bring next batsman from queue,
     * or show dialog if queue is empty
     */
    private void handleWicketFall() {
        android.util.Log.d("CricketLiveScore", "handleWicketFall called");
        
        // Get fresh match data from ViewModel
        com.example.tournafy.domain.models.base.Match match = matchViewModel.offlineMatch.getValue();
        if (!(match instanceof CricketMatch)) {
            android.util.Log.w("CricketLiveScore", "Match is not CricketMatch or is null in handleWicketFall");
            matchViewModel.clearWicketFallEvent();
            return;
        }
        
        CricketMatch cricketMatch = (CricketMatch) match;
        boolean hasBatsmanInQueue = cricketMatch.hasBatsmanInQueue();
        android.util.Log.d("CricketLiveScore", "Has batsman in queue: " + hasBatsmanInQueue);
        
        if (hasBatsmanInQueue) {
            // Get next batsman from queue
            String nextBatsmanId = cricketMatch.getNextBatsmanFromQueue();
            android.util.Log.d("CricketLiveScore", "Next batsman from queue: " + nextBatsmanId);
            if (nextBatsmanId != null) {
                cricketMatch.setCurrentStrikerId(nextBatsmanId);
                matchViewModel.updateMatch(cricketMatch);
                
                String playerName = getPlayerName(nextBatsmanId);
                Toast.makeText(getContext(), 
                        playerName + " is now batting (from queue)", 
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Queue is empty, show dialog
            android.util.Log.d("CricketLiveScore", "Queue empty, showing next batsman dialog");
            showNextBatsmanDialog(false);
        }
        
        // Clear the event flag
        matchViewModel.clearWicketFallEvent();
    }
    
    /**
     * Handle match start - show initial batsman and bowler selection dialogs in sequence
     * This is called when match transitions to LIVE status (triggered by match start event)
     * 
     * At this point:
     * - Match status is LIVE
     * - Innings structure has been created
     * - First over has been created
     * - But no players are initialized yet
     * 
     * Dialog sequence:
     * 1. Select Opening Bowler
     * 2. Select Opening Striker (first batsman)
     * 3. Select Opening Non-Striker (second batsman)
     */
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
    
    /**
     * Shows a dialog when match is completed, offering to view full scorecard
     */
    private void showMatchCompletedDialog() {
        if (currentMatch == null) return;
        
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Match Completed")
                .setMessage("The match has ended. Would you like to view the full scorecard and match details?")
                .setPositiveButton("View Scorecard", (dialog, which) -> {
                    navigateToMatchDetails();
                })
                .setNegativeButton("Stay Here", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }
    
    /**
     * Navigate to MatchActivity to view completed match details
     */
    private void navigateToMatchDetails() {
        if (currentMatch == null) return;
        
        android.content.Intent intent = new android.content.Intent(requireContext(), 
                com.example.tournafy.ui.activities.MatchActivity.class);
        intent.putExtra(com.example.tournafy.ui.activities.MatchActivity.EXTRA_MATCH_ID, 
                currentMatch.getEntityId());
        startActivity(intent);
    }
}