package com.example.tournafy.ui.fragments.match.cricket;

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
import com.example.tournafy.domain.models.match.cricket.CricketMatch;
import com.example.tournafy.domain.models.team.MatchTeam;
import com.example.tournafy.ui.adapters.PlayingXIAdapter;
import com.example.tournafy.ui.viewmodels.MatchViewModel;
import com.google.android.material.chip.Chip;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CricketLineupsFragment extends Fragment {

    private MatchViewModel matchViewModel;
    
    // UI Components
    private TextView tvTeamAName;
    private Chip chipTeamAPlayers;
    private RecyclerView rvTeamAPlayers;
    
    private TextView tvTeamBName;
    private Chip chipTeamBPlayers;
    private RecyclerView rvTeamBPlayers;
    
    private PlayingXIAdapter teamAAdapter;
    private PlayingXIAdapter teamBAdapter;
    
    private CricketMatch cricketMatch;

    public CricketLineupsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cricket_lineups, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get ViewModel from activity
        matchViewModel = new ViewModelProvider(requireActivity()).get(MatchViewModel.class);
        
        initViews(view);
        setupRecyclerViews();
        observeMatch();
    }

    private void initViews(View view) {
        tvTeamAName = view.findViewById(R.id.tvTeamAName);
        chipTeamAPlayers = view.findViewById(R.id.chipTeamAPlayers);
        rvTeamAPlayers = view.findViewById(R.id.rvTeamAPlayers);
        
        tvTeamBName = view.findViewById(R.id.tvTeamBName);
        chipTeamBPlayers = view.findViewById(R.id.chipTeamBPlayers);
        rvTeamBPlayers = view.findViewById(R.id.rvTeamBPlayers);
    }

    private void setupRecyclerViews() {
        // Team A RecyclerView
        teamAAdapter = new PlayingXIAdapter(true);
        rvTeamAPlayers.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTeamAPlayers.setAdapter(teamAAdapter);
        rvTeamAPlayers.setNestedScrollingEnabled(false);
        
        // Team B RecyclerView
        teamBAdapter = new PlayingXIAdapter(false);
        rvTeamBPlayers.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTeamBPlayers.setAdapter(teamBAdapter);
        rvTeamBPlayers.setNestedScrollingEnabled(false);
    }

    private void observeMatch() {
        matchViewModel.getCurrentMatch().observe(getViewLifecycleOwner(), match -> {
            if (match instanceof CricketMatch) {
                this.cricketMatch = (CricketMatch) match;
                updateLineups();
            }
        });
    }

    private void updateLineups() {
        if (cricketMatch == null) return;
        
        List<MatchTeam> teams = cricketMatch.getTeams();
        if (teams == null || teams.size() < 2) return;
        
        // Team A
        MatchTeam teamA = teams.get(0);
        tvTeamAName.setText(teamA.getTeamName());
        chipTeamAPlayers.setText(teamA.getPlayers().size() + " Players");
        teamAAdapter.setPlayers(teamA.getPlayers());
        teamAAdapter.setBatsmanStats(cricketMatch.getBatsmanStatsMap());
        
        // Team B
        MatchTeam teamB = teams.get(1);
        tvTeamBName.setText(teamB.getTeamName());
        chipTeamBPlayers.setText(teamB.getPlayers().size() + " Players");
        teamBAdapter.setPlayers(teamB.getPlayers());
        teamBAdapter.setBowlerStats(cricketMatch.getBowlerStatsMap());
    }
}
