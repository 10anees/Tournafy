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

    private HostViewModel hostViewModel;

    // UI Components
    private TextInputEditText etTeamAName, etTeamBName;
    private RecyclerView rvTeamAPlayers, rvTeamBPlayers;
    private TextView tvTeamAPlayersCount, tvTeamBPlayersCount;
    private MaterialButton btnAddPlayerA, btnAddPlayerB;

    // Local State
    private Team teamA;
    private Team teamB;
    private PlayerListAdapter adapterA, adapterB;

    public AddMatchTeamsFragment() {
        // Required empty public constructor
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

        // Initialize default empty teams
        teamA = new Team(); // Assuming Team has a default constructor or builder
        teamA.setPlayers(new ArrayList<>());
        
        teamB = new Team();
        teamB.setPlayers(new ArrayList<>());

        initViews(view);
        setupRecyclerViews();
        setupListeners();
    }

    private void initViews(View view) {
        etTeamAName = view.findViewById(R.id.etTeamAName);
        etTeamBName = view.findViewById(R.id.etTeamBName);
        rvTeamAPlayers = view.findViewById(R.id.rvTeamAPlayers);
        rvTeamBPlayers = view.findViewById(R.id.rvTeamBPlayers);
        tvTeamAPlayersCount = view.findViewById(R.id.tvTeamAPlayersCount);
        tvTeamBPlayersCount = view.findViewById(R.id.tvTeamBPlayersCount);
        btnAddPlayerA = view.findViewById(R.id.btnAddPlayerA);
        btnAddPlayerB = view.findViewById(R.id.btnAddPlayerB);
    }

    private void setupRecyclerViews() {
        // Adapter needs a callback for remove/edit. Using lambda for now.
        adapterA = new PlayerListAdapter(player -> removePlayerFromTeam(player, true));
        rvTeamAPlayers.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTeamAPlayers.setAdapter(adapterA);

        adapterB = new PlayerListAdapter(player -> removePlayerFromTeam(player, false));
        rvTeamBPlayers.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTeamBPlayers.setAdapter(adapterB);
    }

    private void setupListeners() {
        btnAddPlayerA.setOnClickListener(v -> showAddPlayerDialog(true));
        btnAddPlayerB.setOnClickListener(v -> showAddPlayerDialog(false));
    }

    private void showAddPlayerDialog(boolean isTeamA) {
        // This Dialog handles inputting a player name and role
        AddPlayerDialog dialog = AddPlayerDialog.newInstance(isTeamA);
        dialog.setListener(this); // Callback to this fragment
        dialog.show(getChildFragmentManager(), "AddPlayerDialog");
    }

    @Override
    public void onPlayerAdded(Player player, boolean isTeamA) {
        if (isTeamA) {
            teamA.getPlayers().add(player);
            adapterA.submitList(new ArrayList<>(teamA.getPlayers())); // Refresh list
            tvTeamAPlayersCount.setText("Players: " + teamA.getPlayers().size());
        } else {
            teamB.getPlayers().add(player);
            adapterB.submitList(new ArrayList<>(teamB.getPlayers())); // Refresh list
            tvTeamBPlayersCount.setText("Players: " + teamB.getPlayers().size());
        }
    }

    private void removePlayerFromTeam(Player player, boolean isTeamA) {
        if (isTeamA) {
            teamA.getPlayers().remove(player);
            adapterA.submitList(new ArrayList<>(teamA.getPlayers()));
            tvTeamAPlayersCount.setText("Players: " + teamA.getPlayers().size());
        } else {
            teamB.getPlayers().remove(player);
            adapterB.submitList(new ArrayList<>(teamB.getPlayers()));
            tvTeamBPlayersCount.setText("Players: " + teamB.getPlayers().size());
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