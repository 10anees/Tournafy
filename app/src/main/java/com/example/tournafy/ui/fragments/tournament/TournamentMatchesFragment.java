package com.example.tournafy.ui.fragments.tournament;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tournafy.R;
import com.example.tournafy.data.repository.offline.MatchFirestoreRepository;
import com.example.tournafy.data.repository.offline.TournamentMatchFirestoreRepository;
import com.example.tournafy.domain.models.base.Match;
import com.example.tournafy.domain.models.tournament.Tournament;
import com.example.tournafy.domain.models.tournament.TournamentMatch;
import com.example.tournafy.ui.activities.MatchActivity;
import com.example.tournafy.ui.adapters.tournament.TournamentMatchAdapter;
import com.example.tournafy.ui.adapters.tournament.TournamentMatchAdapter.TournamentMatchWithDetails;
import com.example.tournafy.ui.viewmodels.TournamentViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Fragment displaying all tournament matches.
 * Features:
 * - List of all matches in the tournament
 * - Filtering by status (All, Live, Completed, Scheduled)
 * - Grouped by stage/round
 * - Match cards showing teams, scores, status
 * - Click to view match details
 * - FAB for hosts to create new match
 * - Empty state when no matches
 */
@AndroidEntryPoint
public class TournamentMatchesFragment extends Fragment {

    private static final String TAG = "TournamentMatchesFrag";
    private static final String ARG_TOURNAMENT_ID = "tournament_id";
    private static final String ARG_IS_ONLINE = "is_online";

    @Inject
    TournamentMatchFirestoreRepository tournamentMatchRepository;
    
    @Inject
    MatchFirestoreRepository matchRepository;

    private TournamentViewModel tournamentViewModel;
    private TournamentMatchAdapter matchAdapter;
    private RecyclerView rvMatches;
    private View layoutEmptyState;
    private TextView tvEmptyMessage;
    private ChipGroup chipGroupFilter;
    private Chip chipAll, chipLive, chipCompleted, chipScheduled;
    private ExtendedFloatingActionButton fabCreateMatch;

    private String tournamentId;
    private boolean isOnline;
    private Tournament currentTournament;
    private String currentFilter = "ALL";
    
    // Store loaded matches
    private final Map<String, Match> loadedMatches = new HashMap<>();
    private final List<TournamentMatchWithDetails> matchesWithDetails = new ArrayList<>();

    public TournamentMatchesFragment() {}

    public static TournamentMatchesFragment newInstance(String tournamentId, boolean isOnline) {
        TournamentMatchesFragment fragment = new TournamentMatchesFragment();
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
        return inflater.inflate(R.layout.fragment_tournament_matches, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        tournamentViewModel = new ViewModelProvider(requireActivity()).get(TournamentViewModel.class);
        
        initViews(view);
        setupFilters();
        setupListeners();
        observeData();
    }
    
    private void initViews(View view) {
        rvMatches = view.findViewById(R.id.rvMatches);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);
        chipGroupFilter = view.findViewById(R.id.chipGroupFilter);
        chipAll = view.findViewById(R.id.chipAll);
        chipLive = view.findViewById(R.id.chipLive);
        chipCompleted = view.findViewById(R.id.chipCompleted);
        chipScheduled = view.findViewById(R.id.chipScheduled);
        fabCreateMatch = view.findViewById(R.id.fabCreateMatch);
        
        rvMatches.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMatches.setHasFixedSize(true);
        
        // Setup adapter
        setupAdapter();
    }
    
    private void setupAdapter() {
        matchAdapter = new TournamentMatchAdapter((tournamentMatch, match) -> {
            // Navigate to MatchActivity
            if (match != null) {
                // TODO: Check if user is host and match is SCHEDULED/LIVE
                // If host, navigate to host scoring interface
                // For now, always navigate to MatchActivity
                
                Intent intent = new Intent(requireContext(), MatchActivity.class);
                intent.putExtra(MatchActivity.EXTRA_MATCH_ID, match.getEntityId());
                intent.putExtra("IS_ONLINE", isOnline);
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Match data not loaded", Toast.LENGTH_SHORT).show();
            }
        });
        rvMatches.setAdapter(matchAdapter);
    }
    
    private void setupFilters() {
        if (chipGroupFilter != null) {
            chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds.isEmpty()) return;
                
                int checkedId = checkedIds.get(0);
                if (checkedId == R.id.chipAll) {
                    currentFilter = "ALL";
                } else if (checkedId == R.id.chipLive) {
                    currentFilter = "LIVE";
                } else if (checkedId == R.id.chipCompleted) {
                    currentFilter = "COMPLETED";
                } else if (checkedId == R.id.chipScheduled) {
                    currentFilter = "SCHEDULED";
                }
                
                loadTournamentMatches();
            });
        }
    }
    
    private void setupListeners() {
        if (fabCreateMatch != null) {
            fabCreateMatch.setOnClickListener(v -> showCreateMatchDialog());
        }
    }
    
    private void observeData() {
        // Observe tournament
        if (isOnline) {
            tournamentViewModel.onlineTournament.observe(getViewLifecycleOwner(), this::onTournamentLoaded);
        } else {
            tournamentViewModel.offlineTournament.observe(getViewLifecycleOwner(), this::onTournamentLoaded);
        }
    }
    
    private void onTournamentLoaded(Tournament tournament) {
        if (tournament == null) return;
        
        currentTournament = tournament;
        
        // Show/hide FAB based on user being host
        updateFabVisibility();
        
        // Load matches
        loadTournamentMatches();
    }
    
    private void updateFabVisibility() {
        if (fabCreateMatch == null) return;
        
        // TODO: Check if current user is host
        // For now, hide FAB as match creation is not yet implemented
        fabCreateMatch.setVisibility(View.GONE);
        
        // Once match creation is implemented:
        // boolean isHost = currentTournament.getHostUserId().equals(currentUserId);
        // boolean canCreateMatches = isHost && !"COMPLETED".equals(currentTournament.getStatus());
        // fabCreateMatch.setVisibility(canCreateMatches ? View.VISIBLE : View.GONE);
    }
    
    private void loadTournamentMatches() {
        if (tournamentId == null) {
            showEmptyState("Tournament ID not available");
            return;
        }
        
        Log.d(TAG, "Loading matches for tournament: " + tournamentId);
        
        // Load all tournament matches (filter will be applied later)
        LiveData<List<TournamentMatch>> matchesLiveData = 
            tournamentMatchRepository.getAllForTournament(tournamentId);
        
        matchesLiveData.observe(getViewLifecycleOwner(), tournamentMatches -> {
            if (tournamentMatches == null || tournamentMatches.isEmpty()) {
                Log.d(TAG, "No tournament matches found");
                showEmptyState(getEmptyMessage());
                return;
            }
            
            Log.d(TAG, "Found " + tournamentMatches.size() + " tournament matches");
            
            // Load actual Match details for each TournamentMatch
            loadMatchDetails(tournamentMatches);
        });
    }
    
    private void loadMatchDetails(List<TournamentMatch> tournamentMatches) {
        matchesWithDetails.clear();
        int totalMatches = tournamentMatches.size();
        final int[] loadedCount = {0};
        
        for (TournamentMatch tm : tournamentMatches) {
            if (tm.getMatchId() == null) {
                loadedCount[0]++;
                if (loadedCount[0] == totalMatches) {
                    filterAndDisplayMatches();
                }
                continue;
            }
            
            // Load actual Match data
            matchRepository.getById(tm.getMatchId()).observe(getViewLifecycleOwner(), match -> {
                if (match != null) {
                    loadedMatches.put(tm.getMatchId(), match);
                    matchesWithDetails.add(new TournamentMatchWithDetails(tm, match));
                    Log.d(TAG, "Loaded match: " + match.getName() + " (Status: " + match.getMatchStatus() + ")");
                }
                
                loadedCount[0]++;
                if (loadedCount[0] == totalMatches) {
                    Log.d(TAG, "All matches loaded, filtering and displaying");
                    filterAndDisplayMatches();
                }
            });
        }
    }
    
    private void filterAndDisplayMatches() {
        List<TournamentMatchWithDetails> filtered = new ArrayList<>();
        
        for (TournamentMatchWithDetails mwd : matchesWithDetails) {
            if (mwd.match == null) continue;
            
            String status = mwd.match.getMatchStatus();
            boolean include = false;
            
            switch (currentFilter) {
                case "LIVE":
                    include = "LIVE".equalsIgnoreCase(status) || 
                             "IN_PROGRESS".equalsIgnoreCase(status);
                    break;
                case "COMPLETED":
                    include = "COMPLETED".equalsIgnoreCase(status);
                    break;
                case "SCHEDULED":
                    include = "SCHEDULED".equalsIgnoreCase(status) || 
                             "DRAFT".equalsIgnoreCase(status);
                    break;
                default: // ALL
                    include = true;
            }
            
            if (include) {
                filtered.add(mwd);
            }
        }
        
        if (filtered.isEmpty()) {
            showEmptyState(getEmptyMessage());
        } else {
            // Sort by match order
            filtered.sort((a, b) -> Integer.compare(
                a.tournamentMatch.getMatchOrder(), 
                b.tournamentMatch.getMatchOrder()
            ));
            
            // Pass the complete list with Match details to adapter
            showMatches(filtered);
        }
    }
    
    private String getEmptyMessage() {
        switch (currentFilter) {
            case "LIVE":
                return "No live matches at the moment.\nCheck back when matches are in progress.";
            case "COMPLETED":
                return "No completed matches yet.\nResults will appear here after matches are finished.";
            case "SCHEDULED":
                return "No matches scheduled yet.\nThe tournament host needs to create matches.";
            default:
                return "No matches in this tournament yet.\n\nMatches will be created when the tournament starts.";
        }
    }
    
    private void showMatches(List<?> matches) {
        if (matches == null || matches.isEmpty()) {
            rvMatches.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
            
            // Update empty message based on filter
            String emptyMessage;
            switch (currentFilter) {
                case "LIVE":
                    emptyMessage = "No live matches at the moment";
                    break;
                case "COMPLETED":
                    emptyMessage = "No completed matches yet";
                    break;
                case "SCHEDULED":
                    emptyMessage = "No scheduled matches";
                    break;
                default:
                    emptyMessage = "No matches added yet. Tap + to create a match.";
            }
            tvEmptyMessage.setText(emptyMessage);
            
            Log.d(TAG, "Showing empty state: " + emptyMessage);
        } else {
            rvMatches.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
            
            // Pass data to adapter (cast to expected type)
            @SuppressWarnings("unchecked")
            List<TournamentMatchWithDetails> matchList = (List<TournamentMatchWithDetails>) matches;
            matchAdapter.setMatchesWithDetails(matchList);
            
            Log.d(TAG, "Displaying " + matches.size() + " matches");
        }
    }
    
    private void showEmptyState(String message) {
        rvMatches.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.VISIBLE);
        
        if (tvEmptyMessage != null) {
            tvEmptyMessage.setText(message);
        }
    }
    
    private void onMatchClick(Object match) {
        // Future implementation
        /*
        Intent intent = new Intent(requireContext(), MatchActivity.class);
        intent.putExtra(MatchActivity.EXTRA_MATCH_ID, match.getMatchId());
        intent.putExtra(MatchActivity.EXTRA_IS_ONLINE, isOnline);
        startActivity(intent);
        */
        
        android.widget.Toast.makeText(getContext(), 
            "Match details will open here", 
            android.widget.Toast.LENGTH_SHORT).show();
    }
    
    private void showCreateMatchDialog() {
        if (currentTournament == null) {
            android.widget.Toast.makeText(getContext(), 
                "Tournament data not loaded", 
                android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        
        // TODO: Implement match creation dialog
        // Should allow host to:
        // 1. Select two teams from tournament teams
        // 2. Choose stage/round
        // 3. Set date/time
        // 4. Set venue
        // 5. Create match
        
        android.widget.Toast.makeText(getContext(), 
            "Match creation - Coming soon", 
            android.widget.Toast.LENGTH_SHORT).show();
    }
}
