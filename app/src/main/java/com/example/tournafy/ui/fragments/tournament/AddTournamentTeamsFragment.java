package com.example.tournafy.ui.fragments.tournament;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tournafy.R;
import com.example.tournafy.data.repository.offline.TeamFirestoreRepository;
import com.example.tournafy.data.repository.offline.TournamentTeamFirestoreRepository;
import com.example.tournafy.domain.models.team.Team;
import com.example.tournafy.domain.models.team.TournamentTeam;
import com.example.tournafy.ui.viewmodels.TournamentViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Fragment for adding teams to a newly created tournament.
 * Allows user to:
 * - Add existing teams from their team list
 * - Create new teams on the fly
 * - Set minimum teams required
 * - Navigate to tournament details once teams are added
 */
@AndroidEntryPoint
public class AddTournamentTeamsFragment extends Fragment {

    private static final String ARG_TOURNAMENT_ID = "tournament_id";
    private static final String ARG_IS_ONLINE = "is_online";

    @Inject
    TeamFirestoreRepository teamRepository;
    
    @Inject
    TournamentTeamFirestoreRepository tournamentTeamRepository;

    private TournamentViewModel tournamentViewModel;
    
    // UI Components
    private RecyclerView rvTeams;
    private ExtendedFloatingActionButton fabQuickAdd;
    private MaterialButton btnContinue;
    private MaterialButton btnBack;
    private MaterialButton btnAddTeam;
    private TextView tvTeamsAdded;
    private TextView tvRequiredTeams;
    private View layoutEmptyState;

    private String tournamentId;
    private boolean isOnline;
    private String currentUserId = "user123"; // TODO: Get from auth
    private int minTeamsRequired = 2;
    private int playersPerTeam = 11; // Default, will be updated from tournament config

    public AddTournamentTeamsFragment() {
        // Required empty public constructor
    }

    public static AddTournamentTeamsFragment newInstance(String tournamentId, boolean isOnline) {
        AddTournamentTeamsFragment fragment = new AddTournamentTeamsFragment();
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
        return inflater.inflate(R.layout.fragment_add_tournament_teams, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        tournamentViewModel = new ViewModelProvider(requireActivity()).get(TournamentViewModel.class);
        
        initViews(view);
        setupListeners();
        loadTeams();
    }

    private void initViews(View view) {
        // Find views
        rvTeams = view.findViewById(R.id.rvTeams);
        fabQuickAdd = view.findViewById(R.id.fabQuickAdd);
        btnContinue = view.findViewById(R.id.btnNext);
        btnBack = view.findViewById(R.id.btnBack);
        btnAddTeam = view.findViewById(R.id.btnAddTeam);
        tvTeamsAdded = view.findViewById(R.id.tvTeamsAdded);
        tvRequiredTeams = view.findViewById(R.id.tvRequiredTeams);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        
        // Set up RecyclerView
        rvTeams.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Initially disable continue button
        btnContinue.setEnabled(false);
        
        // Update UI
        updateTeamCountUI();
    }

    private List<Team> availableTeams = new ArrayList<>();
    private List<Team> selectedTeams = new ArrayList<>();
    
    private com.example.tournafy.ui.adapters.tournament.SelectedTeamsAdapter selectedTeamsAdapter;

    private void setupListeners() {
        // Remove FAB - not needed anymore
        fabQuickAdd.setVisibility(View.GONE);
        
        // Add team button now creates new team directly
        btnAddTeam.setOnClickListener(v -> showCreateTeamDialog());
        
        btnContinue.setOnClickListener(v -> {
            android.util.Log.d("AddTournamentTeams", "Continue button clicked");
            
            if (selectedTeams.size() < minTeamsRequired) {
                android.util.Log.d("AddTournamentTeams", "Not enough teams: " + selectedTeams.size() + " < " + minTeamsRequired);
                Toast.makeText(getContext(), 
                    "Please add at least " + minTeamsRequired + " teams", 
                    Toast.LENGTH_SHORT).show();
                return;
            }
            
            android.util.Log.d("AddTournamentTeams", "Checking player counts...");
            
            // Check if all teams have required number of players
            boolean allTeamsHavePlayers = true;
            for (Team team : selectedTeams) {
                if (team.getPlayers() == null || team.getPlayers().size() < playersPerTeam) {
                    allTeamsHavePlayers = false;
                    break;
                }
            }
            
            if (!allTeamsHavePlayers) {
                android.util.Log.d("AddTournamentTeams", "Some teams missing players, showing dialog");
                new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Incomplete Teams")
                    .setMessage("Some teams don't have the required " + playersPerTeam + " players. Do you want to continue anyway?")
                    .setPositiveButton("Continue", (dialog, which) -> saveTeamsToTournament())
                    .setNegativeButton("Add Players", null)
                    .show();
            } else {
                android.util.Log.d("AddTournamentTeams", "All teams have players, saving...");
                saveTeamsToTournament();
            }
        });
        
        btnBack.setOnClickListener(v -> {
            requireActivity().onBackPressed();
        });
    }

    private void loadTeams() {
        // Setup selected teams adapter with click and remove handlers
        selectedTeamsAdapter = new com.example.tournafy.ui.adapters.tournament.SelectedTeamsAdapter(
            new com.example.tournafy.ui.adapters.tournament.SelectedTeamsAdapter.OnTeamInteractionListener() {
                @Override
                public void onTeamClick(Team team) {
                    // Open dialog to manage players for this team
                    showManagePlayersDialog(team);
                }

                @Override
                public void onTeamRemove(Team team) {
                    // Show confirmation dialog before removing
                    new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Remove Team")
                        .setMessage("Are you sure you want to remove " + team.getTeamName() + " from the tournament?")
                        .setPositiveButton("Remove", (dialog, which) -> {
                            selectedTeams.remove(team);
                            // Update adapter with new list for DiffUtil to detect changes
                            updateAdapters();
                            Toast.makeText(getContext(), team.getTeamName() + " removed", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                }
            });
        
        rvTeams.setAdapter(selectedTeamsAdapter);
        
        // Load teams from repository
        teamRepository.getAll().observe(getViewLifecycleOwner(), teams -> {
            if (teams != null) {
                availableTeams.clear();
                availableTeams.addAll(teams);
            }
        });
        
        // Load tournament to get configuration
        if (isOnline) {
            tournamentViewModel.loadOnlineTournament(tournamentId);
            tournamentViewModel.onlineTournament.observe(getViewLifecycleOwner(), tournament -> {
                if (tournament != null && tournament.getTournamentConfig() != null) {
                    updateTournamentConfig(tournament.getTournamentConfig());
                }
            });
        } else {
            tournamentViewModel.loadOfflineTournament(tournamentId);
            tournamentViewModel.offlineTournament.observe(getViewLifecycleOwner(), tournament -> {
                if (tournament != null && tournament.getTournamentConfig() != null) {
                    updateTournamentConfig(tournament.getTournamentConfig());
                }
            });
        }
        
        updateTeamCountUI();
    }
    
    private void updateTournamentConfig(Map<String, Object> config) {
        if (config.containsKey("playersPerTeam")) {
            Object value = config.get("playersPerTeam");
            if (value instanceof Number) {
                playersPerTeam = ((Number) value).intValue();
            }
        }
        if (config.containsKey("minTeams")) {
            Object value = config.get("minTeams");
            if (value instanceof Number) {
                minTeamsRequired = ((Number) value).intValue();
            }
        }
        updateTeamCountUI();
    }
    
    private void updateAdapters() {
        // Update selected teams adapter
        if (selectedTeamsAdapter != null) {
            selectedTeamsAdapter.submitList(new ArrayList<>(selectedTeams));
        }
        
        // Update UI
        updateTeamCountUI();
    }
    
    private void updateTeamCountUI() {
        // Update teams added text
        if (tvTeamsAdded != null) {
            int count = selectedTeams.size();
            tvTeamsAdded.setText(count + " team" + (count == 1 ? "" : "s") + " added");
        }
        
        // Update required teams text
        if (tvRequiredTeams != null) {
            tvRequiredTeams.setText("Add at least " + minTeamsRequired + " teams to continue");
        }
        
        // Update button states
        if (btnContinue != null) {
            boolean canContinue = selectedTeams.size() >= minTeamsRequired;
            btnContinue.setEnabled(canContinue);
            
            if (canContinue) {
                btnContinue.setText("Continue (" + selectedTeams.size() + " teams)");
            } else {
                btnContinue.setText("Continue (need " + (minTeamsRequired - selectedTeams.size()) + " more)");
            }
        }
        
        // Show/hide empty state
        if (layoutEmptyState != null && rvTeams != null) {
            if (selectedTeams.isEmpty()) {
                layoutEmptyState.setVisibility(View.VISIBLE);
                rvTeams.setVisibility(View.GONE);
            } else {
                layoutEmptyState.setVisibility(View.GONE);
                rvTeams.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showSelectTeamDialog() {
        if (availableTeams.isEmpty()) {
            Toast.makeText(getContext(), "No teams available. Create one first!", Toast.LENGTH_SHORT).show();
            showCreateTeamDialog();
            return;
        }
        
        // Filter out already selected teams
        List<Team> unselectedTeams = new ArrayList<>();
        for (Team team : availableTeams) {
            boolean isSelected = false;
            for (Team selectedTeam : selectedTeams) {
                if (selectedTeam.getTeamId().equals(team.getTeamId())) {
                    isSelected = true;
                    break;
                }
            }
            if (!isSelected) {
                unselectedTeams.add(team);
            }
        }
        
        if (unselectedTeams.isEmpty()) {
            Toast.makeText(getContext(), "All teams already added!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Create list of team names with player count
        String[] teamNames = new String[unselectedTeams.size()];
        for (int i = 0; i < unselectedTeams.size(); i++) {
            Team team = unselectedTeams.get(i);
            int playerCount = team.getPlayers() != null ? team.getPlayers().size() : 0;
            teamNames[i] = team.getTeamName() + " (" + playerCount + "/" + playersPerTeam + " players)";
        }
        
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Team")
            .setItems(teamNames, (dialog, which) -> {
                Team selectedTeam = unselectedTeams.get(which);
                addTeamToTournament(selectedTeam);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void addTeamToTournament(Team team) {
        selectedTeams.add(team);
        Toast.makeText(getContext(), "Added " + team.getTeamName(), Toast.LENGTH_SHORT).show();
        updateAdapters();
        
        // Offer to add players if team doesn't have enough
        int playerCount = team.getPlayers() != null ? team.getPlayers().size() : 0;
        if (playerCount < playersPerTeam) {
            new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Add Players?")
                .setMessage(team.getTeamName() + " needs " + (playersPerTeam - playerCount) + 
                           " more players. Would you like to add them now?")
                .setPositiveButton("Add Players", (dialog, which) -> showManagePlayersDialog(team))
                .setNegativeButton("Later", null)
                .show();
        }
    }
    
    private void showCreateTeamDialog() {
        // Create input field
        final EditText input = new EditText(getContext());
        input.setHint("Team Name");
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        
        // Add padding to input
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding, padding, padding);
        
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Create New Team")
            .setMessage("Enter the name for your new team:")
            .setView(input)
            .setPositiveButton("Create", (dialog, which) -> {
                String teamName = input.getText().toString().trim();
                if (!teamName.isEmpty()) {
                    createNewTeam(teamName);
                } else {
                    Toast.makeText(getContext(), "Please enter a team name", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void showManagePlayersDialog(Team team) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_manage_players, null);
        
        RecyclerView rvPlayers = dialogView.findViewById(R.id.rvPlayers);
        MaterialButton btnAddPlayer = dialogView.findViewById(R.id.btnAddPlayer);
        TextView tvPlayerCount = dialogView.findViewById(R.id.tvPlayerCount);
        
        // Setup RecyclerView
        rvPlayers.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Get or initialize players list
        List<com.example.tournafy.domain.models.team.Player> players = team.getPlayers();
        if (players == null) {
            players = new ArrayList<>();
            team.setPlayers(players);
        }
        final List<com.example.tournafy.domain.models.team.Player> finalPlayers = players;
        
        // Create adapter with remove callback
        final com.example.tournafy.ui.adapters.tournament.PlayerAdapter[] adapterHolder = 
            new com.example.tournafy.ui.adapters.tournament.PlayerAdapter[1];
        
        adapterHolder[0] = new com.example.tournafy.ui.adapters.tournament.PlayerAdapter(player -> {
            // Remove player
            new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Remove Player")
                .setMessage("Remove " + player.getPlayerName() + " from " + team.getTeamName() + "?")
                .setPositiveButton("Remove", (d, w) -> {
                    finalPlayers.remove(player);
                    // Create new list for DiffUtil to detect changes
                    List<com.example.tournafy.domain.models.team.Player> updatedList = new ArrayList<>(finalPlayers);
                    adapterHolder[0].submitList(updatedList);
                    updatePlayerCountText(tvPlayerCount, finalPlayers.size());
                    saveTeamToRepository(team);
                    updateTeamInSelectedList(team);
                })
                .setNegativeButton("Cancel", null)
                .show();
        });
        
        final com.example.tournafy.ui.adapters.tournament.PlayerAdapter playerAdapter = adapterHolder[0];
        
        rvPlayers.setAdapter(playerAdapter);
        playerAdapter.submitList(new ArrayList<>(players));
        
        // Update player count
        updatePlayerCountText(tvPlayerCount, players.size());
        
        // Add player button
        btnAddPlayer.setOnClickListener(v -> {
            showAddPlayerDialog(team, playerAdapter, tvPlayerCount);
        });
        
        // Show dialog
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Manage Players - " + team.getTeamName())
            .setView(dialogView)
            .setPositiveButton("Done", (d, w) -> {
                saveTeamToRepository(team);
                updateAdapters(); // Refresh the team list
            })
            .setNegativeButton("Cancel", null)
            .create();
        
        dialog.show();
    }
    
    private void showAddPlayerDialog(Team team, 
                                     com.example.tournafy.ui.adapters.tournament.PlayerAdapter adapter,
                                     TextView tvPlayerCount) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_player, null);
        
        com.google.android.material.textfield.TextInputEditText etPlayerName = 
            dialogView.findViewById(R.id.etPlayerName);
        com.google.android.material.chip.ChipGroup chipGroupRole = 
            dialogView.findViewById(R.id.chipGroupRole);
        com.google.android.material.materialswitch.MaterialSwitch switchStartingXI = 
            dialogView.findViewById(R.id.switchStartingXI);
        com.google.android.material.button.MaterialButton btnAdd = 
            dialogView.findViewById(R.id.btnAdd);
        com.google.android.material.button.MaterialButton btnCancel = 
            dialogView.findViewById(R.id.btnCancel);
        
        // Pre-select first role (Batsman)
        chipGroupRole.check(R.id.chipBatsman);
        
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create();
        
        btnAdd.setOnClickListener(v -> {
            String playerName = etPlayerName.getText().toString().trim();
            
            if (playerName.isEmpty()) {
                etPlayerName.setError("Please enter player name");
                etPlayerName.requestFocus();
                return;
            }
            
            // Get selected role from ChipGroup
            int selectedChipId = chipGroupRole.getCheckedChipId();
            String role = "Player"; // default
            if (selectedChipId == R.id.chipBatsman) {
                role = "Batsman";
            } else if (selectedChipId == R.id.chipBowler) {
                role = "Bowler";
            } else if (selectedChipId == R.id.chipAllRounder) {
                role = "All-Rounder";
            } else if (selectedChipId == R.id.chipWicketKeeper) {
                role = "Wicket Keeper";
            }
            
            boolean isStartingXI = switchStartingXI.isChecked();
            
            // Generate jersey number (next available number)
            int jerseyNumber = team.getPlayers() != null ? team.getPlayers().size() + 1 : 1;
            
            // Create new player
            com.example.tournafy.domain.models.team.Player player = 
                new com.example.tournafy.domain.models.team.Player();
            player.setPlayerId(UUID.randomUUID().toString());
            player.setTeamId(team.getTeamId());
            player.setPlayerName(playerName);
            player.setJerseyNumber(jerseyNumber);
            player.setRole(role);
            player.setStartingXI(isStartingXI);
            
            // Add to team
            if (team.getPlayers() == null) {
                team.setPlayers(new ArrayList<>());
            }
            team.getPlayers().add(player);
            
            // Update adapter - create new list for DiffUtil to detect changes
            List<com.example.tournafy.domain.models.team.Player> updatedList = new ArrayList<>(team.getPlayers());
            adapter.submitList(updatedList);
            updatePlayerCountText(tvPlayerCount, team.getPlayers().size());
            
            // Update the team in selected teams list to trigger UI refresh
            updateTeamInSelectedList(team);
            
            Toast.makeText(getContext(), "Added " + playerName, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }
    
    private void updatePlayerCountText(TextView tvPlayerCount, int count) {
        if (tvPlayerCount != null) {
            tvPlayerCount.setText(count + " / " + playersPerTeam + " players");
            if (count < playersPerTeam) {
                tvPlayerCount.setTextColor(getResources().getColor(android.R.color.holo_orange_dark, null));
            } else {
                tvPlayerCount.setTextColor(getResources().getColor(android.R.color.holo_green_dark, null));
            }
        }
    }
    
    private void saveTeamToRepository(Team team) {
        teamRepository.update(team)
            .addOnSuccessListener(aVoid -> {
                // Success - team saved
                updateTeamInSelectedList(team);
            })
            .addOnFailureListener(e -> {
                Toast.makeText(getContext(), 
                    "Failed to save team: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }
    
    private void updateTeamInSelectedList(Team updatedTeam) {
        // Find and update the team in selected teams list
        for (int i = 0; i < selectedTeams.size(); i++) {
            if (selectedTeams.get(i).getTeamId().equals(updatedTeam.getTeamId())) {
                selectedTeams.set(i, updatedTeam);
                break;
            }
        }
        // Refresh adapter with new list instance
        updateAdapters();
    }
    
    private void createNewTeam(String teamName) {
        // Create new team
        Team newTeam = new Team();
        newTeam.setTeamId(UUID.randomUUID().toString());
        newTeam.setTeamName(teamName);
        newTeam.setCreatedBy(currentUserId);
        newTeam.setCreatedAt(new java.util.Date());
        newTeam.setPlayers(new ArrayList<>()); // Initialize empty player list
        
        // Add directly to tournament first (optimistic update)
        selectedTeams.add(newTeam);
        updateAdapters();
        
        Toast.makeText(getContext(), 
            "Team '" + teamName + "' added!", 
            Toast.LENGTH_SHORT).show();
        
        // Save to repository in background
        teamRepository.add(newTeam)
            .addOnSuccessListener(aVoid -> {
                // Successfully saved to repository
            })
            .addOnFailureListener(e -> {
                Toast.makeText(getContext(), 
                    "Warning: Team saved locally but not synced: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
        
        // Offer to add players immediately
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Players?")
            .setMessage("Would you like to add players to " + teamName + " now?")
            .setPositiveButton("Add Players", (dialog, which) -> showManagePlayersDialog(newTeam))
            .setNegativeButton("Later", null)
            .show();
    }
    
    private void saveTeamsToTournament() {
        android.util.Log.d("AddTournamentTeams", "saveTeamsToTournament called with " + selectedTeams.size() + " teams");
        android.util.Log.d("AddTournamentTeams", "Tournament ID: " + tournamentId);
        android.util.Log.d("AddTournamentTeams", "Is Online: " + isOnline);
        android.util.Log.d("AddTournamentTeams", "Repository null? " + (tournamentTeamRepository == null));
        
        if (tournamentTeamRepository == null) {
            android.util.Log.e("AddTournamentTeams", "ERROR: tournamentTeamRepository is NULL!");
            Toast.makeText(getContext(), "Error: Repository not initialized", Toast.LENGTH_LONG).show();
            return;
        }
        
        Toast.makeText(getContext(), 
            "Adding " + selectedTeams.size() + " teams to tournament...", 
            Toast.LENGTH_SHORT).show();
        
        // Convert Team objects to TournamentTeam objects
        List<TournamentTeam> tournamentTeams = new ArrayList<>();
        for (Team team : selectedTeams) {
            TournamentTeam tournamentTeam = new TournamentTeam();
            tournamentTeam.setTournamentTeamId(UUID.randomUUID().toString());
            tournamentTeam.setTournamentId(tournamentId);
            tournamentTeam.setTeamId(team.getTeamId());
            tournamentTeam.setTeamName(team.getTeamName()); // Store team name for display
            
            // Initialize statistics
            tournamentTeam.setMatchesPlayed(0);
            tournamentTeam.setMatchesWon(0);
            tournamentTeam.setMatchesLost(0);
            tournamentTeam.setMatchesDrawn(0);
            tournamentTeam.setPoints(0);
            tournamentTeam.setGoalsFor(0);
            tournamentTeam.setGoalsAgainst(0);
            tournamentTeam.setNetRunRate(0.0f);
            
            tournamentTeams.add(tournamentTeam);
        }
        
        android.util.Log.d("AddTournamentTeams", "Converted teams to TournamentTeams, calling repository.addTeams()");
        
        // Batch add all teams to tournament
        com.google.android.gms.tasks.Task<Void> task = tournamentTeamRepository.addTeams(tournamentId, tournamentTeams);
        android.util.Log.d("AddTournamentTeams", "Task object created: " + (task != null));
        
        // Set up completion monitoring
        final boolean[] navigationDone = {false};
        
        task.addOnSuccessListener(aVoid -> {
                android.util.Log.d("AddTournamentTeams", "SUCCESS callback triggered!");
                if (!navigationDone[0]) {
                    navigationDone[0] = true;
                    Toast.makeText(getContext(), 
                        "Successfully added " + selectedTeams.size() + " teams!", 
                        Toast.LENGTH_SHORT).show();
                    navigateToTournament();
                }
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("AddTournamentTeams", "FAILURE callback triggered: " + e.getMessage(), e);
                if (!navigationDone[0]) {
                    navigationDone[0] = true;
                    Toast.makeText(getContext(), 
                        "Failed to add teams: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
                    navigateToTournament();
                }
            })
            .addOnCompleteListener(task1 -> {
                android.util.Log.d("AddTournamentTeams", "COMPLETE callback triggered. Success: " + task1.isSuccessful());
            });
        
        // Set a timeout fallback - navigate after 2 seconds if Firestore doesn't respond
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            if (!navigationDone[0]) {
                android.util.Log.w("AddTournamentTeams", "TIMEOUT: Firestore operation took too long, forcing navigation");
                navigationDone[0] = true;
                navigateToTournament();
            }
        }, 2000);
    }

    private void navigateToTournament() {
        // Debug log
        android.util.Log.d("AddTournamentTeams", "Navigating to tournament: " + tournamentId + ", online: " + isOnline);
        
        if (tournamentId == null || tournamentId.isEmpty()) {
            Toast.makeText(getContext(), "Error: Tournament ID is missing", Toast.LENGTH_LONG).show();
            return;
        }
        
        try {
            // Launch TournamentActivity directly via Intent
            android.content.Intent intent = new android.content.Intent(requireContext(), 
                com.example.tournafy.ui.activities.TournamentActivity.class);
            intent.putExtra("tournament_id", tournamentId);
            intent.putExtra("is_online", isOnline);
            
            android.util.Log.d("AddTournamentTeams", "Starting TournamentActivity...");
            startActivity(intent);
            
            // Finish the host activity to prevent back stack issues
            if (getActivity() != null) {
                getActivity().finish();
            }
        } catch (Exception e) {
            android.util.Log.e("AddTournamentTeams", "Navigation failed", e);
            Toast.makeText(getContext(), "Navigation failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
