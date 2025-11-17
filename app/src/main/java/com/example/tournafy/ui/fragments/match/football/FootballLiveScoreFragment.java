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
import com.example.tournafy.domain.models.match.football.FootballMatch;
import com.example.tournafy.ui.components.ScoreboardView;
import com.example.tournafy.ui.dialogs.EventInputDialog;
import com.example.tournafy.ui.viewmodels.MatchViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FootballLiveScoreFragment extends Fragment implements EventInputDialog.EventInputListener {

    private MatchViewModel matchViewModel;
    private String matchId;
    
    // UI Views
    private ScoreboardView scoreboardView;
    private MaterialButton btnStartTimer, btnPauseTimer;
    private MaterialCardView cardGoal, cardCard, cardSub, cardUndo;

    // Timer State
    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private long startTime = 0L;
    private long timeInMilliseconds = 0L;
    private long timeSwapBuff = 0L;
    private boolean isRunning = false;
    
    // Helper to format time string for display
    private String currentTimerText = "00:00";

    // Runnable for updating the timer UI
    private final Runnable updateTimerThread = new Runnable() {
        public void run() {
            timeInMilliseconds = System.currentTimeMillis() - startTime;
            long updatedTime = timeSwapBuff + timeInMilliseconds;

            int secs = (int) (updatedTime / 1000);
            int mins = secs / 60;
            secs = secs % 60;
            
            currentTimerText = String.format(Locale.getDefault(), "%02d:%02d", mins, secs);
            
            // Update the scoreboard with current timer
            if (matchViewModel != null && matchViewModel.offlineMatch.getValue() instanceof FootballMatch) {
                 FootballMatch fm = (FootballMatch) matchViewModel.offlineMatch.getValue();
                 
                 // Get team names from teams list
                 String teamAName = fm.getTeams().size() > 0 ? fm.getTeams().get(0).getTeamName() : "Team A";
                 String teamBName = fm.getTeams().size() > 1 ? fm.getTeams().get(1).getTeamName() : "Team B";
                 
                 scoreboardView.updateFootballScore(
                     teamAName, 
                     teamBName, 
                     fm.getHomeScore(), 
                     fm.getAwayScore(), 
                     currentTimerText
                 );
             }
            
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
    }

    private void setupListeners() {
        btnStartTimer.setOnClickListener(v -> startTimer());
        btnPauseTimer.setOnClickListener(v -> pauseTimer());

        cardGoal.setOnClickListener(v -> showEventDialog(EventInputDialog.EventType.GOAL));
        cardCard.setOnClickListener(v -> showEventDialog(EventInputDialog.EventType.CARD));
        cardSub.setOnClickListener(v -> showEventDialog(EventInputDialog.EventType.SUB));
        
        cardUndo.setOnClickListener(v -> {
            matchViewModel.undoLastEvent();
            Toast.makeText(getContext(), "Last event undone", Toast.LENGTH_SHORT).show();
        });
    }

    private void observeViewModel() {
        matchViewModel.offlineMatch.observe(getViewLifecycleOwner(), match -> {
            if (match instanceof FootballMatch) {
                FootballMatch fm = (FootballMatch) match;
                
                // Get team names from teams list
                String teamAName = fm.getTeams().size() > 0 ? fm.getTeams().get(0).getTeamName() : "Team A";
                String teamBName = fm.getTeams().size() > 1 ? fm.getTeams().get(1).getTeamName() : "Team B";
                
                // Update UI with latest score from Domain
                scoreboardView.updateFootballScore(
                    teamAName, 
                    teamBName, 
                    fm.getHomeScore(), 
                    fm.getAwayScore(), 
                    currentTimerText // Keep the local timer reference
                );
            }
        });
    }

    private void startTimer() {
        if (!isRunning) {
            startTime = System.currentTimeMillis();
            timerHandler.postDelayed(updateTimerThread, 0);
            isRunning = true;
            btnStartTimer.setEnabled(false);
            btnPauseTimer.setEnabled(true);
        }
    }

    private void pauseTimer() {
        if (isRunning) {
            timeSwapBuff += timeInMilliseconds;
            timerHandler.removeCallbacks(updateTimerThread);
            isRunning = false;
            btnStartTimer.setEnabled(true);
            btnPauseTimer.setEnabled(false);
        }
    }

    private void showEventDialog(EventInputDialog.EventType type) {
        // Retrieve current match data to pass to dialog
        if (matchViewModel.offlineMatch.getValue() instanceof FootballMatch) {
            FootballMatch fm = (FootballMatch) matchViewModel.offlineMatch.getValue();
            
            // Get team names from teams list
            String teamAName = fm.getTeams().size() > 0 ? fm.getTeams().get(0).getTeamName() : "Team A";
            String teamBName = fm.getTeams().size() > 1 ? fm.getTeams().get(1).getTeamName() : "Team B";
            
            // In a real implementation, these would come from team players
            ArrayList<String> playersA = new ArrayList<>();
            playersA.add("Player A1"); playersA.add("Player A2");
            
            ArrayList<String> playersB = new ArrayList<>();
            playersB.add("Player B1"); playersB.add("Player B2");

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
     * @param teamName Name of the team involved (or "Team A" / "Team B")
     * @param playerName Name/ID of the player involved
     * @param detail Additional info (e.g. "Penalty", "Red Card")
     */
    @Override
    public void onEventCreated(String teamName, String playerName, String detail) {
        // Delegate logic to ViewModel which uses Commands
        // Note: You would map the strings back to IDs or use proper Objects in a full app
        
        // Example logic for GOAL
        // We determine if it's Team A or B based on name comparison
        boolean isHomeTeam = false;
        if (matchViewModel.offlineMatch.getValue() instanceof FootballMatch) {
             FootballMatch fm = (FootballMatch) matchViewModel.offlineMatch.getValue();
             String teamAName = fm.getTeams().size() > 0 ? fm.getTeams().get(0).getTeamName() : "Team A";
             if (teamName.equals(teamAName)) isHomeTeam = true;
        }

        // Pass 0 as placeholder for minute, ViewModel can grab it or we pass parsed timer
        // Assuming currentTimerText "MM:SS" -> parse to int minutes
        int minute = 0;
        try {
            minute = Integer.parseInt(currentTimerText.split(":")[0]);
        } catch (Exception e) { minute = 0; }

        // Call the no-argument method
        matchViewModel.addFootballEvent(); 
        
        Toast.makeText(getContext(), detail + " added for " + playerName, Toast.LENGTH_SHORT).show();
    }
}