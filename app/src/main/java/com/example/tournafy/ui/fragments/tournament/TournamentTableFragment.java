package com.example.tournafy.ui.fragments.tournament;

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
import com.example.tournafy.domain.models.team.TournamentTeam;
import com.example.tournafy.domain.models.tournament.Tournament;
import com.example.tournafy.ui.adapters.tournament.PointsTableAdapter;
import com.example.tournafy.ui.viewmodels.TournamentViewModel;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Fragment displaying tournament standings/points table.
 * Features:
 * - Sorted team rankings with comprehensive statistics
 * - Different metrics for Cricket (NRR) vs Football (GD)
 * - Highlighting of qualification spots
 * - Empty state when no teams or no matches played
 * - Automatic sorting by: Points > Win% > NRR/GD > Goals/Runs For
 * - Team detail view on click
 */
@AndroidEntryPoint
public class TournamentTableFragment extends Fragment {

    private static final String ARG_TOURNAMENT_ID = "tournament_id";
    private static final String ARG_IS_ONLINE = "is_online";

    private TournamentViewModel tournamentViewModel;
    private RecyclerView rvStandings;
    private View layoutEmptyState;
    private TextView tvEmptyMessage;
    private MaterialCardView cardLegend;
    private PointsTableAdapter adapter;

    private String tournamentId;
    private boolean isOnline;
    private boolean isCricket = false;
    private Tournament currentTournament;

    public TournamentTableFragment() {}

    public static TournamentTableFragment newInstance(String tournamentId, boolean isOnline) {
        TournamentTableFragment fragment = new TournamentTableFragment();
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
        return inflater.inflate(R.layout.fragment_tournament_table, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tournamentViewModel = new ViewModelProvider(requireActivity()).get(TournamentViewModel.class);

        initViews(view);
        setupAdapter();
        observeData();
    }

    private void initViews(View view) {
        rvStandings = view.findViewById(R.id.rvPointsTable);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);
        cardLegend = view.findViewById(R.id.cardLegend);

        rvStandings.setLayoutManager(new LinearLayoutManager(getContext()));
        // FIX: Removed setHasFixedSize(true) to resolve Lint build error
    }

    private void setupAdapter() {
        adapter = new PointsTableAdapter(this::onTeamClick, isCricket);
        rvStandings.setAdapter(adapter);
    }

    private void observeData() {
        // Observe tournament to get sport type
        if (isOnline) {
            tournamentViewModel.onlineTournament.observe(getViewLifecycleOwner(), this::onTournamentLoaded);
        } else {
            tournamentViewModel.offlineTournament.observe(getViewLifecycleOwner(), this::onTournamentLoaded);
        }

        // Observe teams data
        tournamentViewModel.tournamentTeams.observe(getViewLifecycleOwner(), this::onTeamsLoaded);

        // Observe loading state
        tournamentViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            // Could show loading indicator
        });
    }

    private void onTournamentLoaded(Tournament tournament) {
        if (tournament == null) return;

        currentTournament = tournament;

        // Determine sport type
        String sportId = tournament.getSportId();
        boolean newIsCricket = sportId != null &&
                (sportId.equalsIgnoreCase("CRICKET") || sportId.equalsIgnoreCase("cricket"));

        // If sport type changed, recreate adapter
        if (newIsCricket != isCricket) {
            isCricket = newIsCricket;
            adapter = new PointsTableAdapter(this::onTeamClick, isCricket);
            rvStandings.setAdapter(adapter);
        }

        // Update legend visibility
        updateLegendVisibility();
    }

    private void onTeamsLoaded(List<TournamentTeam> teams) {
        if (teams == null || teams.isEmpty()) {
            showEmptyState("No teams have been added to this tournament yet.");
            return;
        }

        // Check if any matches have been played
        boolean anyMatchesPlayed = false;
        for (TournamentTeam team : teams) {
            if (team.getMatchesPlayed() > 0) {
                anyMatchesPlayed = true;
                break;
            }
        }

        if (!anyMatchesPlayed) {
            showEmptyState("No matches have been played yet.\nStandings will appear once matches are completed.");
            // Still show teams with 0 points
            List<TournamentTeam> sortedTeams = sortTeams(teams);
            adapter.submitList(sortedTeams);
            return;
        }

        // Sort and display teams
        List<TournamentTeam> sortedTeams = sortTeams(teams);
        adapter.submitList(sortedTeams);
        showStandingsTable();
    }

    /**
     * Sort teams by tournament standings rules:
     * 1. Points (descending)
     * 2. Matches Won (descending)
     * 3. Net Run Rate / Goal Difference (descending)
     * 4. Goals/Runs For (descending)
     * 5. Team Name (alphabetical)
     */
    private List<TournamentTeam> sortTeams(List<TournamentTeam> teams) {
        List<TournamentTeam> sortedList = new ArrayList<>(teams);

        Collections.sort(sortedList, new Comparator<TournamentTeam>() {
            @Override
            public int compare(TournamentTeam t1, TournamentTeam t2) {
                // 1. Points (higher is better)
                if (t1.getPoints() != t2.getPoints()) {
                    return Integer.compare(t2.getPoints(), t1.getPoints());
                }

                // 2. Matches Won (higher is better)
                if (t1.getMatchesWon() != t2.getMatchesWon()) {
                    return Integer.compare(t2.getMatchesWon(), t1.getMatchesWon());
                }

                // 3. NRR or Goal Difference (higher is better)
                if (isCricket) {
                    if (t1.getNetRunRate() != t2.getNetRunRate()) {
                        return Float.compare(t2.getNetRunRate(), t1.getNetRunRate());
                    }
                } else {
                    int gd1 = t1.getGoalsFor() - t1.getGoalsAgainst();
                    int gd2 = t2.getGoalsFor() - t2.getGoalsAgainst();
                    if (gd1 != gd2) {
                        return Integer.compare(gd2, gd1);
                    }
                }

                // 4. Goals/Runs For (higher is better)
                if (t1.getGoalsFor() != t2.getGoalsFor()) {
                    return Integer.compare(t2.getGoalsFor(), t1.getGoalsFor());
                }

                // 5. Team Name (alphabetical)
                String name1 = t1.getTeamName() != null ? t1.getTeamName() : "";
                String name2 = t2.getTeamName() != null ? t2.getTeamName() : "";
                return name1.compareToIgnoreCase(name2);
            }
        });

        return sortedList;
    }

    private void onTeamClick(TournamentTeam team) {
        // Show team details dialog
        showTeamDetailsDialog(team);
    }

    private void showTeamDetailsDialog(TournamentTeam team) {
        if (team == null) return;

        // Build detailed stats message
        StringBuilder message = new StringBuilder();
        message.append("Matches Played: ").append(team.getMatchesPlayed()).append("\n");
        message.append("Matches Won: ").append(team.getMatchesWon()).append("\n");
        message.append("Matches Lost: ").append(team.getMatchesLost()).append("\n");
        message.append("Matches Drawn: ").append(team.getMatchesDrawn()).append("\n");
        message.append("Points: ").append(team.getPoints()).append("\n\n");

        if (isCricket) {
            message.append("Net Run Rate: ").append(String.format("%.3f", team.getNetRunRate()));
        } else {
            message.append("Goals For: ").append(team.getGoalsFor()).append("\n");
            message.append("Goals Against: ").append(team.getGoalsAgainst()).append("\n");
            int gd = team.getGoalsFor() - team.getGoalsAgainst();
            message.append("Goal Difference: ").append(gd > 0 ? "+" : "").append(gd);
        }

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(team.getTeamName() != null ? team.getTeamName() : "Team Details")
                .setMessage(message.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void showEmptyState(String message) {
        rvStandings.setVisibility(View.GONE);
        if (cardLegend != null) {
            cardLegend.setVisibility(View.GONE);
        }
        layoutEmptyState.setVisibility(View.VISIBLE);
        if (tvEmptyMessage != null) {
            tvEmptyMessage.setText(message);
        }
    }

    private void showStandingsTable() {
        rvStandings.setVisibility(View.VISIBLE);
        layoutEmptyState.setVisibility(View.GONE);
        updateLegendVisibility();
    }

    private void updateLegendVisibility() {
        // Show legend only if tournament is knockout or mixed
        if (cardLegend != null && currentTournament != null) {
            String type = currentTournament.getTournamentType();
            boolean showLegend = type != null &&
                    (type.equalsIgnoreCase("KNOCKOUT") || type.equalsIgnoreCase("MIXED"));
            cardLegend.setVisibility(showLegend ? View.VISIBLE : View.GONE);
        }
    }
}