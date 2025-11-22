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
import com.example.tournafy.domain.models.team.Player;
import com.example.tournafy.domain.models.team.Team;
import com.example.tournafy.ui.adapters.PlayerListAdapter;
import com.example.tournafy.ui.dialogs.AddPlayerDialog;
import com.example.tournafy.ui.viewmodels.HostViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class AddMatchTeamsFragment extends Fragment implements AddPlayerDialog.AddPlayerListener {

    private static final String ARG_SPORT_TYPE = "sport_type";

    private HostViewModel hostViewModel;

    // UI Components
    private TextInputEditText etTeamAName, etTeamBName;
    private RecyclerView rvTeamAPlayers, rvTeamBPlayers;
    private TextView tvTeamAPlayersCount, tvTeamBPlayersCount;
    private TextView tvTeamAStartingCount, tvTeamASubsCount;
    private TextView tvTeamBStartingCount, tvTeamBSubsCount;
    private MaterialButton btnAddPlayerA, btnAddPlayerB;

    // Local State
    private Team teamA;
    private Team teamB;
    private PlayerListAdapter adapterA, adapterB;
    private String sportType = "FOOTBALL"; // Default to football
    private int requiredStartingPlayers = 11; // Default, will be updated from config

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
                updateTeamCounts(false);
            }
        });
        rvTeamBPlayers.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTeamBPlayers.setAdapter(adapterB);
    }

    private void setupListeners() {
        btnAddPlayerA.setOnClickListener(v -> showAddPlayerDialog(true));
        btnAddPlayerB.setOnClickListener(v -> showAddPlayerDialog(false));
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
}