package com.example.tournafy.ui.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.tournafy.R;
import com.example.tournafy.domain.models.base.Match;
import com.example.tournafy.domain.models.match.cricket.CricketMatch;
import com.example.tournafy.domain.models.match.cricket.Innings;
import com.example.tournafy.domain.models.match.football.FootballMatch;
import com.example.tournafy.domain.models.team.MatchTeam;
import com.example.tournafy.ui.adapters.CricketMatchTabsAdapter;
import com.example.tournafy.ui.adapters.FootballMatchTabsAdapter;
import com.example.tournafy.ui.viewmodels.MatchViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MatchActivity extends AppCompatActivity {

    private static final String TAG = "MatchActivity";
    public static final String EXTRA_MATCH_ID = "match_id";

    private MatchViewModel matchViewModel;

    // UI Components
    private MaterialToolbar toolbar;
    private Chip chipStatus;
    private TextView tvTeamAName;
    private TextView tvTeamAScore;
    private TextView tvTeamBName;
    private TextView tvTeamBScore;
    private TextView tvMatchResult;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    private FragmentStateAdapter tabsAdapter;
    private String matchId;
    private boolean isFootball = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match);

        // Initialize ViewModel
        matchViewModel = new ViewModelProvider(this).get(MatchViewModel.class);

        initViews();
        setupToolbar();
        observeMatch(); // Setup ViewPager after determining match type

        // Check if opened from deep link
        android.content.Intent intent = getIntent();
        android.net.Uri data = intent.getData();
        
        if (data != null) {
            // Deep link: https://tournafy.app/match/{visibilityLink} OR tournafy://match/{visibilityLink}
            String visibilityLink = data.getLastPathSegment();
            Log.d(TAG, "Opened from deep link - Scheme: " + data.getScheme() + 
                ", Host: " + data.getHost() + ", Visibility Link: " + visibilityLink);
            
            if (visibilityLink != null && !visibilityLink.isEmpty()) {
                // Load match by visibility link
                matchViewModel.loadMatchByVisibilityLink(visibilityLink);
            } else {
                Log.e(TAG, "Invalid deep link - no visibility link found");
                Toast.makeText(this, "Invalid match link", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            // Normal flow - get match ID from intent
            matchId = getIntent().getStringExtra(EXTRA_MATCH_ID);
            if (matchId == null) {
                Log.e(TAG, "No match ID provided");
                finish();
                return;
            }
            
            // Load match by ID
            matchViewModel.loadMatchById(matchId);
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        chipStatus = findViewById(R.id.chipStatus);
        tvTeamAName = findViewById(R.id.tvTeamAName);
        tvTeamAScore = findViewById(R.id.tvTeamAScore);
        tvTeamBName = findViewById(R.id.tvTeamBName);
        tvTeamBScore = findViewById(R.id.tvTeamBScore);
        tvMatchResult = findViewById(R.id.tvMatchResult);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Match Details");
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupViewPager(boolean isFootball) {
        if (isFootball) {
            tabsAdapter = new FootballMatchTabsAdapter(this);
        } else {
            tabsAdapter = new CricketMatchTabsAdapter(this);
        }
        
        viewPager.setAdapter(tabsAdapter);

        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                if (isFootball) {
                    switch (position) {
                        case 0:
                            tab.setText("Statistics");
                            break;
                        case 1:
                            tab.setText("Lineups");
                            break;
                        case 2:
                            tab.setText("Timeline");
                            break;
                    }
                } else {
                    switch (position) {
                        case 0:
                            tab.setText("Scorecard");
                            break;
                        case 1:
                            tab.setText("Lineups");
                            break;
                    }
                }
            }
        }).attach();
    }

    private void observeMatch() {
        matchViewModel.getCurrentMatch().observe(this, match -> {
            if (match instanceof FootballMatch) {
                if (!isFootball) {
                    isFootball = true;
                    setupViewPager(true);
                }
                updateFootballScorecard((FootballMatch) match);
            } else if (match instanceof CricketMatch) {
                if (isFootball) {
                    isFootball = false;
                    setupViewPager(false);
                } else if (tabsAdapter == null) {
                    setupViewPager(false);
                }
                updateCricketScorecard((CricketMatch) match);
            }
        });
    }

    private void updateCricketScorecard(CricketMatch match) {
        // Update match status
        String status = match.getMatchStatus();
        chipStatus.setText(status);
        
        // Set chip color based on status
        if ("LIVE".equals(status)) {
            chipStatus.setChipBackgroundColorResource(R.color.live_red);
        } else if ("COMPLETED".equals(status)) {
            chipStatus.setChipBackgroundColorResource(R.color.success_green);
        } else {
            chipStatus.setChipBackgroundColorResource(R.color.md_theme_dark_primaryContainer);
        }

        // Get teams
        List<MatchTeam> teams = match.getTeams();
        if (teams == null || teams.size() < 2) {
            Log.e(TAG, "Invalid teams data");
            return;
        }

        MatchTeam teamA = teams.get(0);
        MatchTeam teamB = teams.get(1);

        // Update team names
        tvTeamAName.setText(teamA.getTeamName());
        tvTeamBName.setText(teamB.getTeamName());

        // Update scores from innings
        List<Innings> inningsList = match.getInnings();
        if (inningsList != null && !inningsList.isEmpty()) {
            // Find innings for each team
            String teamAScore = getTeamScore(inningsList, teamA.getTeamId());
            String teamBScore = getTeamScore(inningsList, teamB.getTeamId());
            
            tvTeamAScore.setText(teamAScore);
            tvTeamBScore.setText(teamBScore);
        } else {
            tvTeamAScore.setText("0/0");
            tvTeamBScore.setText("0/0");
        }

        // Update match result
        if (match.getMatchResult() != null) {
            String resultText = match.getMatchResult().getResultText();
            tvMatchResult.setText(resultText);
            tvMatchResult.setVisibility(View.VISIBLE);
        } else if ("COMPLETED".equals(status)) {
            tvMatchResult.setText("Match Completed");
            tvMatchResult.setVisibility(View.VISIBLE);
        } else {
            tvMatchResult.setVisibility(View.GONE);
        }
    }

    private void updateFootballScorecard(FootballMatch match) {
        // Update match status
        String status = match.getMatchStatus();
        chipStatus.setText(status);
        
        // Set chip color based on status
        if ("LIVE".equals(status)) {
            chipStatus.setChipBackgroundColorResource(R.color.live_red);
        } else if ("COMPLETED".equals(status)) {
            chipStatus.setChipBackgroundColorResource(R.color.success_green);
        } else {
            chipStatus.setChipBackgroundColorResource(R.color.md_theme_dark_primaryContainer);
        }

        // Get teams
        List<MatchTeam> teams = match.getTeams();
        if (teams == null || teams.size() < 2) {
            Log.e(TAG, "Invalid teams data");
            return;
        }

        MatchTeam teamA = teams.get(0);
        MatchTeam teamB = teams.get(1);

        // Update team names
        tvTeamAName.setText(teamA.getTeamName());
        tvTeamBName.setText(teamB.getTeamName());

        // Update scores
        tvTeamAScore.setText(String.valueOf(match.getHomeScore()));
        tvTeamBScore.setText(String.valueOf(match.getAwayScore()));

        // Update match result
        if (match.getMatchResult() != null) {
            String resultText = match.getMatchResult().getResultText();
            tvMatchResult.setText(resultText);
            tvMatchResult.setVisibility(View.VISIBLE);
        } else if ("COMPLETED".equals(status)) {
            // Generate result text
            if (match.getHomeScore() > match.getAwayScore()) {
                tvMatchResult.setText(teamA.getTeamName() + " wins " + match.getHomeScore() + "-" + match.getAwayScore());
            } else if (match.getAwayScore() > match.getHomeScore()) {
                tvMatchResult.setText(teamB.getTeamName() + " wins " + match.getAwayScore() + "-" + match.getHomeScore());
            } else {
                tvMatchResult.setText("Match Drawn " + match.getHomeScore() + "-" + match.getAwayScore());
            }
            tvMatchResult.setVisibility(View.VISIBLE);
        } else {
            tvMatchResult.setVisibility(View.GONE);
        }
    }

    private String getTeamScore(List<Innings> inningsList, String teamId) {
        int totalRuns = 0;
        int totalWickets = 0;
        double totalOvers = 0.0;

        for (Innings innings : inningsList) {
            if (innings.getBattingTeamId().equals(teamId)) {
                totalRuns += innings.getTotalRuns();
                totalWickets += innings.getWicketsFallen();
                totalOvers = innings.getTotalOvers();
            }
        }

        if (totalOvers > 0) {
            int completeOvers = (int) totalOvers;
            int balls = (int) ((totalOvers - completeOvers) * 10);
            return String.format(Locale.getDefault(), "%d/%d (%d.%d)", 
                totalRuns, totalWickets, completeOvers, balls);
        } else {
            return String.format(Locale.getDefault(), "%d/%d", totalRuns, totalWickets);
        }
    }
}
