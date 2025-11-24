package com.example.tournafy.ui.fragments.tournament;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tournafy.R;
import com.example.tournafy.domain.models.team.TournamentTeam;
import com.example.tournafy.domain.models.tournament.Tournament;
import com.example.tournafy.ui.viewmodels.TournamentViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Fragment displaying tournament overview information:
 * - Tournament details (format, dates, venue, teams count)
 * - Winner display (if completed)
 * - Host information
 * - Statistics summary
 * - Action buttons for tournament management
 */
@AndroidEntryPoint
public class TournamentOverviewFragment extends Fragment {

    private static final String ARG_TOURNAMENT_ID = "tournament_id";
    private static final String ARG_IS_ONLINE = "is_online";

    private TournamentViewModel tournamentViewModel;
    
    // UI Components
    private TextView tvTournamentFormat;
    private TextView tvStartDate;
    private TextView tvEndDate;
    private TextView tvVenue;
    private TextView tvWinner;
    private TextView tvTeamsCount;
    private TextView tvMatchesPlayed;
    private TextView tvMatchesTotal;
    private TextView tvHostName;
    private TextView tvCoHostsCount;
    private MaterialCardView cardWinner;
    private MaterialCardView cardStats;
    private MaterialButton btnStartTournament;
    private MaterialButton btnGenerateBrackets;
    private MaterialButton btnCompleteTournament;
    private LinearProgressIndicator progressMatches;
    private LinearLayout layoutCoHosts;
    private RecyclerView rvCoHosts;

    private String tournamentId;
    private boolean isOnline;
    private Tournament currentTournament;
    private List<TournamentTeam> tournamentTeams = new ArrayList<>();

    public TournamentOverviewFragment() {
        // Required empty public constructor
    }

    public static TournamentOverviewFragment newInstance(String tournamentId, boolean isOnline) {
        TournamentOverviewFragment fragment = new TournamentOverviewFragment();
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
        return inflater.inflate(R.layout.fragment_tournament_overview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        tournamentViewModel = new ViewModelProvider(requireActivity()).get(TournamentViewModel.class);
        
        initViews(view);
        setupListeners();
        observeTournament();
        observeTeams();
    }

    private void initViews(View view) {
        tvTournamentFormat = view.findViewById(R.id.tvTournamentFormat);
        tvStartDate = view.findViewById(R.id.tvStartDate);
        tvVenue = view.findViewById(R.id.tvVenue);
        tvWinner = view.findViewById(R.id.tvWinner);
        cardWinner = view.findViewById(R.id.cardWinner);
        btnStartTournament = view.findViewById(R.id.btnStartTournament);
        btnGenerateBrackets = view.findViewById(R.id.btnGenerateBrackets);
        btnCompleteTournament = view.findViewById(R.id.btnCompleteTournament);
        
        // Find additional views (will be added to layout)
        tvTeamsCount = view.findViewById(R.id.tvTeamsCount);
        tvMatchesPlayed = view.findViewById(R.id.tvMatchesPlayed);
        tvMatchesTotal = view.findViewById(R.id.tvMatchesTotal);
        tvEndDate = view.findViewById(R.id.tvEndDate);
        tvHostName = view.findViewById(R.id.tvHostName);
        tvCoHostsCount = view.findViewById(R.id.tvCoHostsCount);
        cardStats = view.findViewById(R.id.cardStats);
        progressMatches = view.findViewById(R.id.progressMatches);
        layoutCoHosts = view.findViewById(R.id.layoutCoHosts);
        rvCoHosts = view.findViewById(R.id.rvCoHosts);
    }

    private void setupListeners() {
        if (btnStartTournament != null) {
            btnStartTournament.setOnClickListener(v -> startTournament());
        }
        if (btnGenerateBrackets != null) {
            btnGenerateBrackets.setOnClickListener(v -> showBracketGenerationDialog());
        }
        if (btnCompleteTournament != null) {
            btnCompleteTournament.setOnClickListener(v -> completeTournament());
        }
    }

    private void observeTournament() {
        if (isOnline) {
            tournamentViewModel.onlineTournament.observe(getViewLifecycleOwner(), this::updateUI);
        } else {
            tournamentViewModel.offlineTournament.observe(getViewLifecycleOwner(), this::updateUI);
        }
        
        // Observe error messages
        tournamentViewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                tournamentViewModel.clearErrorMessage();
            }
        });
        
        // Observe loading state
        tournamentViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            // You can show/hide a progress indicator here if needed
            if (btnGenerateBrackets != null) {
                btnGenerateBrackets.setEnabled(!isLoading && tournamentTeams.size() >= 2);
            }
            if (btnStartTournament != null) {
                btnStartTournament.setEnabled(!isLoading && tournamentTeams.size() >= 2);
            }
        });
    }
    
    private void observeTeams() {
        tournamentViewModel.tournamentTeams.observe(getViewLifecycleOwner(), teams -> {
            if (teams != null) {
                tournamentTeams = teams;
                updateTeamsCount();
                updateStatistics();
                // Update button states when teams load
                if (currentTournament != null) {
                    updateActionButtons(currentTournament.getStatus());
                }
            }
        });
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Refresh button states when fragment becomes visible
        if (currentTournament != null) {
            updateActionButtons(currentTournament.getStatus());
        }
    }

    private void updateUI(Tournament tournament) {
        if (tournament == null) return;
        
        currentTournament = tournament;

        // Tournament format
        tvTournamentFormat.setText(formatTournamentType(tournament.getTournamentType()));
        
        // Dates
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        if (tournament.getStartDate() != null) {
            tvStartDate.setText(dateFormat.format(tournament.getStartDate()));
        } else {
            tvStartDate.setText("Not scheduled");
        }
        
        // End date (if available in config)
        if (tvEndDate != null) {
            if (tournament.getTournamentConfig() != null && 
                tournament.getTournamentConfig().containsKey("endDate")) {
                try {
                    Object endDateObj = tournament.getTournamentConfig().get("endDate");
                    if (endDateObj instanceof java.util.Date) {
                        tvEndDate.setText(dateFormat.format((java.util.Date) endDateObj));
                    } else if (endDateObj instanceof Long) {
                        tvEndDate.setText(dateFormat.format(new java.util.Date((Long) endDateObj)));
                    } else {
                        tvEndDate.setText("TBA");
                    }
                } catch (Exception e) {
                    tvEndDate.setText("TBA");
                }
            } else {
                tvEndDate.setText("TBA");
            }
        }

        // Venue
        if (tournament.getTournamentConfig() != null && 
            tournament.getTournamentConfig().containsKey("venue")) {
            tvVenue.setText(tournament.getTournamentConfig().get("venue").toString());
        } else {
            tvVenue.setText("TBA");
        }
        
        // Host information
        if (tvHostName != null) {
            String hostId = tournament.getHostUserId();
            tvHostName.setText(hostId != null ? hostId : "Unknown Host");
        }
        
        // Co-hosts
        if (tvCoHostsCount != null && tournament.getTournamentConfig() != null) {
            Object coHostsObj = tournament.getTournamentConfig().get("coHosts");
            if (coHostsObj instanceof List) {
                int coHostsCount = ((List<?>) coHostsObj).size();
                tvCoHostsCount.setText(coHostsCount + " Co-host" + (coHostsCount != 1 ? "s" : ""));
                if (layoutCoHosts != null) {
                    layoutCoHosts.setVisibility(coHostsCount > 0 ? View.VISIBLE : View.GONE);
                }
            } else {
                tvCoHostsCount.setText("No co-hosts");
                if (layoutCoHosts != null) {
                    layoutCoHosts.setVisibility(View.GONE);
                }
            }
        }

        // Winner display
        if (tournament.getWinnerTeamId() != null && !tournament.getWinnerTeamId().isEmpty()) {
            cardWinner.setVisibility(View.VISIBLE);
            // Find winner team name from teams list
            String winnerName = findTeamName(tournament.getWinnerTeamId());
            tvWinner.setText(winnerName != null ? winnerName : tournament.getWinnerTeamId());
        } else {
            cardWinner.setVisibility(View.GONE);
        }

        // Update action buttons
        updateActionButtons(tournament.getStatus());
    }
    
    private void updateTeamsCount() {
        if (tvTeamsCount != null) {
            tvTeamsCount.setText(String.valueOf(tournamentTeams.size()));
        }
    }
    
    private void updateStatistics() {
        if (cardStats == null) return;
        
        // Calculate total matches played
        int totalMatchesPlayed = 0;
        for (TournamentTeam team : tournamentTeams) {
            // Each match is counted twice (once per team), so we divide by 2
            totalMatchesPlayed = Math.max(totalMatchesPlayed, team.getMatchesPlayed());
        }
        
        if (tvMatchesPlayed != null) {
            tvMatchesPlayed.setText(String.valueOf(totalMatchesPlayed));
        }
        
        // Calculate total matches based on tournament type
        int totalMatches = calculateTotalMatches();
        if (tvMatchesTotal != null) {
            tvMatchesTotal.setText(String.valueOf(totalMatches));
        }
        
        // Update progress bar
        if (progressMatches != null && totalMatches > 0) {
            int progress = (int) (((float) totalMatchesPlayed / totalMatches) * 100);
            progressMatches.setProgress(progress);
        }
    }
    
    private int calculateTotalMatches() {
        if (currentTournament == null) return 0;
        
        int teamsCount = tournamentTeams.size();
        if (teamsCount < 2) return 0;
        
        String tournamentType = currentTournament.getTournamentType();
        if (tournamentType == null) return 0;
        
        switch (tournamentType.toUpperCase()) {
            case "KNOCKOUT":
                // Knockout: n-1 matches for n teams
                return teamsCount - 1;
                
            case "LEAGUE":
            case "ROUND ROBIN":
                // Round robin: each team plays every other team once
                // Formula: n(n-1)/2
                return (teamsCount * (teamsCount - 1)) / 2;
                
            case "MIXED":
                // Group stage + knockout (estimate based on config)
                if (currentTournament.getTournamentConfig() != null) {
                    Object groupsObj = currentTournament.getTournamentConfig().get("numberOfGroups");
                    int groups = 2; // default
                    if (groupsObj instanceof Number) {
                        groups = ((Number) groupsObj).intValue();
                    }
                    
                    int teamsPerGroup = teamsCount / groups;
                    // Group stage matches per group
                    int groupMatches = groups * ((teamsPerGroup * (teamsPerGroup - 1)) / 2);
                    // Knockout matches (top 2 from each group)
                    int knockoutTeams = groups * 2;
                    int knockoutMatches = knockoutTeams - 1;
                    
                    return groupMatches + knockoutMatches;
                }
                return teamsCount; // fallback estimate
                
            default:
                return 0;
        }
    }
    
    private String findTeamName(String teamId) {
        for (TournamentTeam team : tournamentTeams) {
            if (team.getTeamId() != null && team.getTeamId().equals(teamId)) {
                return team.getTeamName();
            }
            if (team.getTournamentTeamId() != null && team.getTournamentTeamId().equals(teamId)) {
                return team.getTeamName();
            }
        }
        return null;
    }

    private void updateActionButtons(String status) {
        if (status == null) status = "DRAFT";
        
        // Null checks for button references
        if (btnStartTournament == null || btnGenerateBrackets == null || btnCompleteTournament == null) {
            return;
        }

        switch (status.toUpperCase()) {
            case "DRAFT":
            case "SCHEDULED":
                btnStartTournament.setVisibility(View.VISIBLE);
                btnStartTournament.setEnabled(tournamentTeams.size() >= 2);
                btnGenerateBrackets.setVisibility(View.VISIBLE);
                btnGenerateBrackets.setEnabled(tournamentTeams.size() >= 2);
                btnCompleteTournament.setVisibility(View.GONE);
                break;
            
            case "IN_PROGRESS":
            case "ACTIVE":
            case "LIVE":
                btnStartTournament.setVisibility(View.GONE);
                btnGenerateBrackets.setVisibility(View.GONE);
                btnCompleteTournament.setVisibility(View.VISIBLE);
                btnCompleteTournament.setEnabled(true);
                break;
            
            case "COMPLETED":
                btnStartTournament.setVisibility(View.GONE);
                btnGenerateBrackets.setVisibility(View.GONE);
                btnCompleteTournament.setVisibility(View.GONE);
                break;
            
            default:
                btnStartTournament.setVisibility(View.VISIBLE);
                btnStartTournament.setEnabled(tournamentTeams.size() >= 2);
                btnGenerateBrackets.setVisibility(View.VISIBLE);
                btnGenerateBrackets.setEnabled(tournamentTeams.size() >= 2);
                btnCompleteTournament.setVisibility(View.GONE);
        }
    }

    private String formatTournamentType(String type) {
        if (type == null) return "Unknown";
        
        switch (type.toUpperCase()) {
            case "KNOCKOUT":
                return "Knockout Tournament";
            case "LEAGUE":
            case "ROUND ROBIN":
                return "League / Round Robin";
            case "MIXED":
                return "Group Stage + Knockout";
            default:
                return type;
        }
    }

    private void startTournament() {
        if (currentTournament == null) {
            Toast.makeText(getContext(), "Tournament data not loaded", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (tournamentTeams.size() < 2) {
            Toast.makeText(getContext(), "At least 2 teams are required to start the tournament", Toast.LENGTH_SHORT).show();
            return;
        }

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Start Tournament")
            .setMessage("Are you sure you want to start this tournament? This will mark it as active and you can begin scheduling matches.")
            .setPositiveButton("Start", (dialog, which) -> {
                tournamentViewModel.startTournament(tournamentId, isOnline);
                Toast.makeText(getContext(), "Tournament started!", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showBracketGenerationDialog() {
        if (currentTournament == null) {
            Toast.makeText(getContext(), "Tournament data not loaded", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (tournamentTeams.size() < 2) {
            Toast.makeText(getContext(), "At least 2 teams are required to generate brackets", Toast.LENGTH_SHORT).show();
            return;
        }

        final String[] strategies = {"Random Pairing", "Seeded (by standings)"};
        
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Generate Knockout Brackets")
            // Remove setMessage - it can interfere with setItems
            .setItems(strategies, (dialog, which) -> {
                String strategyName = null;
                switch (which) {
                    case 0:
                        strategyName = "RANDOM";
                        break;
                    case 1:
                        strategyName = "SEEDED";
                        break;
                }
                
                if (strategyName != null) {
                    generateBrackets(strategyName);
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void generateBrackets(String strategyType) {
        Toast.makeText(getContext(), 
            "Generating " + strategyType.toLowerCase() + " brackets for " + tournamentTeams.size() + " teams...", 
            Toast.LENGTH_SHORT).show();
        
        // Create strategy instance based on type
        com.example.tournafy.service.strategies.tournament.IBracketGenerationStrategy strategy;
        if ("RANDOM".equals(strategyType)) {
            strategy = new com.example.tournafy.service.strategies.tournament.RandomBracketStrategy();
        } else if ("SEEDED".equals(strategyType)) {
            strategy = new com.example.tournafy.service.strategies.tournament.SeededBracketStrategy();
        } else {
            Toast.makeText(getContext(), "Unknown strategy type", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Call the ViewModel method that actually generates brackets
        tournamentViewModel.onGenerateBracketsClicked(strategy);
    }

    private void completeTournament() {
        if (currentTournament == null) {
            Toast.makeText(getContext(), "Tournament data not loaded", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (tournamentTeams.isEmpty()) {
            Toast.makeText(getContext(), "No teams found", Toast.LENGTH_SHORT).show();
            return;
        }

        showWinnerSelectionDialog();
    }
    
    private void showWinnerSelectionDialog() {
        // Build team names array
        final String[] teamNames = new String[tournamentTeams.size()];
        final String[] teamIds = new String[tournamentTeams.size()];
        
        for (int i = 0; i < tournamentTeams.size(); i++) {
            TournamentTeam team = tournamentTeams.get(i);
            teamNames[i] = team.getTeamName() != null ? team.getTeamName() : "Team " + (i + 1);
            teamIds[i] = team.getTeamId();
        }
        
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Select Tournament Winner")
            .setItems(teamNames, (dialog, which) -> {
                confirmCompleteTournament(teamIds[which], teamNames[which]);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void confirmCompleteTournament(String winnerTeamId, String winnerName) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Complete Tournament")
            .setMessage("Declare " + winnerName + " as the winner and complete this tournament?\n\nThis action cannot be undone.")
            .setPositiveButton("Complete", (dialog, which) -> {
                tournamentViewModel.completeTournament(tournamentId, winnerTeamId, isOnline);
                Toast.makeText(getContext(), "Tournament completed! Winner: " + winnerName, Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
