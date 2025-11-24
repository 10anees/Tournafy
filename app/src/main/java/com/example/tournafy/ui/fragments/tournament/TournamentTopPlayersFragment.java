package com.example.tournafy.ui.fragments.tournament;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tournafy.R;
import com.example.tournafy.domain.models.statistics.PlayerStatistics;
import com.example.tournafy.ui.adapters.tournament.TopPlayersAdapter;
import com.example.tournafy.ui.viewmodels.TournamentViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Fragment displaying top players statistics.
 * Shows leading batsmen, bowlers, and all-rounders for the tournament.
 */
@AndroidEntryPoint
public class TournamentTopPlayersFragment extends Fragment {

    private static final String ARG_TOURNAMENT_ID = "tournament_id";
    private static final String ARG_IS_ONLINE = "is_online";

    private TournamentViewModel tournamentViewModel;
    private ChipGroup chipGroupCategory;
    private RecyclerView rvTopPlayers;
    private TopPlayersAdapter adapter;
    private TopPlayersAdapter.Category currentCategory = TopPlayersAdapter.Category.BATTING;

    private String tournamentId;
    private boolean isOnline;

    public TournamentTopPlayersFragment() {}

    public static TournamentTopPlayersFragment newInstance(String tournamentId, boolean isOnline) {
        TournamentTopPlayersFragment fragment = new TournamentTopPlayersFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TOURNAMENT_ID, tournamentId);
        args.putBoolean(ARG_IS_ONLINE, isOnline);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tournamentId = getArguments().getString(ARG_TOURNAMENT_ID);
            isOnline = getArguments().getBoolean(ARG_IS_ONLINE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tournament_top_players, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        tournamentViewModel = new ViewModelProvider(requireActivity()).get(TournamentViewModel.class);
        
        initViews(view);
        setupAdapter();
        setupCategoryFilter();
        observeData();
        loadTopPlayers();
    }

    private void initViews(View view) {
        chipGroupCategory = view.findViewById(R.id.chipGroupCategory);
        rvTopPlayers = view.findViewById(R.id.rvTopPlayers);
        rvTopPlayers.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setupAdapter() {
        adapter = new TopPlayersAdapter(player -> {
            // TODO: Navigate to player profile or show detailed statistics
        }, currentCategory);
        rvTopPlayers.setAdapter(adapter);
    }

    private void setupCategoryFilter() {
        chipGroupCategory.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);
                
                if (checkedId == R.id.chipBatting) {
                    currentCategory = TopPlayersAdapter.Category.BATTING;
                } else if (checkedId == R.id.chipBowling) {
                    currentCategory = TopPlayersAdapter.Category.BOWLING;
                } else if (checkedId == R.id.chipFielding) {
                    currentCategory = TopPlayersAdapter.Category.FIELDING;
                }
                
                // Recreate adapter with new category
                setupAdapter();
                loadTopPlayers();
            }
        });
        
        // Set default selection
        ((Chip) chipGroupCategory.findViewById(R.id.chipBatting)).setChecked(true);
    }

    private void observeData() {
        if (isOnline) {
            tournamentViewModel.onlineTournament.observe(getViewLifecycleOwner(), tournament -> {
                if (tournament != null) {
                    // Tournament data updated, could refresh player stats if needed
                }
            });
        } else {
            tournamentViewModel.offlineTournament.observe(getViewLifecycleOwner(), tournament -> {
                if (tournament != null) {
                    // Tournament data updated, could refresh player stats if needed
                }
            });
        }
    }

    private void loadTopPlayers() {
        // TODO: Load actual player statistics from repository based on currentCategory
        // For now, show empty list (display model not yet implemented)
        adapter.submitList(new ArrayList<>());
    }

    private List<PlayerStatistics> generateMockPlayers() {
        // TODO: Implement mock data once display model is created
        // PlayerStatistics stores data in Map format, need to create display model or helper methods
        return new ArrayList<>();
        
        /*
        List<PlayerStatistics> players = new ArrayList<>();
        
        switch (currentCategory) {
            case BATTING:
                // Mock batting leaders
                for (int i = 1; i <= 10; i++) {
                    PlayerStatistics stats = new PlayerStatistics();
                    stats.setPlayerId("player" + i);
                    stats.setPlayerName("Batsman " + i);
                    stats.setTeamId("team" + ((i % 4) + 1));
                    stats.setTeamName("Team " + ((i % 4) + 1));
                    
                    // Mock batting stats
                    stats.setTotalRuns(500 - (i * 50));
                    stats.setBattingAverage(45.0 - (i * 2.0));
                    stats.setStrikeRate(135.0 - (i * 5.0));
                    stats.setInningsPlayed(10);
                    
                    players.add(stats);
                }
                break;
                
            case BOWLING:
                // Mock bowling leaders
                for (int i = 1; i <= 10; i++) {
                    PlayerStatistics stats = new PlayerStatistics();
                    stats.setPlayerId("player" + i);
                    stats.setPlayerName("Bowler " + i);
                    stats.setTeamId("team" + ((i % 4) + 1));
                    stats.setTeamName("Team " + ((i % 4) + 1));
                    
                    // Mock bowling stats
                    stats.setTotalWickets(20 - i);
                    stats.setBowlingAverage(18.0 + (i * 1.5));
                    stats.setEconomy(5.5 + (i * 0.3));
                    stats.setInningsBowled(10);
                    
                    players.add(stats);
                }
                break;
                
            case FIELDING:
                // Mock fielding leaders
                for (int i = 1; i <= 10; i++) {
                    PlayerStatistics stats = new PlayerStatistics();
                    stats.setPlayerId("player" + i);
                    stats.setPlayerName("Fielder " + i);
                    stats.setTeamId("team" + ((i % 4) + 1));
                    stats.setTeamName("Team " + ((i % 4) + 1));
                    
                    // Mock fielding stats
                    stats.setTotalCatches(15 - i);
                    stats.setTotalStumpings(5 - (i / 2));
                    stats.setTotalRunOuts(8 - (i / 2));
                    
                    players.add(stats);
                }
                break;
        }
        
        return players;
        */
    }
}
