package com.example.tournafy.ui.fragments.tournament;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tournafy.R;
import com.example.tournafy.data.repository.offline.MatchFirestoreRepository;
import com.example.tournafy.data.repository.offline.TournamentMatchFirestoreRepository;
import com.example.tournafy.domain.models.base.Match;
import com.example.tournafy.domain.models.tournament.TournamentMatch;
import com.example.tournafy.ui.activities.MatchActivity;
import com.example.tournafy.ui.adapters.tournament.TournamentMatchAdapter;
import com.example.tournafy.ui.adapters.tournament.TournamentMatchAdapter.TournamentMatchWithDetails;
import com.example.tournafy.ui.viewmodels.TournamentViewModel;
import com.example.tournafy.ui.views.BracketView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Fragment displaying knockout bracket visualization.
 * Shows tournament tree structure with match results.
 */
@AndroidEntryPoint
public class TournamentKnockoutFragment extends Fragment {

    private static final String TAG = "TournamentKnockout";
    private static final String ARG_TOURNAMENT_ID = "tournament_id";
    private static final String ARG_IS_ONLINE = "is_online";

    @Inject
    TournamentMatchFirestoreRepository tournamentMatchRepository;
    
    @Inject
    MatchFirestoreRepository matchRepository;

    private TournamentViewModel tournamentViewModel;
    private BracketView bracketView;
    private FrameLayout bracketContainer;
    private TextView tvBracketPlaceholder;
    private RecyclerView rvUpcomingMatches;
    private TournamentMatchAdapter upcomingMatchesAdapter;

    private String tournamentId;
    private boolean isOnline;
    
    // Store loaded matches
    private final Map<String, Match> loadedMatches = new HashMap<>();
    private final List<TournamentMatchWithDetails> matchesWithDetails = new ArrayList<>();

    public TournamentKnockoutFragment() {}

    public static TournamentKnockoutFragment newInstance(String tournamentId, boolean isOnline) {
        TournamentKnockoutFragment fragment = new TournamentKnockoutFragment();
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
        return inflater.inflate(R.layout.fragment_tournament_knockout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        tournamentViewModel = new ViewModelProvider(requireActivity()).get(TournamentViewModel.class);
        
        initViews(view);
        setupBracketView();
        setupUpcomingMatchesAdapter();
        observeData();
        loadKnockoutMatches();
    }

    private void initViews(View view) {
        bracketContainer = view.findViewById(R.id.bracketContainer);
        tvBracketPlaceholder = view.findViewById(R.id.tvBracketPlaceholder);
        rvUpcomingMatches = view.findViewById(R.id.rvUpcomingMatches);
        rvUpcomingMatches.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setupBracketView() {
        // TODO: Implement BracketView once TournamentMatch display model is complete
        // For now, show placeholder
        tvBracketPlaceholder.setVisibility(View.VISIBLE);
        tvBracketPlaceholder.setText("Bracket visualization coming soon");
        /*
        bracketView = new BracketView(requireContext());
        bracketView.setOnMatchClickListener(match -> {
            // Navigate to match details
            navigateToMatch(match.getMatchId());
        });
        
        // Add BracketView to container
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        bracketContainer.addView(bracketView, params);
        */
    }

    private void setupUpcomingMatchesAdapter() {
        upcomingMatchesAdapter = new TournamentMatchAdapter((tournamentMatch, match) -> {
            if (match != null) {
                // TODO: Check if user is host and match is SCHEDULED/LIVE
                // If host, navigate to host scoring interface
                // For now, always navigate to MatchActivity
                
                Intent intent = new Intent(requireContext(), MatchActivity.class);
                intent.putExtra(MatchActivity.EXTRA_MATCH_ID, match.getEntityId());
                intent.putExtra("IS_ONLINE", isOnline);
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Match data not available", Toast.LENGTH_SHORT).show();
            }
        });
        rvUpcomingMatches.setAdapter(upcomingMatchesAdapter);
    }

    private void observeData() {
        if (isOnline) {
            tournamentViewModel.onlineTournament.observe(getViewLifecycleOwner(), tournament -> {
                if (tournament != null) {
                    loadKnockoutMatches();
                }
            });
        } else {
            tournamentViewModel.offlineTournament.observe(getViewLifecycleOwner(), tournament -> {
                if (tournament != null) {
                    loadKnockoutMatches();
                }
            });
        }
    }

    private void loadKnockoutMatches() {
        if (tournamentId == null) {
            Log.e(TAG, "Cannot load matches - tournament ID is null");
            return;
        }

        Log.d(TAG, "Loading knockout matches for tournament: " + tournamentId);

        // Load TournamentMatch entities for this tournament
        tournamentMatchRepository.getAllForTournament(tournamentId).observe(
            getViewLifecycleOwner(),
            tournamentMatches -> {
                if (tournamentMatches != null && !tournamentMatches.isEmpty()) {
                    Log.d(TAG, "Loaded " + tournamentMatches.size() + " tournament matches");
                    
                    // Clear previous data
                    loadedMatches.clear();
                    matchesWithDetails.clear();
                    
                    // Load Match details for each TournamentMatch
                    loadMatchDetailsForBrackets(tournamentMatches);
                } else {
                    Log.e(TAG, "Failed to load tournament matches or list is empty");
                    showEmptyState();
                }
            }
        );
    }
    
    private void loadMatchDetailsForBrackets(List<TournamentMatch> tournamentMatches) {
        final int totalMatches = tournamentMatches.size();
        final int[] loadedCount = {0};
        
        Log.d(TAG, "Loading match details for " + totalMatches + " matches");
        
        for (TournamentMatch tournamentMatch : tournamentMatches) {
            String matchId = tournamentMatch.getMatchId();
            
            if (matchId == null) {
                Log.w(TAG, "TournamentMatch has null matchId, skipping");
                loadedCount[0]++;
                continue;
            }
            
            // Load Match entity
            matchRepository.getById(matchId).observe(getViewLifecycleOwner(), match -> {
                loadedCount[0]++;
                
                if (match != null) {
                    loadedMatches.put(matchId, match);
                    matchesWithDetails.add(new TournamentMatchWithDetails(tournamentMatch, match));
                    Log.d(TAG, "Loaded match details for: " + matchId);
                } else {
                    Log.w(TAG, "Failed to load match: " + matchId);
                    // Add without match details
                    matchesWithDetails.add(new TournamentMatchWithDetails(tournamentMatch, null));
                }
                
                // When all matches are loaded, display them
                if (loadedCount[0] == totalMatches) {
                    Log.d(TAG, "All bracket matches loaded, displaying");
                    displayBrackets();
                }
            });
        }
    }
    
    private void displayBrackets() {
        if (matchesWithDetails.isEmpty()) {
            showEmptyState();
            return;
        }
        
        // Sort by match order
        matchesWithDetails.sort((a, b) -> Integer.compare(
            a.tournamentMatch.getMatchOrder(), 
            b.tournamentMatch.getMatchOrder()
        ));
        
        // Show matches in RecyclerView
        tvBracketPlaceholder.setVisibility(View.GONE);
        rvUpcomingMatches.setVisibility(View.VISIBLE);
        upcomingMatchesAdapter.setMatchesWithDetails(matchesWithDetails);
        
        Log.d(TAG, "Displaying " + matchesWithDetails.size() + " bracket matches");
        
        // TODO: Implement actual bracket tree visualization
        // For now, showing linear list of matches
    }
    
    private void showEmptyState() {
        tvBracketPlaceholder.setVisibility(View.VISIBLE);
        tvBracketPlaceholder.setText("No knockout matches yet.\n\nGenerate brackets from the Overview tab to create knockout rounds.");
        rvUpcomingMatches.setVisibility(View.GONE);
        Log.d(TAG, "Showing empty state - no matches");
    }


}
