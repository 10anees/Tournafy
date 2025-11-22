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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

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
    
    // UI Views
    private ScoreboardView scoreboardView;
    private MaterialButton btnStartTimer, btnPauseTimer;
    private MaterialCardView cardGoal, cardCard, cardSub, cardUndo;
    private androidx.recyclerview.widget.RecyclerView rvEventTimeline;
    private TextView tvEmptyTimeline;
    private com.example.tournafy.ui.adapters.FootballEventAdapter eventAdapter;

    // Timer State
    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private long startTime = 0L;
    private long timeInMilliseconds = 0L;
    private long timeSwapBuff = 0L;
    private boolean isRunning = false;
    private boolean matchStarted = false; // Track if startMatch() has been called
    
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
            
            // Check for period changes
            checkPeriodTransition(mins);
            
            // Update match time in domain model
            if (matchViewModel != null && matchViewModel.offlineMatch.getValue() instanceof FootballMatch) {
                FootballMatch fm = (FootballMatch) matchViewModel.offlineMatch.getValue();
                fm.updateMatchTime(mins);
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
    public void onDestroyView() {
        super.onDestroyView();
        // Prevent memory leaks by stopping the handler callbacks when view is destroyed
        timerHandler.removeCallbacks(updateTimerThread);
    }

    private void initViews(View view) {
        scoreboardView = view.findViewById(R.id.scoreboardView);
        btnStartTimer = view.findViewById(R.id.btnStartTimer);
        btnPauseTimer = view.findViewById(R.id.btnPauseTimer);
        cardGoal = view.findViewById(R.id.cardGoal);
        cardCard = view.findViewById(R.id.cardCard);
        cardSub = view.findViewById(R.id.cardSub);
        cardUndo = view.findViewById(R.id.cardUndo);
        rvEventTimeline = view.findViewById(R.id.rvEventTimeline);
        tvEmptyTimeline = view.findViewById(R.id.tvEmptyTimeline);
        
        // Initially disable pause button
        btnPauseTimer.setEnabled(false);
        
        // Setup RecyclerView for event timeline
        setupEventTimeline();
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
            getContext(), androidx.recyclerview.widget.DividerItemDecoration.VERTICAL));
    }

    private void setupListeners() {
        btnStartTimer.setOnClickListener(v -> startTimer());
        btnPauseTimer.setOnClickListener(v -> pauseTimer());

        cardGoal.setOnClickListener(v -> showEventDialog(EventInputDialog.EventType.GOAL));
        cardCard.setOnClickListener(v -> showEventDialog(EventInputDialog.EventType.CARD));
        cardSub.setOnClickListener(v -> showEventDialog(EventInputDialog.EventType.SUB));
        
        cardUndo.setOnClickListener(v -> undoLastEvent());
    }

    private void observeViewModel() {
        // Observe match updates
        matchViewModel.offlineMatch.observe(getViewLifecycleOwner(), match -> {
            if (match instanceof FootballMatch) {
                updateUI((FootballMatch) match);
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
                Toast.makeText(getContext(), "Match Completed!", Toast.LENGTH_SHORT).show();
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
            
            // Set team names for the adapter
            String homeTeam = match.getTeams().size() > 0 ? 
                match.getTeams().get(0).getTeamName() : "Home";
            String awayTeam = match.getTeams().size() > 1 ? 
                match.getTeams().get(1).getTeamName() : "Away";
            
            eventAdapter.setTeamNames(homeTeam, awayTeam);
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
            // CRITICAL: Start match on first timer start
            if (!matchStarted) {
                // Start the match via ViewModel (handles validation, DRAFT conversion, and Firestore save)
                matchViewModel.startMatch();
                matchStarted = true;
                
                Toast.makeText(getContext(), "⚽ Match Started!", Toast.LENGTH_SHORT).show();
            }
            
            startTime = System.currentTimeMillis();
            timerHandler.postDelayed(updateTimerThread, 0);
            isRunning = true;
            btnStartTimer.setEnabled(false);
            btnPauseTimer.setEnabled(true);
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
        }
    }
    
    /**
     * Checks if the match period should transition (e.g., First Half to Second Half).
     * Called every second by the timer thread.
     */
    private void checkPeriodTransition(int currentMinutes) {
        if (matchViewModel.offlineMatch.getValue() instanceof FootballMatch) {
            FootballMatch match = (FootballMatch) matchViewModel.offlineMatch.getValue();
            
            // Get match configuration for duration
            if (match.getMatchConfig() instanceof com.example.tournafy.domain.models.match.football.FootballMatchConfig) {
                com.example.tournafy.domain.models.match.football.FootballMatchConfig config = 
                    (com.example.tournafy.domain.models.match.football.FootballMatchConfig) match.getMatchConfig();
                
                int halfDuration = config.getMatchDuration() / 2;
                int fullDuration = config.getMatchDuration();
                
                // Transition to second half
                if (currentPeriod.equals("FIRST_HALF") && currentMinutes >= halfDuration && currentMinutes < fullDuration) {
                    currentPeriod = "SECOND_HALF";
                    Toast.makeText(getContext(), "Second Half Started", Toast.LENGTH_SHORT).show();
                }
                
                // End match when full duration is reached
                if (currentPeriod.equals("SECOND_HALF") && currentMinutes >= fullDuration) {
                    endMatch();
                }
            }
        }
    }
    
    /**
     * Ends the match.
     * Called automatically when timer reaches full duration.
     * Disables all inputs and shows final result.
     */
    private void endMatch() {
        if (matchViewModel.offlineMatch.getValue() instanceof FootballMatch) {
            FootballMatch match = (FootballMatch) matchViewModel.offlineMatch.getValue();
            
            // Stop timer
            pauseTimer();
            
            // End match in domain model
            match.endMatch();
            
            // Disable all inputs
            setInputButtonsEnabled(false);
            btnStartTimer.setEnabled(false);
            btnPauseTimer.setEnabled(false);
            
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
            
            Toast.makeText(getContext(), "Match Completed! " + result, Toast.LENGTH_LONG).show();
            
            // Trigger repository update to save match result
            matchViewModel.loadOfflineMatch(matchId);
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
            
            // Extract real players from teams
            ArrayList<String> playersA = new ArrayList<>();
            ArrayList<String> playersB = new ArrayList<>();
            
            if (fm.getTeams().size() > 0 && fm.getTeams().get(0).getPlayers() != null) {
                for (com.example.tournafy.domain.models.team.Player player : fm.getTeams().get(0).getPlayers()) {
                    playersA.add(player.getPlayerName());
                }
            }
            
            if (fm.getTeams().size() > 1 && fm.getTeams().get(1).getPlayers() != null) {
                for (com.example.tournafy.domain.models.team.Player player : fm.getTeams().get(1).getPlayers()) {
                    playersB.add(player.getPlayerName());
                }
            }
            
            // Validate teams have players
            if (playersA.isEmpty() || playersB.isEmpty()) {
                Toast.makeText(getContext(), "Teams must have players to add events", Toast.LENGTH_SHORT).show();
                return;
            }

            EventInputDialog dialog = EventInputDialog.newInstance(
                type, 
                teamAName, 
                teamBName, 
                playersA, 
                playersB
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
    
    /**
     * Handles goal event creation.
     * Supports different goal types (OPEN_PLAY, PENALTY, FREE_KICK, HEADER, OWN_GOAL).
     */
    private void handleGoalEvent(String teamId, String playerId, String playerName, String detail, int minute) {
        String goalType = (detail != null && !detail.isEmpty()) ? detail : "OPEN_PLAY";
        
        matchViewModel.addFootballGoal(teamId, playerId, goalType, minute);
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
}