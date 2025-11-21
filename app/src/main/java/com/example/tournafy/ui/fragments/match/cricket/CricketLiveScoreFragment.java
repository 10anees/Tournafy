package com.example.tournafy.ui.fragments.match.cricket;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.tournafy.R;
import com.example.tournafy.domain.models.match.cricket.Ball;
import com.example.tournafy.domain.models.match.cricket.CricketMatch;
import com.example.tournafy.domain.models.match.cricket.Innings;
import com.example.tournafy.domain.models.match.cricket.Over;
import com.example.tournafy.domain.enums.MatchStatus;
import com.example.tournafy.ui.components.ScoreboardView;
import com.example.tournafy.ui.viewmodels.MatchViewModel;
import com.google.android.material.button.MaterialButton;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CricketLiveScoreFragment extends Fragment {

    private MatchViewModel matchViewModel;
    private String matchId;

    // UI Components from new Layout
    private MaterialButton btnStartMatch;
    private ScoreboardView scoreboardView;
    private ProgressBar progressBar;
    private TextView tvStriker, tvNonStriker, tvBowler;
    private LinearLayout llRecentBalls;

    // Keypad Buttons
    private Button btnZero, btnOne, btnTwo, btnThree, btnFour, btnFive, btnSix;
    private Button btnWide, btnNoBall, btnBye, btnLegBye;
    private Button btnWicket, btnUndo, btnRetire, btnSwap;

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
        scoreboardView = view.findViewById(R.id.scoreboardView);
        progressBar = scoreboardView.findViewById(R.id.progressBar);
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
    }

    private void observeViewModel() {
        // Observe match updates
        matchViewModel.offlineMatch.observe(getViewLifecycleOwner(), match -> {
            if (match instanceof CricketMatch) {
                updateUI((CricketMatch) match);
            }
        });
        
        // Observe loading state
        matchViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            if (progressBar != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });
        
        // Observe errors
        matchViewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                matchViewModel.clearErrorMessage();
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
        Innings currentInnings = match.getCurrentInnings();

        if (currentInnings != null) {
            // 1. Update Scoreboard View with proper overs format (e.g., "12.3")
            String oversText = matchViewModel.getCurrentOversText();
            float currentRunRate = matchViewModel.getCurrentRunRate();
            
            scoreboardView.updateCricketScore(
                    matchViewModel.getTeamAName(),
                    matchViewModel.getTeamBName(),
                    currentInnings.getTotalRuns(),
                    currentInnings.getWicketsFallen(),
                    oversText,
                    currentRunRate
            );

            // 2. Update Players - Now shows actual player names from team roster
            tvStriker.setText(matchViewModel.getStrikerId() + " *");
            tvNonStriker.setText(matchViewModel.getNonStrikerId());
            tvBowler.setText(matchViewModel.getCurrentBowlerId());
        }

        // 3. Update Recent Balls (This Over)
        updateRecentBalls(match);
        
        // 4. Update button states
        btnUndo.setEnabled(matchViewModel.canUndo());
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
}