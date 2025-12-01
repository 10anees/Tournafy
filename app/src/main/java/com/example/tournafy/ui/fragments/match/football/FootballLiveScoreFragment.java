package com.example.tournafy.ui.fragments.match.football;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.tournafy.R;
import com.example.tournafy.domain.enums.MatchStatus;
import com.example.tournafy.domain.models.match.football.FootballMatch;
import com.example.tournafy.ui.components.ScoreboardView;
import com.example.tournafy.ui.dialogs.EventInputDialog;
import com.example.tournafy.ui.viewmodels.MatchViewModel;
import com.example.tournafy.utils.ShareHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.widget.TextView;
import java.util.List;

import java.util.ArrayList;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * FootballLiveScoreFragment - Comprehensive Football Match Scoring Interface
 * 
 * COMPLETE IMPLEMENTATION following cricket pattern with:
 * - Event-driven architecture (Goals, Cards, Substitutions)
 * - Real-time timer with period management
 * - Offline-first persistence (single Match.update() pattern)
 * - Match status handling (SCHEDULED -> LIVE -> COMPLETED)
 * - Input validation and error handling
 * - Loading states and UI feedback
 * - Button disabling after match completion
 * - Event timeline display
 * - Undo functionality via Command Pattern
 * 
 * Author: Tournafy Team
 * Date: 2024
 */
@AndroidEntryPoint
public class FootballLiveScoreFragment extends Fragment implements EventInputDialog.EventInputListener {

    private MatchViewModel matchViewModel;
    private String matchId;
    private FootballMatch currentMatch; // Store current match for sharing
    
    // UI Views
    private ScoreboardView scoreboardView;
    private MaterialButton btnShareMatch;
    private MaterialButton btnStartTimer, btnPauseTimer, btnHalfTime, btnEndMatch, btnToggleSquad;
    private MaterialCardView cardGoal, cardCard, cardSub, cardUndo;
    private androidx.recyclerview.widget.RecyclerView rvEventTimeline, rvTeamAPlayers, rvTeamBPlayers;
    private android.widget.LinearLayout tvEmptyTimeline, llSquadContent;
    private android.widget.TextView tvTeamAName, tvTeamBName;
    private com.example.tournafy.ui.adapters.FootballEventAdapter eventAdapter;
    private com.example.tournafy.ui.adapters.PlayerStatusAdapter teamAPlayerAdapter, teamBPlayerAdapter;

    // Timer State
    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private long startTime = 0L;
    private long timeInMilliseconds = 0L;
    private long timeSwapBuff = 0L;
    private boolean isRunning = false;
    private boolean matchStarted = false; // Track if startMatch() has been called
    private String currentLoadedMatchId = null; // Track which match is currently loaded to detect match changes
    private boolean timerStateRestored = false; // Track if we've restored timer state for current match
    private boolean completedDialogShown = false; // Track if completion dialog was shown for current match
    private String previousMatchStatus = null; // Track previous status to detect transitions
    
    // Match State
    private String currentTimerText = "00:00";
    private int currentMinute = 0;
    private String currentPeriod = "FIRST_HALF";
    private EventInputDialog.EventType currentEventType; // Track dialog event type

    // Runnable for updating the timer UI
    private final Runnable updateTimerThread = new Runnable() {
        public void run() {
            timeInMilliseconds = System.currentTimeMillis() - startTime;
            long updatedTime = timeSwapBuff + timeInMilliseconds;

            int secs = (int) (updatedTime / 1000);
            int mins = secs / 60;
            secs = secs % 60;
            
            currentMinute = mins;
            currentTimerText = String.format(Locale.getDefault(), "%02d:%02d", mins, secs);
            
            // Update match time in domain model
            if (matchViewModel != null && matchViewModel.offlineMatch.getValue() instanceof FootballMatch) {
                FootballMatch fm = (FootballMatch) matchViewModel.offlineMatch.getValue();
                fm.updateMatchTime(mins);
            }
            
            // Auto-save timer state every 10 seconds for persistence
            if (secs % 10 == 0) {
                saveTimerState();
            }
            
            // Update UI
            updateScoreboard();
            
            timerHandler.postDelayed(this, 1000);
        }
    };

    public FootballLiveScoreFragment() {
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
        return inflater.inflate(R.layout.fragment_football_live_score, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        matchViewModel = new ViewModelProvider(requireActivity()).get(MatchViewModel.class);

        initViews(view);
        setupListeners();

        if (matchId != null) {
            matchViewModel.loadOfflineMatch(matchId);
            observeViewModel();
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        // Save timer state when navigating away
        if (matchStarted) {
            saveTimerState();
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Prevent memory leaks by stopping the handler callbacks when view is destroyed
        timerHandler.removeCallbacks(updateTimerThread);
    }
    
    /**
     * Resets all fragment state variables when loading a new match.
     * CRITICAL: This prevents stale state from a previous match affecting the new one.
     */
    private void resetFragmentState() {
        android.util.Log.d("FootballLiveScore", "Resetting fragment state for new match");
        
        // Stop any running timer
        timerHandler.removeCallbacks(updateTimerThread);
        
        // Reset timer state
        startTime = 0L;
        timeInMilliseconds = 0L;
        timeSwapBuff = 0L;
        isRunning = false;
        matchStarted = false;
        timerStateRestored = false; // Reset timer restore tracking
        completedDialogShown = false; // Reset completion dialog tracking
        previousMatchStatus = null; // Reset previous status tracking
        
        // Reset match state
        currentTimerText = "00:00";
        currentMinute = 0;
        currentPeriod = "FIRST_HALF";
        currentEventType = null;
        currentMatch = null;
        
        // Reset UI button states
        if (btnStartTimer != null) btnStartTimer.setEnabled(true);
        if (btnPauseTimer != null) btnPauseTimer.setEnabled(false);
        if (btnHalfTime != null) btnHalfTime.setEnabled(false);
        if (btnEndMatch != null) btnEndMatch.setEnabled(false);
        
        // Enable input buttons for fresh match
        setInputButtonsEnabled(false); // Start disabled until match is LIVE
    }

    private void initViews(View view) {
        scoreboardView = view.findViewById(R.id.scoreboardView);
        btnShareMatch = view.findViewById(R.id.btnShareMatch);
        btnStartTimer = view.findViewById(R.id.btnStartTimer);
        btnPauseTimer = view.findViewById(R.id.btnPauseTimer);
        btnHalfTime = view.findViewById(R.id.btnHalfTime);
        btnEndMatch = view.findViewById(R.id.btnEndMatch);
        btnToggleSquad = view.findViewById(R.id.btnToggleSquad);
        cardGoal = view.findViewById(R.id.cardGoal);
        cardCard = view.findViewById(R.id.cardCard);
        cardSub = view.findViewById(R.id.cardSub);
        cardUndo = view.findViewById(R.id.cardUndo);
        rvEventTimeline = view.findViewById(R.id.rvEventTimeline);
        tvEmptyTimeline = view.findViewById(R.id.tvEmptyTimeline);
        llSquadContent = view.findViewById(R.id.llSquadContent);
        tvTeamAName = view.findViewById(R.id.tvTeamAName);
        tvTeamBName = view.findViewById(R.id.tvTeamBName);
        rvTeamAPlayers = view.findViewById(R.id.rvTeamAPlayers);
        rvTeamBPlayers = view.findViewById(R.id.rvTeamBPlayers);
        
        // Initially disable pause, half time, and end match buttons
        btnPauseTimer.setEnabled(false);
        btnHalfTime.setEnabled(false);
        btnEndMatch.setEnabled(false);
        
        // Setup RecyclerView for event timeline
        setupEventTimeline();
        
        // Setup player status views
        setupPlayerStatusViews();
    }
    
    /**
     * Initializes the RecyclerView for displaying match events chronologically.
     * Uses LinearLayoutManager in reverse order (newest events at top).
     */
    private void setupEventTimeline() {
        eventAdapter = new com.example.tournafy.ui.adapters.FootballEventAdapter();
        rvEventTimeline.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        rvEventTimeline.setAdapter(eventAdapter);
        
        // Optional: Add item decoration for spacing
        int spacing = (int) (8 * getResources().getDisplayMetrics().density);
        rvEventTimeline.addItemDecoration(new androidx.recyclerview.widget.DividerItemDecoration(
                requireContext(), androidx.recyclerview.widget.DividerItemDecoration.VERTICAL));
    }
    
    /**
     * Initializes the player status views to show who's playing vs substitutes.
     */
    private void setupPlayerStatusViews() {
        // Initialize adapters
        teamAPlayerAdapter = new com.example.tournafy.ui.adapters.PlayerStatusAdapter();
        teamBPlayerAdapter = new com.example.tournafy.ui.adapters.PlayerStatusAdapter();
        
        // Setup RecyclerViews
        rvTeamAPlayers.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        rvTeamAPlayers.setAdapter(teamAPlayerAdapter);
        
        rvTeamBPlayers.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        rvTeamBPlayers.setAdapter(teamBPlayerAdapter);
        
        // Toggle button for showing/hiding squad
        btnToggleSquad.setOnClickListener(v -> {
            if (llSquadContent.getVisibility() == View.GONE) {
                llSquadContent.setVisibility(View.VISIBLE);
                btnToggleSquad.setText("Hide");
            } else {
                llSquadContent.setVisibility(View.GONE);
                btnToggleSquad.setText("Show");
            }
        });
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
            
            android.util.Log.d("FootballLiveScore", "Share button clicked");
            android.util.Log.d("FootballLiveScore", "currentMatch: " + (currentMatch != null ? currentMatch.getName() : "null"));
            if (currentMatch != null) {
                android.util.Log.d("FootballLiveScore", "currentMatch visibilityLink: " + currentMatch.getVisibilityLink());
            }
            ShareHelper.shareMatch(requireContext(), currentMatch);
        });
        
        btnStartTimer.setOnClickListener(v -> startTimer());
        btnPauseTimer.setOnClickListener(v -> pauseTimer());
        btnHalfTime.setOnClickListener(v -> transitionToHalfTime());
        btnEndMatch.setOnClickListener(v -> endMatch());

        cardGoal.setOnClickListener(v -> showEventDialog(EventInputDialog.EventType.GOAL));
        cardCard.setOnClickListener(v -> showEventDialog(EventInputDialog.EventType.CARD));
        cardSub.setOnClickListener(v -> showEventDialog(EventInputDialog.EventType.SUB));
        
        cardUndo.setOnClickListener(v -> undoLastEvent());
    }

    private void observeViewModel() {
        // Observe match updates
        matchViewModel.offlineMatch.observe(getViewLifecycleOwner(), match -> {
            if (match instanceof FootballMatch) {
                FootballMatch footballMatch = (FootballMatch) match;
                
                // CRITICAL FIX: Detect when a NEW match is loaded and reset fragment state
                String loadedMatchId = footballMatch.getEntityId();
                if (currentLoadedMatchId != null && !currentLoadedMatchId.equals(loadedMatchId)) {
                    // A different match was loaded - reset all fragment state
                    android.util.Log.d("FootballLiveScore", "New match detected! Resetting fragment state. Old: " + 
                        currentLoadedMatchId + ", New: " + loadedMatchId);
                    resetFragmentState();
                }
                currentLoadedMatchId = loadedMatchId;
                
                // Determine matchStarted from actual match status (not persisted boolean)
                String status = footballMatch.getMatchStatus();
                if (status != null && (status.equals("LIVE") || status.equals("COMPLETED"))) {
                    matchStarted = true;
                } else {
                    // SCHEDULED or DRAFT - match hasn't started yet
                    matchStarted = false;
                }
                
                currentMatch = footballMatch; // Store for sharing
                updateUI(footballMatch);
                
                // Check if match JUST TRANSITIONED to completed - show option to view details
                // Only show dialog if: status changed from non-COMPLETED to COMPLETED, and dialog not already shown
                String currentStatus = footballMatch.getMatchStatus();
                if ("COMPLETED".equals(currentStatus) && 
                    previousMatchStatus != null && 
                    !"COMPLETED".equals(previousMatchStatus) && 
                    !completedDialogShown) {
                    completedDialogShown = true;
                    showMatchCompletedDialog();
                }
                previousMatchStatus = currentStatus;
            }
        });
        
        // Observe error messages
        matchViewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
        
        // Observe loading state
        matchViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            // Could show loading indicator here if needed
            // For now, we rely on delayed LiveData refresh like cricket
        });
    }
    
    /**
     * Updates all UI elements based on current match state.
     * Called whenever match data changes from LiveData.
     */
    private void updateUI(FootballMatch match) {
        if (match == null) return;
        
        // Update scoreboard
        updateScoreboard();
        
        // Update event timeline
        updateEventTimeline(match);
        
        // Update player status displays
        updatePlayerStatusDisplay(match);
        
        // Restore timer state on first load or when coming back to fragment
        // Check if we need to restore timer state (LIVE match with elapsed time that hasn't been restored yet)
        String statusStr = match.getMatchStatus();
        boolean isLiveOrCompleted = statusStr != null && (statusStr.equals("LIVE") || statusStr.equals("COMPLETED"));
        if (match.getElapsedTimeMillis() > 0 && isLiveOrCompleted && !timerStateRestored) {
            android.util.Log.d("FootballLiveScore", "Restoring timer state - elapsed: " + match.getElapsedTimeMillis() + 
                ", running: " + match.isTimerRunning());
            restoreTimerState();
            timerStateRestored = true;
        }
        
        // Check if match is completed and disable inputs
        // Handle DRAFT status (fallback to SCHEDULED)
        String statusString = match.getMatchStatus();
        if (statusString == null || statusString.equals("DRAFT")) {
            statusString = "SCHEDULED";
        }
        
        try {
            MatchStatus status = MatchStatus.valueOf(statusString);
            if (status == MatchStatus.COMPLETED) {
                setInputButtonsEnabled(false);
                btnStartTimer.setEnabled(false);
                btnPauseTimer.setEnabled(false);
                // Don't toast here - already shown in endMatch()
            } else if (status == MatchStatus.LIVE) {
                // Match is live, ensure buttons are enabled
                setInputButtonsEnabled(true);
            }
        } catch (IllegalArgumentException e) {
            // Invalid status, treat as SCHEDULED
            android.util.Log.w("FootballLiveScore", "Invalid match status: " + statusString + ", treating as SCHEDULED");
        }
        
        // Update period state from match
        currentPeriod = match.getMatchPeriod();
    }
    
    /**
     * Updates the player status displays showing who's playing and who's on the bench.
     */
    private void updatePlayerStatusDisplay(FootballMatch match) {
        if (match == null || match.getTeams() == null || match.getTeams().isEmpty()) {
            return;
        }
        
        // Update Team A
        if (match.getTeams().size() > 0) {
            com.example.tournafy.domain.models.team.MatchTeam teamA = match.getTeams().get(0);
            tvTeamAName.setText(teamA.getTeamName());
            teamAPlayerAdapter.setPlayers(teamA.getPlayers());
        }
        
        // Update Team B
        if (match.getTeams().size() > 1) {
            com.example.tournafy.domain.models.team.MatchTeam teamB = match.getTeams().get(1);
            tvTeamBName.setText(teamB.getTeamName());
            teamBPlayerAdapter.setPlayers(teamB.getPlayers());
        }
    }
    
    /**
     * Updates the event timeline RecyclerView with latest match events.
     * Shows/hides empty state message based on event count.
     */
    private void updateEventTimeline(FootballMatch match) {
        if (match == null) {
            android.util.Log.d("FootballTimeline", "Match is null");
            return;
        }
        
        List<com.example.tournafy.domain.models.match.football.FootballEvent> events = 
            match.getFootballEvents();
        
        android.util.Log.d("FootballTimeline", "Events count: " + (events != null ? events.size() : "null"));
        
        if (events == null || events.isEmpty()) {
            rvEventTimeline.setVisibility(View.GONE);
            tvEmptyTimeline.setVisibility(View.VISIBLE);
            android.util.Log.d("FootballTimeline", "Showing empty state");
        } else {
            rvEventTimeline.setVisibility(View.VISIBLE);
            tvEmptyTimeline.setVisibility(View.GONE);
            
            android.util.Log.d("FootballTimeline", "Showing " + events.size() + " events");
            
            // Set team names and IDs for the adapter
            String homeTeam = match.getTeams().size() > 0 ? 
                match.getTeams().get(0).getTeamName() : "Home";
            String awayTeam = match.getTeams().size() > 1 ? 
                match.getTeams().get(1).getTeamName() : "Away";
            String homeTeamId = match.getTeams().size() > 0 ? 
                match.getTeams().get(0).getTeamId() : null;
            String awayTeamId = match.getTeams().size() > 1 ? 
                match.getTeams().get(1).getTeamId() : null;
            
            eventAdapter.setTeamNames(homeTeam, awayTeam, homeTeamId, awayTeamId);
            eventAdapter.setEvents(events);
            
            // Scroll to bottom to show latest event
            rvEventTimeline.smoothScrollToPosition(events.size() - 1);
        }
    }
    
    /**
     * Updates the scoreboard view with current match data.
     */
    private void updateScoreboard() {
        if (matchViewModel.offlineMatch.getValue() instanceof FootballMatch) {
            FootballMatch fm = (FootballMatch) matchViewModel.offlineMatch.getValue();
            
            // Get team names from teams list
            String teamAName = fm.getTeams().size() > 0 ? fm.getTeams().get(0).getTeamName() : "Team A";
            String teamBName = fm.getTeams().size() > 1 ? fm.getTeams().get(1).getTeamName() : "Team B";
            
            // Update scoreboard with current state
            scoreboardView.updateFootballScore(
                teamAName, 
                teamBName, 
                fm.getHomeScore(), 
                fm.getAwayScore(), 
                currentTimerText
            );
            
            // Update match status display
            String matchStatus = fm.getMatchStatus();
            if (matchStatus != null) {
                // Format status for display (e.g., "LIVE" -> "⚽ LIVE", "COMPLETED" -> "Full Time")
                String displayStatus;
                switch (matchStatus) {
                    case "LIVE":
                        displayStatus = "⚽ LIVE";
                        break;
                    case "COMPLETED":
                        displayStatus = "Full Time";
                        break;
                    case "SCHEDULED":
                        displayStatus = "Not Started";
                        break;
                    case "DRAFT":
                        displayStatus = "Draft";
                        break;
                    default:
                        displayStatus = matchStatus;
                }
                scoreboardView.setMatchStatus(displayStatus);
            }
        }
    }
    
    /**
     * Enables or disables all input buttons (Goal, Card, Sub, Undo).
     * Used to prevent input after match completion.
     */
    private void setInputButtonsEnabled(boolean enabled) {
        cardGoal.setEnabled(enabled);
        cardCard.setEnabled(enabled);
        cardSub.setEnabled(enabled);
        cardUndo.setEnabled(enabled);
        
        // Also update clickable state
        cardGoal.setClickable(enabled);
        cardCard.setClickable(enabled);
        cardSub.setClickable(enabled);
        cardUndo.setClickable(enabled);
    }

    /**
     * Starts the match timer.
     * On first start, calls startMatch() on the domain model to initialize match state.
     */
    private void startTimer() {
        if (!isRunning) {
            // CRITICAL: Start match on first timer start (only if not already started)
            if (!matchStarted) {
                // Check if match is already LIVE (restored from database)
                if (matchViewModel.offlineMatch.getValue() instanceof FootballMatch) {
                    FootballMatch fm = (FootballMatch) matchViewModel.offlineMatch.getValue();
                    
                    // CRITICAL FIX: Validate that the loaded match is the correct one
                    // This prevents race conditions where old match data is still in the ViewModel
                    if (!fm.getEntityId().equals(matchId)) {
                        android.util.Log.w("FootballTimer", "Match ID mismatch! Expected: " + matchId + 
                            ", Got: " + fm.getEntityId() + ". Waiting for correct match to load...");
                        Toast.makeText(getContext(), "Loading match data, please try again...", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    // If match is already LIVE, just mark as started and resume timer
                    String statusString = fm.getMatchStatus();
                    if (statusString != null && statusString.equals(com.example.tournafy.domain.enums.MatchStatus.LIVE.name())) {
                        android.util.Log.d("FootballTimer", "Match already LIVE, skipping startMatch() call");
                        matchStarted = true;
                        // Fall through to start timer
                    } else {
                        // Validate teams and players before starting
                        if (fm.getTeams() == null || fm.getTeams().size() < 2) {
                            Toast.makeText(getContext(), "Cannot start match! Need at least 2 teams.", Toast.LENGTH_LONG).show();
                            return;
                        }
                        
                        if (fm.getMatchConfig() instanceof com.example.tournafy.domain.models.match.football.FootballMatchConfig) {
                            com.example.tournafy.domain.models.match.football.FootballMatchConfig config = 
                                (com.example.tournafy.domain.models.match.football.FootballMatchConfig) fm.getMatchConfig();
                            int minPlayers = config.getPlayersPerSide();
                            
                            // Check each team has minimum players
                            for (com.example.tournafy.domain.models.team.MatchTeam team : fm.getTeams()) {
                                if (team.getPlayers() == null || team.getPlayers().size() < minPlayers) {
                                    String errorMsg = "Cannot start match! Each team needs at least " + minPlayers + " players.";
                                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                                    return;
                                }
                            }
                        }
                        
                        // Start the match via ViewModel (handles validation, DRAFT→SCHEDULED→LIVE, and Firestore save)
                        matchViewModel.startMatch();
                        matchStarted = true;
                        
                        Toast.makeText(getContext(), "⚽ Match Started!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            
            startTime = System.currentTimeMillis();
            timerHandler.postDelayed(updateTimerThread, 0);
            isRunning = true;
            btnStartTimer.setEnabled(false);
            btnPauseTimer.setEnabled(true);
            btnHalfTime.setEnabled(true);
            btnEndMatch.setEnabled(true);
            
            // Save timer state to match
            saveTimerState();
        }
    }

    /**
     * Pauses the match timer.
     * Timer can be resumed later from the same point.
     */
    private void pauseTimer() {
        if (isRunning) {
            timeSwapBuff += timeInMilliseconds;
            timerHandler.removeCallbacks(updateTimerThread);
            isRunning = false;
            btnStartTimer.setEnabled(true);
            btnPauseTimer.setEnabled(false);
            
            // Save timer state to match
            saveTimerState();
        }
    }
    
    /**
     * Manually transitions to half time.
     * User clicks "Half Time" button to transition.
     */
    private void transitionToHalfTime() {
        if (matchViewModel.offlineMatch.getValue() instanceof FootballMatch) {
            FootballMatch match = (FootballMatch) matchViewModel.offlineMatch.getValue();
            
            if (currentPeriod.equals("FIRST_HALF")) {
                currentPeriod = "SECOND_HALF";
                match.setMatchStatus("SECOND_HALF");
                Toast.makeText(getContext(), "⚽ Second Half Started", Toast.LENGTH_SHORT).show();
                
                // Save the period change
                matchViewModel.updateTimerState(timeSwapBuff + timeInMilliseconds, isRunning);
                updateScoreboard();
            } else {
                Toast.makeText(getContext(), "Already in second half", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    /**
     * Ends the match manually.
     * Called when user clicks "End Match" button.
     * Disables all inputs and shows final result.
     */
    private void endMatch() {
        if (matchViewModel.offlineMatch.getValue() instanceof FootballMatch) {
            FootballMatch match = (FootballMatch) matchViewModel.offlineMatch.getValue();
            
            // CRITICAL FIX: Validate that the loaded match is the correct one
            if (!match.getEntityId().equals(matchId)) {
                android.util.Log.w("FootballLiveScore", "Match ID mismatch in endMatch! Expected: " + matchId);
                Toast.makeText(getContext(), "Loading match data, please wait...", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Check if already completed to avoid duplicate calls
            if (match.getMatchStatus().equals("COMPLETED")) {
                Toast.makeText(getContext(), "Match is already completed", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Stop timer completely - remove all callbacks
            timerHandler.removeCallbacks(updateTimerThread);
            isRunning = false;
            
            // End match in domain model
            match.endMatch();
            
            // Disable all inputs
            setInputButtonsEnabled(false);
            btnStartTimer.setEnabled(false);
            btnPauseTimer.setEnabled(false);
            btnHalfTime.setEnabled(false);
            btnEndMatch.setEnabled(false);
            
            // Show result
            String result;
            if (match.getHomeScore() > match.getAwayScore()) {
                String homeTeam = match.getTeams().size() > 0 ? match.getTeams().get(0).getTeamName() : "Home";
                result = homeTeam + " wins " + match.getHomeScore() + "-" + match.getAwayScore();
            } else if (match.getAwayScore() > match.getHomeScore()) {
                String awayTeam = match.getTeams().size() > 1 ? match.getTeams().get(1).getTeamName() : "Away";
                result = awayTeam + " wins " + match.getAwayScore() + "-" + match.getHomeScore();
            } else {
                result = "Match Drawn " + match.getHomeScore() + "-" + match.getAwayScore();
            }
            
            Toast.makeText(getContext(), "🏁 Match Completed! " + result, Toast.LENGTH_LONG).show();
            
            // Save final match state
            matchViewModel.updateTimerState(timeSwapBuff + timeInMilliseconds, false);
        }
    }

    /**
     * Saves the current timer state to the match object and persists to Firestore.
     * Called when timer is started, paused, or fragment lifecycle changes.
     */
    private void saveTimerState() {
        if (matchViewModel.offlineMatch.getValue() instanceof FootballMatch) {
            long elapsedTime = timeSwapBuff + timeInMilliseconds;
            android.util.Log.d("FootballTimer", "SAVING timer state - Elapsed: " + elapsedTime + 
                "ms (" + (elapsedTime/1000) + "s), Running: " + isRunning);
            matchViewModel.updateTimerState(elapsedTime, isRunning);
        }
    }

    /**
     * Restores timer state from the match object.
     * Called when fragment is created to maintain timer across navigation.
     */
    private void restoreTimerState() {
        if (matchViewModel.offlineMatch.getValue() instanceof FootballMatch) {
            FootballMatch match = (FootballMatch) matchViewModel.offlineMatch.getValue();
            
            long savedTime = match.getElapsedTimeMillis();
            boolean wasRunning = match.isTimerRunning();
            
            android.util.Log.d("FootballTimer", "RESTORING timer state - Saved: " + savedTime + 
                "ms (" + (savedTime/1000) + "s), WasRunning: " + wasRunning);
            
            if (savedTime > 0) {
                // Restore elapsed time
                timeSwapBuff = savedTime;
                timeInMilliseconds = 0;
                matchStarted = true; // Mark match as already started to prevent startMatch() being called again
                
                // Update display
                int seconds = (int) (savedTime / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;
                currentTimerText = String.format("%02d:%02d", minutes, seconds);
                currentMinute = minutes;
                
                android.util.Log.d("FootballTimer", "Restored display time: " + currentTimerText);
                
                // Update scoreboard with restored time
                updateScoreboard();
                
                // Resume timer if it was running
                if (wasRunning) {
                    startTime = System.currentTimeMillis();
                    timerHandler.postDelayed(updateTimerThread, 0);
                    isRunning = true;
                    btnStartTimer.setEnabled(false);
                    btnPauseTimer.setEnabled(true);
                    btnHalfTime.setEnabled(true); // Enable half time button
                    btnEndMatch.setEnabled(true); // Enable end match button
                    android.util.Log.d("FootballTimer", "Timer automatically resumed");
                } else {
                    // Timer was paused - still allow resume
                    btnStartTimer.setEnabled(true);
                    btnPauseTimer.setEnabled(false);
                    btnHalfTime.setEnabled(true); // Enable half time button even when paused
                    btnEndMatch.setEnabled(true); // Enable end match button even when paused
                    android.util.Log.d("FootballTimer", "Timer restored in paused state");
                }
            } else {
                android.util.Log.d("FootballTimer", "No saved timer state found (savedTime = 0)");
            }
        }
    }

    /**
     * Shows the event input dialog for adding goals, cards, or substitutions.
     * Validates match state before showing dialog.
     */
    private void showEventDialog(EventInputDialog.EventType type) {
        // Validate match is in LIVE state
        if (matchViewModel.offlineMatch.getValue() instanceof FootballMatch) {
            FootballMatch fm = (FootballMatch) matchViewModel.offlineMatch.getValue();
            
            // CRITICAL FIX: Validate that the loaded match is the correct one
            if (!fm.getEntityId().equals(matchId)) {
                android.util.Log.w("FootballLiveScore", "Match ID mismatch in showEventDialog! Expected: " + matchId);
                Toast.makeText(getContext(), "Loading match data, please wait...", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Handle DRAFT and ACTIVE status (fallback for legacy data)
            String statusString = fm.getMatchStatus();
            if (statusString == null || statusString.equals("DRAFT")) {
                statusString = "SCHEDULED";
            } else if (statusString.equals("ACTIVE")) {
                // Legacy support: ACTIVE was incorrectly used instead of LIVE
                statusString = "LIVE";
            }
            
            try {
                MatchStatus status = MatchStatus.valueOf(statusString);
                if (status != MatchStatus.LIVE) {
                    Toast.makeText(getContext(), "Match must be LIVE to add events", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (IllegalArgumentException e) {
                Toast.makeText(getContext(), "Match must be started to add events", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Store event type for callback
            currentEventType = type;
            
            // Get team names from teams list
            String teamAName = fm.getTeams().size() > 0 ? fm.getTeams().get(0).getTeamName() : "Team A";
            String teamBName = fm.getTeams().size() > 1 ? fm.getTeams().get(1).getTeamName() : "Team B";
            
            // Extract real players from teams - separate starting XI and substitutes
            ArrayList<String> playersA = new ArrayList<>();
            ArrayList<String> playersB = new ArrayList<>();
            ArrayList<String> startingPlayersA = new ArrayList<>();
            ArrayList<String> startingPlayersB = new ArrayList<>();
            ArrayList<String> subPlayersA = new ArrayList<>();
            ArrayList<String> subPlayersB = new ArrayList<>();
            
            // Team A
            if (fm.getTeams().size() > 0 && fm.getTeams().get(0).getPlayers() != null) {
                android.util.Log.d("FootballLiveScore", "Loading Team A players:");
                for (com.example.tournafy.domain.models.team.Player player : fm.getTeams().get(0).getPlayers()) {
                    playersA.add(player.getPlayerName()); // All players
                    android.util.Log.d("FootballLiveScore", "  " + player.getPlayerName() + 
                        " - Starting XI: " + player.isStartingXI());
                    if (player.isStartingXI()) {
                        startingPlayersA.add(player.getPlayerName());
                    } else {
                        subPlayersA.add(player.getPlayerName());
                    }
                }
            }
            
            // Team B
            if (fm.getTeams().size() > 1 && fm.getTeams().get(1).getPlayers() != null) {
                android.util.Log.d("FootballLiveScore", "Loading Team B players:");
                for (com.example.tournafy.domain.models.team.Player player : fm.getTeams().get(1).getPlayers()) {
                    playersB.add(player.getPlayerName()); // All players
                    android.util.Log.d("FootballLiveScore", "  " + player.getPlayerName() + 
                        " - Starting XI: " + player.isStartingXI());
                    if (player.isStartingXI()) {
                        startingPlayersB.add(player.getPlayerName());
                    } else {
                        subPlayersB.add(player.getPlayerName());
                    }
                }
            }
            
            // Debug logging
            android.util.Log.d("FootballLiveScore", "Team A - Total: " + playersA.size() + 
                ", Starting: " + startingPlayersA.size() + ", Subs: " + subPlayersA.size());
            android.util.Log.d("FootballLiveScore", "Team B - Total: " + playersB.size() + 
                ", Starting: " + startingPlayersB.size() + ", Subs: " + subPlayersB.size());
            
            // For GOAL and CARD: use starting XI ONLY if they exist, otherwise show error
            ArrayList<String> displayPlayersA;
            ArrayList<String> displayPlayersB;
            
            if (type == EventInputDialog.EventType.GOAL || type == EventInputDialog.EventType.CARD) {
                // Check if starting XI is properly configured
                if (startingPlayersA.isEmpty() || startingPlayersB.isEmpty()) {
                    // If no starting XI designated, use all players (backward compatibility)
                    android.util.Log.w("FootballLiveScore", "No starting XI found, using all players");
                    displayPlayersA = playersA;
                    displayPlayersB = playersB;
                } else {
                    // Use only starting XI
                    displayPlayersA = startingPlayersA;
                    displayPlayersB = startingPlayersB;
                }
            } else {
                // For SUB: will use separate lists in dialog (starting for out, subs for in)
                displayPlayersA = playersA;
                displayPlayersB = playersB;
            }
            
            // Validate teams have players
            if (playersA.isEmpty() || playersB.isEmpty()) {
                Toast.makeText(getContext(), "Teams must have players to add events", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Validate SUB has both starting and subs
            if (type == EventInputDialog.EventType.SUB) {
                if (startingPlayersA.isEmpty() || startingPlayersB.isEmpty()) {
                    Toast.makeText(getContext(), "No starting XI players found for substitution", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (subPlayersA.isEmpty() && subPlayersB.isEmpty()) {
                    Toast.makeText(getContext(), "No substitute players available", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            EventInputDialog dialog = EventInputDialog.newInstance(
                type, 
                teamAName, 
                teamBName, 
                displayPlayersA, 
                displayPlayersB,
                startingPlayersA,
                subPlayersA,
                startingPlayersB,
                subPlayersB
            );
            dialog.setListener(this);
            dialog.show(getChildFragmentManager(), "EventInput");
        }
    }

    /**
     * Callback from EventInputDialog.
     * Routes the event to the appropriate ViewModel method based on event type.
     * 
     * CRITICAL: Validates input, resolves player IDs, and persists events to database.
     * Uses Command Pattern via ViewModel for undo/redo support.
     * 
     * @param teamName Name of the team involved (matches team name from FootballMatch)
     * @param playerName Name/ID of the player involved
     * @param detail Additional info (e.g. "PENALTY", "RED", "YELLOW", or "playerInName" for subs)
     */
    @Override
    public void onEventCreated(String teamName, String playerName, String detail) {
        if (matchViewModel.offlineMatch.getValue() instanceof FootballMatch) {
            FootballMatch fm = (FootballMatch) matchViewModel.offlineMatch.getValue();
            
            // Validate inputs
            if (teamName == null || teamName.isEmpty()) {
                Toast.makeText(getContext(), "Please select a team", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (playerName == null || playerName.isEmpty()) {
                Toast.makeText(getContext(), "Please select a player", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Resolve team ID from team name
            String teamId = null;
            String playerId = null;
            
            for (com.example.tournafy.domain.models.team.MatchTeam team : fm.getTeams()) {
                if (team.getTeamName().equals(teamName)) {
                    teamId = team.getTeamId();
                    
                    // Resolve actual player ID from player name
                    if (team.getPlayers() != null) {
                        for (com.example.tournafy.domain.models.team.Player player : team.getPlayers()) {
                            if (player.getPlayerName().equals(playerName)) {
                                playerId = player.getPlayerId();
                                break;
                            }
                        }
                    }
                    break;
                }
            }
            
            if (teamId == null) {
                Toast.makeText(getContext(), "Error: Team not found", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (playerId == null) {
                Toast.makeText(getContext(), "Error: Player not found", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Get current minute from timer
            int minute = currentMinute;
            
            // Route to appropriate ViewModel method based on event type
            switch (currentEventType) {
                case GOAL:
                    handleGoalEvent(teamId, playerId, playerName, detail, minute);
                    break;
                    
                case CARD:
                    handleCardEvent(teamId, playerId, playerName, detail, minute);
                    break;
                    
                case SUB:
                    handleSubstitutionEvent(teamId, playerId, playerName, detail, minute, fm);
                    break;
            }
        }
    }
    
    @Override
    public void onGoalCreated(String teamName, String scorerName, String assisterName, String goalType) {
        if (matchViewModel.offlineMatch.getValue() instanceof FootballMatch) {
            FootballMatch fm = (FootballMatch) matchViewModel.offlineMatch.getValue();
            
            // Validate inputs
            if (teamName == null || teamName.isEmpty() || scorerName == null || scorerName.isEmpty()) {
                Toast.makeText(getContext(), "Please select team and scorer", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Resolve team ID and scorer ID
            String teamId = null;
            String scorerId = null;
            String assisterId = null;
            
            for (com.example.tournafy.domain.models.team.MatchTeam team : fm.getTeams()) {
                if (team.getTeamName().equals(teamName)) {
                    teamId = team.getTeamId();
                    
                    if (team.getPlayers() != null) {
                        for (com.example.tournafy.domain.models.team.Player player : team.getPlayers()) {
                            if (player.getPlayerName().equals(scorerName)) {
                                scorerId = player.getPlayerId();
                            }
                            if (!assisterName.isEmpty() && player.getPlayerName().equals(assisterName)) {
                                assisterId = player.getPlayerId();
                            }
                        }
                    }
                    break;
                }
            }
            
            if (teamId == null || scorerId == null) {
                Toast.makeText(getContext(), "Error: Team or player not found", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Call ViewModel with assister
            matchViewModel.addFootballGoal(teamId, scorerId, assisterId, goalType, currentMinute);
            
            String toastMsg = "⚽ Goal! " + scorerName;
            if (assisterId != null) {
                toastMsg += " (Assist: " + assisterName + ")";
            }
            toastMsg += " (" + currentMinute + "')";
            Toast.makeText(getContext(), toastMsg, Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Handles goal event creation.
     * Supports different goal types (OPEN_PLAY, PENALTY, FREE_KICK, HEADER, OWN_GOAL).
     */
    private void handleGoalEvent(String teamId, String playerId, String playerName, String detail, int minute) {
        String goalType = (detail != null && !detail.isEmpty()) ? detail : "OPEN_PLAY";
        
        matchViewModel.addFootballGoal(teamId, playerId, null, goalType, minute);
        Toast.makeText(getContext(), "⚽ Goal! " + playerName + " (" + minute + "')", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Handles card event creation (Yellow or Red cards).
     * Validates card type and assigns appropriate reason.
     */
    private void handleCardEvent(String teamId, String playerId, String playerName, String detail, int minute) {
        if (detail == null || (!detail.equals("RED") && !detail.equals("YELLOW"))) {
            Toast.makeText(getContext(), "Invalid card type", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String cardReason = "FOUL"; // Default reason, can be enhanced in dialog
        matchViewModel.addFootballCard(teamId, playerId, detail, cardReason, minute);
        
        String emoji = detail.equals("RED") ? "🟥" : "🟨";
        Toast.makeText(getContext(), emoji + " " + detail + " Card - " + playerName + " (" + minute + "')", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Handles substitution event creation.
     * Requires both playerOut and playerIn IDs.
     * 
     * TODO: Update EventInputDialog to support dual player selection for substitutions.
     */
    private void handleSubstitutionEvent(String teamId, String playerOutId, String playerOutName, 
                                        String playerInName, int minute, FootballMatch fm) {
        // Resolve playerIn ID from name
        String playerInId = null;
        
        for (com.example.tournafy.domain.models.team.MatchTeam team : fm.getTeams()) {
            if (team.getTeamId().equals(teamId) && team.getPlayers() != null) {
                for (com.example.tournafy.domain.models.team.Player player : team.getPlayers()) {
                    if (player.getPlayerName().equals(playerInName)) {
                        playerInId = player.getPlayerId();
                        break;
                    }
                }
            }
        }
        
        if (playerInId == null) {
            Toast.makeText(getContext(), "Error: Substitute player not found", Toast.LENGTH_SHORT).show();
            return;
        }
        
        matchViewModel.addFootballSubstitution(teamId, playerOutId, playerInId, minute);
        Toast.makeText(getContext(), "🔄 Substitution: " + playerOutName + " → " + playerInName + 
                       " (" + minute + "')", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Undoes the last event using Command Pattern.
     * Validates that there are events to undo.
     */
    private void undoLastEvent() {
        if (matchViewModel.offlineMatch.getValue() instanceof FootballMatch) {
            FootballMatch fm = (FootballMatch) matchViewModel.offlineMatch.getValue();
            
            // Check if there are events to undo
            if (fm.getFootballEvents() == null || fm.getFootballEvents().isEmpty()) {
                Toast.makeText(getContext(), "No events to undo", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Undo via CommandManager
            matchViewModel.undoLastEvent();
            Toast.makeText(getContext(), "↩️ Last event undone", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Shows a dialog when match is completed, offering to view match details.
     */
    private void showMatchCompletedDialog() {
        if (matchViewModel.offlineMatch.getValue() == null) return;
        
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Match Completed")
                .setMessage("The match has ended. Would you like to view the full match statistics and timeline?")
                .setPositiveButton("View Details", (dialog, which) -> {
                    navigateToMatchDetails();
                })
                .setNegativeButton("Stay Here", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }
    
    /**
     * Navigate to MatchActivity to view completed match details.
     */
    private void navigateToMatchDetails() {
        if (matchViewModel.offlineMatch.getValue() == null) return;
        
        android.content.Intent intent = new android.content.Intent(requireContext(), 
                com.example.tournafy.ui.activities.MatchActivity.class);
        intent.putExtra(com.example.tournafy.ui.activities.MatchActivity.EXTRA_MATCH_ID, 
                matchViewModel.offlineMatch.getValue().getEntityId());
        startActivity(intent);
    }
}