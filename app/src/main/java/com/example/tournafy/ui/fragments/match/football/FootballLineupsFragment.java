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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tournafy.R;
import com.example.tournafy.domain.models.match.football.FootballMatch;
import com.example.tournafy.domain.models.team.MatchTeam;
import com.example.tournafy.domain.models.statistics.PlayerStatistics;
import com.example.tournafy.ui.adapters.PlayingXIAdapter;
import com.example.tournafy.ui.viewmodels.MatchViewModel;

import java.util.List;
import java.util.stream.Collectors;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * FootballLineupsFragment - Displays team lineups for a football match.
 * * Features:
 * - Team A and Team B player lists
 * - Shows all players in each team
 * - Integrates with MatchViewModel to fetch live stats
 */
@AndroidEntryPoint
public class FootballLineupsFragment extends Fragment {

    private MatchViewModel matchViewModel;

    // Team A views
    private TextView tvTeamAName;
    private RecyclerView rvTeamAPlayers;
    private PlayingXIAdapter teamAAdapter;

    // Team B views
    private TextView tvTeamBName;
    private RecyclerView rvTeamBPlayers;
    private PlayingXIAdapter teamBAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_football_lineups, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        matchViewModel = new ViewModelProvider(requireActivity()).get(MatchViewModel.class);
        initViews(view);
        observeViewModel();
    }

    private void initViews(View view) {
        // Team A
        tvTeamAName = view.findViewById(R.id.tvTeamAName);
        rvTeamAPlayers = view.findViewById(R.id.rvTeamA);

        // Team B
        tvTeamBName = view.findViewById(R.id.tvTeamBName);
        rvTeamBPlayers = view.findViewById(R.id.rvTeamB);

        // Setup RecyclerViews
        // For football, we disable cricket-specific stats
        teamAAdapter = new PlayingXIAdapter(false, false);
        rvTeamAPlayers.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTeamAPlayers.setAdapter(teamAAdapter);

        teamBAdapter = new PlayingXIAdapter(false, false);
        rvTeamBPlayers.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTeamBPlayers.setAdapter(teamBAdapter);
    }

    private void observeViewModel() {
        // 1. Observe the current match details
        matchViewModel.getCurrentMatch().observe(getViewLifecycleOwner(), match -> {
            if (match instanceof FootballMatch) {
                updateLineups((FootballMatch) match);

                // 2. Once we have the match, observe the stats for this match entity
                matchViewModel.getPlayerStats(match.getEntityId()).observe(getViewLifecycleOwner(), stats -> {
                    if (stats != null) {
                        updatePlayerStats(stats);
                    }
                });
            }
        });
    }

    /**
     * Updates both team lineups with player lists.
     */
    private void updateLineups(FootballMatch match) {
        if (match.getTeams() == null || match.getTeams().size() < 2) {
            return;
        }

        MatchTeam teamA = match.getTeams().get(0);
        MatchTeam teamB = match.getTeams().get(1);

        // Update Team A
        tvTeamAName.setText(teamA.getTeamName());
        teamAAdapter.setPlayers(teamA.getPlayers());

        // Update Team B
        tvTeamBName.setText(teamB.getTeamName());
        teamBAdapter.setPlayers(teamB.getPlayers());
    }

    /**
     * Maps the list of stats to the adapters.
     */
    private void updatePlayerStats(List<PlayerStatistics> stats) {
        if (stats == null || stats.isEmpty()) return;

        // Create a map for O(1) access inside the adapter
        try {
            var statsMap = stats.stream()
                    .collect(Collectors.toMap(PlayerStatistics::getPlayerId, s -> s));

            teamAAdapter.setPlayerStatistics(statsMap);
            teamBAdapter.setPlayerStatistics(statsMap);
        } catch (Exception e) {
            // Handle duplicate keys or stream errors gracefully
            e.printStackTrace();
        }
    }
}