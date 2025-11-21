package com.example.tournafy.ui.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tournafy.R;

import java.util.Locale;

public class ScoreboardView extends FrameLayout {

    private TextView tvTeamAName, tvTeamAScore, tvTeamBName, tvTeamBScore, tvOvers, tvMatchStatus, tvCRR;

    public ScoreboardView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ScoreboardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ScoreboardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        // FIX: 'this' as root, and 'true' for attachToRoot
        LayoutInflater.from(context).inflate(R.layout.component_scoreboard, this, true);

        // Bind Views
        tvTeamAName = findViewById(R.id.tvTeamAName);
        tvTeamAScore = findViewById(R.id.tvTeamAScore);
        tvTeamBName = findViewById(R.id.tvTeamBName);
        tvTeamBScore = findViewById(R.id.tvTeamBScore);
        tvOvers = findViewById(R.id.tvOvers);
        tvMatchStatus = findViewById(R.id.tvMatchStatus);
        tvCRR = findViewById(R.id.tvCRR);
    }

    public void updateCricketScore(String teamA, String teamB, int runs, int wickets, String overs, float runRate) {
        if (tvOvers != null) tvOvers.setVisibility(View.VISIBLE);
        if (tvCRR != null) tvCRR.setVisibility(View.VISIBLE);

        if (tvTeamAName != null) tvTeamAName.setText(teamA);
        if (tvTeamBName != null) tvTeamBName.setText(teamB);

        if (tvTeamAScore != null) {
            tvTeamAScore.setText(String.format(Locale.getDefault(), "%d/%d", runs, wickets));
        }

        if (tvTeamBScore != null) {
            // In a real app, show target or "Yet to bat"
            tvTeamBScore.setText("");
        }

        if (tvOvers != null) {
            // Display overs in format "12.3" (completed.balls)
            tvOvers.setText(String.format(Locale.getDefault(), "Overs: %s", overs));
        }

        if (tvCRR != null) {
            tvCRR.setText(String.format(Locale.getDefault(), "CRR: %.2f", runRate));
        }
    }

    public void updateFootballScore(String teamA, String teamB, int scoreA, int scoreB, String matchTime) {
        if (tvCRR != null) tvCRR.setVisibility(View.GONE);
        if (tvOvers != null) tvOvers.setVisibility(View.VISIBLE);

        if (tvTeamAName != null) tvTeamAName.setText(teamA);
        if (tvTeamBName != null) tvTeamBName.setText(teamB);

        if (tvTeamAScore != null) tvTeamAScore.setText(String.valueOf(scoreA));
        if (tvTeamBScore != null) tvTeamBScore.setText(String.valueOf(scoreB));

        if (tvOvers != null) tvOvers.setText(matchTime);
    }

    public void setMatchStatus(String status) {
        if (tvMatchStatus != null) {
            tvMatchStatus.setText(status);
        }
    }
}