package com.example.tournafy.ui.fragments.host.match;

import android.os.Bundle;
import android.text.TextUtils;
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
import com.example.tournafy.domain.models.base.MatchConfig;
import com.example.tournafy.domain.models.team.Player;
import com.example.tournafy.domain.models.team.Team;
import com.example.tournafy.ui.adapters.PlayerListAdapter;
import com.example.tournafy.ui.dialogs.AddPlayerDialog;
import com.example.tournafy.ui.viewmodels.HostViewModel;
import com.example.tournafy.ui.viewmodels.MatchViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class AddMatchTeamsFragment extends Fragment implements AddPlayerDialog.AddPlayerListener {

    private static final String ARG_SPORT_TYPE = "sport_type";

    private HostViewModel hostViewModel;
    private MatchViewModel matchViewModel;

    // UI Components
    private TextInputEditText etTeamAName, etTeamBName;
    private RecyclerView rvTeamAPlayers, rvTeamBPlayers;
    private TextView tvTeamAPlayersCount, tvTeamBPlayersCount;
    private TextView tvTeamAStartingCount, tvTeamASubsCount;
    private TextView tvTeamBStartingCount, tvTeamBSubsCount;
    private MaterialButton btnAddPlayerA, btnAddPlayerB, btnNextToToss;

    // Local State
    private Team teamA;
    private Team teamB;
    private PlayerListAdapter adapterA, adapterB;
    private String sportType = "FOOTBALL"; // Default to football
    private int requiredStartingPlayers = 11; // Default, will be updated from config
    
    // Tournament match state
    private String matchId;
    private String tournamentId;
    private boolean isTournamentMatch = false;
    private com.example.tournafy.domain.models.base.Match currentMatch; // Store loaded match

    public AddMatchTeamsFragment() {
        // Required empty public constructor
    }
    
    public static AddMatchTeamsFragment newInstance(String sportType) {
        AddMatchTeamsFragment fragment = new AddMatchTeamsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SPORT_TYPE, sportType);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            sportType = getArguments().getString(ARG_SPORT_TYPE, "FOOTBALL");
            
            // Check if this is a tournament match
            matchId = getArguments().getString("match_id");
            tournamentId = getArguments().getString("tournament_id");
            isTournamentMatch = getArguments().getBoolean("is_tournament_match", false);
            
            android.util.Log.d("AddMatchTeams", "Tournament match: " + isTournamentMatch + 
                ", matchId: " + matchId + ", tournamentId: " + tournamentId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_match_teams, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        hostViewModel = new ViewModelProvider(requireActivity()).get(HostViewModel.class);
        matchViewModel = new ViewModelProvider(requireActivity()).get(MatchViewModel.class);

        // Initialize default empty teams with unique IDs
        teamA = new Team();
        teamA.setTeamId(java.util.UUID.randomUUID().toString());
        teamA.setPlayers(new ArrayList<>());
        
        teamB = new Team();
        teamB.setTeamId(java.util.UUID.randomUUID().toString());
        teamB.setPlayers(new ArrayList<>());

        initViews(view);
        setupRecyclerViews();
        setupListeners();
        observeViewModel();
        
        // Load tournament match teams if applicable
        if (isTournamentMatch && matchId != null) {
            loadTournamentMatchTeams();
        }
    }

    private void initViews(View view) {
        etTeamAName = view.findViewById(R.id.etTeamAName);
        etTeamBName = view.findViewById(R.id.etTeamBName);
        rvTeamAPlayers = view.findViewById(R.id.rvTeamAPlayers);
        rvTeamBPlayers = view.findViewById(R.id.rvTeamBPlayers);
        tvTeamAPlayersCount = view.findViewById(R.id.tvTeamAPlayersCount);
        tvTeamBPlayersCount = view.findViewById(R.id.tvTeamBPlayersCount);
        tvTeamAStartingCount = view.findViewById(R.id.tvTeamAStartingCount);
        tvTeamASubsCount = view.findViewById(R.id.tvTeamASubsCount);
        tvTeamBStartingCount = view.findViewById(R.id.tvTeamBStartingCount);
        tvTeamBSubsCount = view.findViewById(R.id.tvTeamBSubsCount);
        btnAddPlayerA = view.findViewById(R.id.btnAddPlayerA);
        btnAddPlayerB = view.findViewById(R.id.btnAddPlayerB);
        btnNextToToss = view.findViewById(R.id.btnNextToToss);
        
        // Show Next button for tournament matches
        if (isTournamentMatch && btnNextToToss != null) {
            btnNextToToss.setVisibility(View.VISIBLE);
        }
    }

    private void setupRecyclerViews() {
        // Adapter with callbacks for remove and starting XI toggle
        adapterA = new PlayerListAdapter(new PlayerListAdapter.OnPlayerActionListener() {
            @Override
            public void onRemove(Player player) {
                removePlayerFromTeam(player, true);
            }

            @Override
            public void onStartingXIChanged(Player player, boolean isStartingXI) {
                android.util.Log.d("AddMatchTeams", "Team A - Player: " + player.getPlayerName() + 
                    " Starting XI changed to: " + isStartingXI);
                player.setStartingXI(isStartingXI);
                // Refresh the adapter with a new list to trigger DiffUtil update
                adapterA.submitList(new ArrayList<>(teamA.getPlayers()));
                updateTeamCounts(true);
            }
        });
        rvTeamAPlayers.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTeamAPlayers.setAdapter(adapterA);

        adapterB = new PlayerListAdapter(new PlayerListAdapter.OnPlayerActionListener() {
            @Override
            public void onRemove(Player player) {
                removePlayerFromTeam(player, false);
            }

            @Override
            public void onStartingXIChanged(Player player, boolean isStartingXI) {
                android.util.Log.d("AddMatchTeams", "Team B - Player: " + player.getPlayerName() + 
                    " Starting XI changed to: " + isStartingXI);
                player.setStartingXI(isStartingXI);
                // Refresh the adapter with a new list to trigger DiffUtil update
                adapterB.submitList(new ArrayList<>(teamB.getPlayers()));
                updateTeamCounts(false);
            }
        });
        rvTeamBPlayers.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTeamBPlayers.setAdapter(adapterB);
    }

    private void setupListeners() {
        btnAddPlayerA.setOnClickListener(v -> showAddPlayerDialog(true));
        btnAddPlayerB.setOnClickListener(v -> showAddPlayerDialog(false));
        
        // Next button for tournament matches
        if (btnNextToToss != null) {
            btnNextToToss.setOnClickListener(v -> {
                if (isTournamentMatch) {
                    navigateToToss();
                }
            });
        }
    }
    
    private void observeViewModel() {
        // Observe players per side configuration
        hostViewModel.playersPerSide.observe(getViewLifecycleOwner(), players -> {
            if (players != null) {
                requiredStartingPlayers = players;
                android.util.Log.d("AddMatchTeams", "Required starting players updated to: " + players);
                // Update UI to show the requirement
                updateTeamCounts(true);
                updateTeamCounts(false);
            }
        });
    }

    private void showAddPlayerDialog(boolean isTeamA) {
        // This Dialog handles inputting a player name and role
        AddPlayerDialog dialog = AddPlayerDialog.newInstance(isTeamA, sportType);
        dialog.setListener(this); // Callback to this fragment
        dialog.show(getChildFragmentManager(), "AddPlayerDialog");
    }

    @Override
    public void onPlayerAdded(Player player, boolean isTeamA) {
        if (isTeamA) {
            teamA.getPlayers().add(player);
            adapterA.submitList(new ArrayList<>(teamA.getPlayers())); // Refresh list
            updateTeamCounts(true);
        } else {
            teamB.getPlayers().add(player);
            adapterB.submitList(new ArrayList<>(teamB.getPlayers())); // Refresh list
            updateTeamCounts(false);
        }
    }

    private void removePlayerFromTeam(Player player, boolean isTeamA) {
        if (isTeamA) {
            teamA.getPlayers().remove(player);
            adapterA.submitList(new ArrayList<>(teamA.getPlayers()));
            updateTeamCounts(true);
        } else {
            teamB.getPlayers().remove(player);
            adapterB.submitList(new ArrayList<>(teamB.getPlayers()));
            updateTeamCounts(false);
        }
    }

    /**
     * Updates the player count displays for a team
     */
    private void updateTeamCounts(boolean isTeamA) {
        List<Player> players = isTeamA ? teamA.getPlayers() : teamB.getPlayers();
        
        int totalCount = players.size();
        int startingCount = 0;
        int subsCount = 0;
        
        for (Player player : players) {
            if (player.isStartingXI()) {
                startingCount++;
            } else {
                subsCount++;
            }
        }
        
        if (isTeamA) {
            tvTeamAPlayersCount.setText("Players: " + totalCount);
            
            // Highlight starting count based on requirement
            String startingText = "Starting XI: " + startingCount + "/" + requiredStartingPlayers;
            tvTeamAStartingCount.setText(startingText);
            if (startingCount == requiredStartingPlayers) {
                tvTeamAStartingCount.setTextColor(getResources().getColor(com.google.android.material.R.color.design_default_color_primary, null));
            } else if (startingCount < requiredStartingPlayers) {
                tvTeamAStartingCount.setTextColor(getResources().getColor(com.google.android.material.R.color.design_default_color_error, null));
            } else {
                tvTeamAStartingCount.setTextColor(getResources().getColor(com.google.android.material.R.color.material_on_surface_emphasis_medium, null));
            }
            
            tvTeamASubsCount.setText("Subs: " + subsCount);
        } else {
            tvTeamBPlayersCount.setText("Players: " + totalCount);
            
            // Highlight starting count based on requirement
            String startingText = "Starting XI: " + startingCount + "/" + requiredStartingPlayers;
            tvTeamBStartingCount.setText(startingText);
            if (startingCount == requiredStartingPlayers) {
                tvTeamBStartingCount.setTextColor(getResources().getColor(com.google.android.material.R.color.design_default_color_primary, null));
            } else if (startingCount < requiredStartingPlayers) {
                tvTeamBStartingCount.setTextColor(getResources().getColor(com.google.android.material.R.color.design_default_color_error, null));
            } else {
                tvTeamBStartingCount.setTextColor(getResources().getColor(com.google.android.material.R.color.material_on_surface_emphasis_medium, null));
            }
            
            tvTeamBSubsCount.setText("Subs: " + subsCount);
        }
    }

    /**
     * Called by the Parent Fragment (HostNewMatchFragment) to validate input
     * before finalizing creation.
     */
    public boolean validate() {
        boolean valid = true;

        String nameA = etTeamAName.getText().toString().trim();
        String nameB = etTeamBName.getText().toString().trim();

        if (TextUtils.isEmpty(nameA)) {
            etTeamAName.setError("Team Name required");
            valid = false;
        }

        if (TextUtils.isEmpty(nameB)) {
            etTeamBName.setError("Team Name required");
            valid = false;
        }

        // Count starting XI players for each team
        int teamAStarting = countStartingPlayers(teamA.getPlayers());
        int teamBStarting = countStartingPlayers(teamB.getPlayers());

        // Validation: Must have EXACTLY the required number of starting XI players
        if (teamAStarting != requiredStartingPlayers) {
            android.widget.Toast.makeText(getContext(), 
                "Team A must have exactly " + requiredStartingPlayers + " Starting XI players (currently: " + teamAStarting + ")", 
                android.widget.Toast.LENGTH_LONG).show();
            valid = false;
        }

        if (teamBStarting != requiredStartingPlayers) {
            android.widget.Toast.makeText(getContext(), 
                "Team B must have exactly " + requiredStartingPlayers + " Starting XI players (currently: " + teamBStarting + ")", 
                android.widget.Toast.LENGTH_LONG).show();
            valid = false;
        }
        
        // Validation: Ensure teams have at least the minimum players total
        if (teamA.getPlayers().size() < requiredStartingPlayers) {
            android.widget.Toast.makeText(getContext(), 
                "Team A must have at least " + requiredStartingPlayers + " players total", 
                android.widget.Toast.LENGTH_LONG).show();
            valid = false;
        }
        
        if (teamB.getPlayers().size() < requiredStartingPlayers) {
            android.widget.Toast.makeText(getContext(), 
                "Team B must have at least " + requiredStartingPlayers + " players total", 
                android.widget.Toast.LENGTH_LONG).show();
            valid = false;
        }

        if (valid) {
            // Update the ViewModel or Builder with the final data
            teamA.setTeamName(nameA);
            teamB.setTeamName(nameB);
            
            // We might need to expose these teams to the parent/ViewModel
            // hostViewModel.setTeamA(teamA);
            // hostViewModel.setTeamB(teamB);
            // Or directly update the builder if available
        }

        return valid;
    }

    /**
     * Counts how many players are marked as starting XI
     */
    private int countStartingPlayers(List<Player> players) {
        int count = 0;
        for (Player player : players) {
            if (player.isStartingXI()) {
                count++;
            }
        }
        return count;
    }
    
    public Team getTeamA() {
        // Ensure name is synced
        teamA.setTeamName(etTeamAName.getText() != null ? etTeamAName.getText().toString() : "Team A");
        return teamA;
    }
    
    public Team getTeamB() {
        // Ensure name is synced
        teamB.setTeamName(etTeamBName.getText() != null ? etTeamBName.getText().toString() : "Team B");
        return teamB;
    }
    
    /**
     * Load teams for tournament match from existing match data
     * Uses the same approach as AddMatchDetailsFragment
     */
    private void loadTournamentMatchTeams() {
        if (matchId == null) {
            android.util.Log.w("AddMatchTeams", "Cannot load tournament teams: matchId is null");
            return;
        }
        
        android.util.Log.d("AddMatchTeams", "Loading tournament match teams for match: " + matchId);
        
        // Load the match from repository via ViewModel (same as AddMatchDetailsFragment)
        hostViewModel.loadMatchById(matchId);
        
        // Simple observe - just like AddMatchDetailsFragment does
        hostViewModel.currentMatch.observe(getViewLifecycleOwner(), match -> {
            if (match == null) {
                android.util.Log.d("AddMatchTeams", "Match is null, waiting...");
                return;
            }
            
            // Store match for later use in navigateToToss()
            currentMatch = match;
            
            android.util.Log.d("AddMatchTeams", "Match loaded: " + match.getName() + " (ID: " + match.getEntityId() + ")");
            
            // CRITICAL: Check if match config exists when loaded
            if (match instanceof com.example.tournafy.domain.models.match.cricket.CricketMatch) {
                com.example.tournafy.domain.models.base.MatchConfig config =
                    ((com.example.tournafy.domain.models.match.cricket.CricketMatch) match).getMatchConfig();
                if (config instanceof com.example.tournafy.domain.models.match.cricket.CricketMatchConfig) {
                    com.example.tournafy.domain.models.match.cricket.CricketMatchConfig cricketConfig = 
                        (com.example.tournafy.domain.models.match.cricket.CricketMatchConfig) config;
                    android.util.Log.w("AddMatchTeams", "LOADED Cricket match config: " + 
                        cricketConfig.getNumberOfOvers() + " overs, " + cricketConfig.getPlayersPerSide() + " players");
                } else {
                    android.util.Log.w("AddMatchTeams", "LOADED Cricket match config: NULL");
                }
            } else if (match instanceof com.example.tournafy.domain.models.match.football.FootballMatch) {
                com.example.tournafy.domain.models.base.MatchConfig config =
                    ((com.example.tournafy.domain.models.match.football.FootballMatch) match).getMatchConfig();
                if (config instanceof com.example.tournafy.domain.models.match.football.FootballMatchConfig) {
                    com.example.tournafy.domain.models.match.football.FootballMatchConfig footballConfig = 
                        (com.example.tournafy.domain.models.match.football.FootballMatchConfig) config;
                    android.util.Log.w("AddMatchTeams", "LOADED Football match config: " + 
                        footballConfig.getMatchDuration() + " mins, " + footballConfig.getPlayersPerSide() + " players");
                } else {
                    android.util.Log.w("AddMatchTeams", "LOADED Football match config: NULL");
                }
            }
            
            // Check if match is CricketMatch or FootballMatch and has teams
            List<com.example.tournafy.domain.models.team.MatchTeam> matchTeams = null;
            
            if (match instanceof com.example.tournafy.domain.models.match.cricket.CricketMatch) {
                matchTeams = ((com.example.tournafy.domain.models.match.cricket.CricketMatch) match).getTeams();
            } else if (match instanceof com.example.tournafy.domain.models.match.football.FootballMatch) {
                matchTeams = ((com.example.tournafy.domain.models.match.football.FootballMatch) match).getTeams();
            }
            
            if (matchTeams == null || matchTeams.size() < 2) {
                android.util.Log.w("AddMatchTeams", "Match doesn't have teams yet");
                return;
            }
            
            android.util.Log.d("AddMatchTeams", "Match has " + matchTeams.size() + " teams");
            
            // Update sport type from match
            sportType = match.getSportId();
            android.util.Log.d("AddMatchTeams", "Sport type: " + sportType);
            
            // Load Team A
            com.example.tournafy.domain.models.team.MatchTeam matchTeamA = matchTeams.get(0);
            teamA.setTeamId(matchTeamA.getTeamId());
            teamA.setTeamName(matchTeamA.getTeamName());
            
            android.util.Log.d("AddMatchTeams", "Team A: " + matchTeamA.getTeamName() + 
                " (ID: " + matchTeamA.getTeamId() + ")");
            
            // Convert MatchTeam players to Player List
            if (matchTeamA.getPlayers() != null) {
                List<Player> playersA = new ArrayList<>();
                for (com.example.tournafy.domain.models.team.Player matchPlayer : matchTeamA.getPlayers()) {
                    Player player = new Player();
                    player.setPlayerId(matchPlayer.getPlayerId());
                    player.setPlayerName(matchPlayer.getPlayerName());
                    player.setRole(matchPlayer.getRole());
                    player.setStartingXI(matchPlayer.isStartingXI());
                    playersA.add(player);
                }
                teamA.setPlayers(playersA);
                android.util.Log.d("AddMatchTeams", "Team A has " + playersA.size() + " players");
            } else {
                teamA.setPlayers(new ArrayList<>());
                android.util.Log.w("AddMatchTeams", "Team A has no players");
            }
            
            // Load Team B
            com.example.tournafy.domain.models.team.MatchTeam matchTeamB = matchTeams.get(1);
            teamB.setTeamId(matchTeamB.getTeamId());
            teamB.setTeamName(matchTeamB.getTeamName());
            
            android.util.Log.d("AddMatchTeams", "Team B: " + matchTeamB.getTeamName() + 
                " (ID: " + matchTeamB.getTeamId() + ")");
            
            // Convert MatchTeam players to Player List
            if (matchTeamB.getPlayers() != null) {
                List<Player> playersB = new ArrayList<>();
                for (com.example.tournafy.domain.models.team.Player matchPlayer : matchTeamB.getPlayers()) {
                    Player player = new Player();
                    player.setPlayerId(matchPlayer.getPlayerId());
                    player.setPlayerName(matchPlayer.getPlayerName());
                    player.setRole(matchPlayer.getRole());
                    player.setStartingXI(matchPlayer.isStartingXI());
                    playersB.add(player);
                }
                teamB.setPlayers(playersB);
                android.util.Log.d("AddMatchTeams", "Team B has " + playersB.size() + " players");
            } else {
                teamB.setPlayers(new ArrayList<>());
                android.util.Log.w("AddMatchTeams", "Team B has no players");
            }
            
            // Update UI
            if (etTeamAName != null) {
                etTeamAName.setText(teamA.getTeamName());
                etTeamAName.setEnabled(false); // Don't allow editing tournament team names
            }
            
            if (etTeamBName != null) {
                etTeamBName.setText(teamB.getTeamName());
                etTeamBName.setEnabled(false); // Don't allow editing tournament team names
            }
            
            // Update RecyclerViews
            if (adapterA != null) {
                adapterA.submitList(new ArrayList<>(teamA.getPlayers()));
                updateTeamCounts(true);
            }
            
            if (adapterB != null) {
                adapterB.submitList(new ArrayList<>(teamB.getPlayers()));
                updateTeamCounts(false);
            }
            
            // Disable add player buttons for tournament matches (teams are predefined)
            if (btnAddPlayerA != null) {
                btnAddPlayerA.setEnabled(false);
                btnAddPlayerA.setAlpha(0.5f);
            }
            
            if (btnAddPlayerB != null) {
                btnAddPlayerB.setEnabled(false);
                btnAddPlayerB.setAlpha(0.5f);
            }
            
            android.util.Log.d("AddMatchTeams", "Tournament match teams UI updated successfully");
        });
    }
    
    /**
     * Navigate to toss fragment for cricket or directly to live score for football
     * For tournament matches, uses the match already loaded by loadTournamentMatchTeams()
     * to avoid creating duplicate observers on the same LiveData
     */
    private void navigateToToss() {
        if (matchId == null) {
            android.util.Log.w("AddMatchTeams", "Cannot navigate: matchId is null");
            return;
        }
        
        if (currentMatch == null) {
            android.util.Log.w("AddMatchTeams", "Cannot navigate: match not loaded yet");
            return;
        }
        
        android.util.Log.d("AddMatchTeams", "Preparing to navigate to toss/live score for sport: " + sportType);
        android.util.Log.d("AddMatchTeams", "Match ID: " + matchId);
        android.util.Log.d("AddMatchTeams", "Match name: " + currentMatch.getName());
        android.util.Log.d("AddMatchTeams", "Is tournament match: " + isTournamentMatch);
        
        // CRITICAL: Verify match has config before saving
        // Check if match config exists
        boolean hasConfig = false;
        if (currentMatch instanceof com.example.tournafy.domain.models.match.cricket.CricketMatch) {
            com.example.tournafy.domain.models.base.MatchConfig config =
                ((com.example.tournafy.domain.models.match.cricket.CricketMatch) currentMatch).getMatchConfig();
            hasConfig = (config != null);
            android.util.Log.d("AddMatchTeams", "Cricket match config exists: " + hasConfig);
            if (config != null && config instanceof com.example.tournafy.domain.models.match.cricket.CricketMatchConfig) {
                com.example.tournafy.domain.models.match.cricket.CricketMatchConfig cricketConfig = 
                    (com.example.tournafy.domain.models.match.cricket.CricketMatchConfig) config;
                android.util.Log.d("AddMatchTeams", "Config - Overs: " + cricketConfig.getNumberOfOvers() + 
                                  ", Players per side: " + cricketConfig.getPlayersPerSide());
            }
        } else if (currentMatch instanceof com.example.tournafy.domain.models.match.football.FootballMatch) {
            com.example.tournafy.domain.models.base.MatchConfig config =
                ((com.example.tournafy.domain.models.match.football.FootballMatch) currentMatch).getMatchConfig();
            hasConfig = (config != null);
            android.util.Log.d("AddMatchTeams", "Football match config exists: " + hasConfig);
            if (config != null && config instanceof com.example.tournafy.domain.models.match.football.FootballMatchConfig) {
                com.example.tournafy.domain.models.match.football.FootballMatchConfig footballConfig = 
                    (com.example.tournafy.domain.models.match.football.FootballMatchConfig) config;
                android.util.Log.d("AddMatchTeams", "Config - Duration: " + footballConfig.getMatchDuration() + 
                                  " mins, Players per side: " + footballConfig.getPlayersPerSide());
            }
        }
        
        // CRITICAL: Don't proceed if config is missing!
        if (!hasConfig) {
            android.util.Log.e("AddMatchTeams", "ERROR: Cannot navigate - Match config is NULL!");
            android.util.Log.e("AddMatchTeams", "The config should have been set in AddMatchDetailsFragment!");
            android.widget.Toast.makeText(getContext(), 
                "Error: Match configuration missing. Please go back and set match details.", 
                android.widget.Toast.LENGTH_LONG).show();
            return;
        }
        
        // Save match to Firestore to ensure any changes are persisted
        android.util.Log.d("AddMatchTeams", "Saving match to Firestore before navigation");
        hostViewModel.updateMatch(currentMatch, new com.example.tournafy.service.interfaces.IHostingService.HostingCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        android.util.Log.d("AddMatchTeams", "Match saved to Firestore successfully");
                        
                        // Add delay to ensure Firestore has propagated the changes
                        // This matches AddMatchDetailsFragment pattern which also has implicit delay
                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // Validate that view is still valid
                                if (!isAdded() || getView() == null) {
                                    android.util.Log.w("AddMatchTeams", "Fragment not attached, cannot navigate");
                                    return;
                                }
                                
                                android.util.Log.d("AddMatchTeams", "Navigating to toss/live score after save delay");
                                
                                Bundle args = new Bundle();
                                args.putString("match_id", matchId);
                                
                                try {
                                    androidx.navigation.NavController navController = androidx.navigation.Navigation.findNavController(requireView());
                                    
                                    // For tournament matches, skip toss screen and go directly to live score
                                    // Toss is already auto-generated during bracket creation
                                    if ("CRICKET".equalsIgnoreCase(sportType)) {
                                        if (isTournamentMatch) {
                                            android.util.Log.d("AddMatchTeams", "Tournament match - skipping toss, going directly to live score");
                                            navController.navigate(R.id.action_addMatchTeams_to_cricketLiveScore, args);
                                        } else {
                                            android.util.Log.d("AddMatchTeams", "Regular match - navigating to cricket toss");
                                            navController.navigate(R.id.action_addMatchTeams_to_cricketToss, args);
                                        }
                                    } else {
                                        android.util.Log.d("AddMatchTeams", "Navigating to football live score");
                                        navController.navigate(R.id.action_addMatchTeams_to_footballLiveScore, args);
                                    }
                                    android.util.Log.d("AddMatchTeams", "Navigation completed successfully");
                                } catch (Exception e) {
                                    android.util.Log.e("AddMatchTeams", "Navigation failed: " + e.getMessage(), e);
                                    android.widget.Toast.makeText(getContext(), 
                                        "Navigation failed: " + e.getMessage(), 
                                        android.widget.Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, 300); // 300ms delay after save - ensures Firestore propagation for MatchViewModel
                    }
                    
                    @Override
                    public void onError(Exception e) {
                        android.util.Log.e("AddMatchTeams", "Failed to save match before navigation", e);
                        android.widget.Toast.makeText(getContext(), 
                            "Failed to save match: " + e.getMessage(), 
                            android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
    }
}