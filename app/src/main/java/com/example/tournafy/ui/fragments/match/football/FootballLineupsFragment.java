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
import com.example.tournafy.ui.adapters.PlayingXIAdapter;
import com.example.tournafy.ui.viewmodels.MatchViewModel;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * FootballLineupsFragment - Displays team lineups for a football match.
 * 
 * Features:
 * - Team A and Team B player lists
 * - Shows all players in each team
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
        // For football, we don't need batting/bowling distinction - just show players
        // Use false for showCricketStats to hide economy rate, strike rate, etc.
        teamAAdapter = new PlayingXIAdapter(false, false);
        rvTeamAPlayers.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTeamAPlayers.setAdapter(teamAAdapter);
        
        teamBAdapter = new PlayingXIAdapter(false, false);
        rvTeamBPlayers.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTeamBPlayers.setAdapter(teamBAdapter);
    }

    private void observeViewModel() {
        matchViewModel.offlineMatch.observe(getViewLifecycleOwner(), match -> {
            if (match instanceof FootballMatch) {
                updateLineups((FootballMatch) match);
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
}
