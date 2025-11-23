package com.example.tournafy.ui.fragments.match.football;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.tournafy.R;
import com.example.tournafy.domain.models.match.football.FootballMatch;
import com.example.tournafy.domain.models.match.football.FootballEvent;
import com.example.tournafy.ui.viewmodels.MatchViewModel;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * FootballStatisticsFragment - Displays match statistics for a football match.
 * 
 * Features:
 * - Possession percentage with progress bar
 * - Shot statistics (total shots, shots on target)
 * - Disciplinary stats (yellow cards, red cards)
 * - Other match statistics (corners, fouls, offsides)
 * 
 * All statistics are calculated from match events.
 */
@AndroidEntryPoint
public class FootballStatisticsFragment extends Fragment {

    private MatchViewModel matchViewModel;
    
    // Statistics views
    private LinearProgressIndicator progressPossession;
    private TextView tvHomeTeamName, tvAwayTeamName;
    private TextView tvHomePossession, tvAwayPossession;
    private TextView tvHomeShots, tvAwayShots;
    private TextView tvHomeShotsOnTarget, tvAwayShotsOnTarget;
    private TextView tvHomeCorners, tvAwayCorners;
    private TextView tvHomeFouls, tvAwayFouls;
    private TextView tvHomeYellowCards, tvAwayYellowCards;
    private TextView tvHomeRedCards, tvAwayRedCards;
    private TextView tvHomeOffsides, tvAwayOffsides;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_football_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        matchViewModel = new ViewModelProvider(requireActivity()).get(MatchViewModel.class);

        initViews(view);
        observeViewModel();
    }

    private void initViews(View view) {
        // Possession - using correct IDs from XML
        progressPossession = view.findViewById(R.id.progressPossession);
        tvHomePossession = view.findViewById(R.id.tvPossessionA);
        tvAwayPossession = view.findViewById(R.id.tvPossessionB);
        
        // Team names
        tvHomeTeamName = view.findViewById(R.id.tvHomeTeamName);
        tvAwayTeamName = view.findViewById(R.id.tvAwayTeamName);
        
        // Stats - using component_stat_item includes
        // Shots
        tvHomeShots = view.findViewById(R.id.statShots).findViewById(R.id.tvHomeValue);
        tvAwayShots = view.findViewById(R.id.statShots).findViewById(R.id.tvAwayValue);
        view.findViewById(R.id.statShots).findViewById(R.id.tvStatLabel).setVisibility(View.VISIBLE);
        ((TextView)view.findViewById(R.id.statShots).findViewById(R.id.tvStatLabel)).setText("Shots");
        
        // Shots on Target
        tvHomeShotsOnTarget = view.findViewById(R.id.statShotsOnTarget).findViewById(R.id.tvHomeValue);
        tvAwayShotsOnTarget = view.findViewById(R.id.statShotsOnTarget).findViewById(R.id.tvAwayValue);
        ((TextView)view.findViewById(R.id.statShotsOnTarget).findViewById(R.id.tvStatLabel)).setText("Shots on Target");
        
        // Corners
        tvHomeCorners = view.findViewById(R.id.statCorners).findViewById(R.id.tvHomeValue);
        tvAwayCorners = view.findViewById(R.id.statCorners).findViewById(R.id.tvAwayValue);
        ((TextView)view.findViewById(R.id.statCorners).findViewById(R.id.tvStatLabel)).setText("Corners");
        
        // Fouls
        tvHomeFouls = view.findViewById(R.id.statFouls).findViewById(R.id.tvHomeValue);
        tvAwayFouls = view.findViewById(R.id.statFouls).findViewById(R.id.tvAwayValue);
        ((TextView)view.findViewById(R.id.statFouls).findViewById(R.id.tvStatLabel)).setText("Fouls");
        
        // Yellow Cards
        tvHomeYellowCards = view.findViewById(R.id.statYellowCards).findViewById(R.id.tvHomeValue);
        tvAwayYellowCards = view.findViewById(R.id.statYellowCards).findViewById(R.id.tvAwayValue);
        ((TextView)view.findViewById(R.id.statYellowCards).findViewById(R.id.tvStatLabel)).setText("Yellow Cards");
        
        // Red Cards
        tvHomeRedCards = view.findViewById(R.id.statRedCards).findViewById(R.id.tvHomeValue);
        tvAwayRedCards = view.findViewById(R.id.statRedCards).findViewById(R.id.tvAwayValue);
        ((TextView)view.findViewById(R.id.statRedCards).findViewById(R.id.tvStatLabel)).setText("Red Cards");
        
        // Offsides
        tvHomeOffsides = view.findViewById(R.id.statOffsides).findViewById(R.id.tvHomeValue);
        tvAwayOffsides = view.findViewById(R.id.statOffsides).findViewById(R.id.tvAwayValue);
        ((TextView)view.findViewById(R.id.statOffsides).findViewById(R.id.tvStatLabel)).setText("Offsides");
    }

    private void observeViewModel() {
        matchViewModel.offlineMatch.observe(getViewLifecycleOwner(), match -> {
            if (match instanceof FootballMatch) {
                updateStatistics((FootballMatch) match);
            }
        });
    }

    /**
     * Updates all statistics based on match events.
     * Calculates stats from FootballEvent list.
     */
    private void updateStatistics(FootballMatch match) {
        if (match.getTeams() == null || match.getTeams().size() < 2) {
            return;
        }

        String homeTeamId = match.getTeams().get(0).getTeamId();
        String awayTeamId = match.getTeams().get(1).getTeamId();
        
        tvHomeTeamName.setText(match.getTeams().get(0).getTeamName());
        tvAwayTeamName.setText(match.getTeams().get(1).getTeamName());

        // Calculate statistics from events
        MatchStatistics stats = calculateStatistics(match.getFootballEvents(), homeTeamId, awayTeamId);

        // Update possession
        int homePossession = stats.homePossession;
        int awayPossession = stats.awayPossession;
        if (tvHomePossession != null) {
            tvHomePossession.setText(homePossession + "%");
        }
        if (tvAwayPossession != null) {
            tvAwayPossession.setText(awayPossession + "%");
        }
        if (progressPossession != null) {
            progressPossession.setProgress(homePossession);
        }

        // Update shots
        tvHomeShots.setText(String.valueOf(stats.homeShots));
        tvAwayShots.setText(String.valueOf(stats.awayShots));
        tvHomeShotsOnTarget.setText(String.valueOf(stats.homeShotsOnTarget));
        tvAwayShotsOnTarget.setText(String.valueOf(stats.awayShotsOnTarget));

        // Update other stats
        tvHomeCorners.setText(String.valueOf(stats.homeCorners));
        tvAwayCorners.setText(String.valueOf(stats.awayCorners));
        tvHomeFouls.setText(String.valueOf(stats.homeFouls));
        tvAwayFouls.setText(String.valueOf(stats.awayFouls));
        tvHomeYellowCards.setText(String.valueOf(stats.homeYellowCards));
        tvAwayYellowCards.setText(String.valueOf(stats.awayYellowCards));
        tvHomeRedCards.setText(String.valueOf(stats.homeRedCards));
        tvAwayRedCards.setText(String.valueOf(stats.awayRedCards));
        tvHomeOffsides.setText(String.valueOf(stats.homeOffsides));
        tvAwayOffsides.setText(String.valueOf(stats.awayOffsides));
    }

    /**
     * Calculates match statistics from event list.
     * Counts events by category and team.
     */
    private MatchStatistics calculateStatistics(List<FootballEvent> events, String homeTeamId, String awayTeamId) {
        MatchStatistics stats = new MatchStatistics();
        
        if (events == null || events.isEmpty()) {
            // Default possession split
            stats.homePossession = 50;
            stats.awayPossession = 50;
            return stats;
        }

        for (FootballEvent event : events) {
            boolean isHomeTeam = homeTeamId.equals(event.getTeamId());
            
            String category = event.getEventCategory();
            if (category == null) continue;

            switch (category) {
                case "GOAL":
                    // Count as shot on target
                    if (isHomeTeam) {
                        stats.homeShots++;
                        stats.homeShotsOnTarget++;
                    } else {
                        stats.awayShots++;
                        stats.awayShotsOnTarget++;
                    }
                    break;

                case "SHOT":
                    if (isHomeTeam) {
                        stats.homeShots++;
                        if (event.getShotDetail() != null && event.getShotDetail().isOnTarget()) {
                            stats.homeShotsOnTarget++;
                        }
                    } else {
                        stats.awayShots++;
                        if (event.getShotDetail() != null && event.getShotDetail().isOnTarget()) {
                            stats.awayShotsOnTarget++;
                        }
                    }
                    break;

                case "CARD":
                    if (event.getCardDetail() != null) {
                        if ("RED".equals(event.getCardDetail().getCardType())) {
                            if (isHomeTeam) stats.homeRedCards++;
                            else stats.awayRedCards++;
                        } else {
                            if (isHomeTeam) stats.homeYellowCards++;
                            else stats.awayYellowCards++;
                        }
                    }
                    break;

                case "CORNER":
                    if (isHomeTeam) stats.homeCorners++;
                    else stats.awayCorners++;
                    break;

                case "FOUL":
                    if (isHomeTeam) stats.homeFouls++;
                    else stats.awayFouls++;
                    break;

                case "OFFSIDE":
                    if (isHomeTeam) stats.homeOffsides++;
                    else stats.awayOffsides++;
                    break;
            }
        }

        // Calculate possession based on events (simple heuristic)
        int totalEvents = stats.homeShots + stats.awayShots + stats.homeCorners + stats.awayCorners;
        if (totalEvents > 0) {
            int homeEvents = stats.homeShots + stats.homeCorners;
            stats.homePossession = (homeEvents * 100) / totalEvents;
            stats.awayPossession = 100 - stats.homePossession;
        } else {
            stats.homePossession = 50;
            stats.awayPossession = 50;
        }

        return stats;
    }

    /**
     * Data class to hold calculated match statistics.
     */
    private static class MatchStatistics {
        int homePossession = 0;
        int awayPossession = 0;
        int homeShots = 0;
        int awayShots = 0;
        int homeShotsOnTarget = 0;
        int awayShotsOnTarget = 0;
        int homeCorners = 0;
        int awayCorners = 0;
        int homeFouls = 0;
        int awayFouls = 0;
        int homeYellowCards = 0;
        int awayYellowCards = 0;
        int homeRedCards = 0;
        int awayRedCards = 0;
        int homeOffsides = 0;
        int awayOffsides = 0;
    }
}
