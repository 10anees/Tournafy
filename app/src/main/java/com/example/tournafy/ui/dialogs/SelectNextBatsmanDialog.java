package com.example.tournafy.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tournafy.R;
import com.example.tournafy.domain.models.match.cricket.BatsmanStats;
import com.example.tournafy.domain.models.match.cricket.CricketMatch;
import com.example.tournafy.domain.models.team.MatchTeam;
import com.example.tournafy.domain.models.team.Player;
import com.example.tournafy.ui.adapters.PlayerSelectionAdapter;

import java.util.ArrayList;
import java.util.List;

public class SelectNextBatsmanDialog extends DialogFragment {

    private CricketMatch match;
    private OnBatsmanSelectedListener listener;
    private RecyclerView recyclerViewPlayers;
    private PlayerSelectionAdapter adapter;
    private boolean isForQueue; // true if adding to queue, false if immediate replacement

    public interface OnBatsmanSelectedListener {
        void onBatsmanSelected(Player player);
    }

    public static SelectNextBatsmanDialog newInstance(CricketMatch match, boolean isForQueue, OnBatsmanSelectedListener listener) {
        SelectNextBatsmanDialog dialog = new SelectNextBatsmanDialog();
        dialog.match = match;
        dialog.isForQueue = isForQueue;
        dialog.listener = listener;
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        android.util.Log.d("SelectNextBatsmanDialog", "onCreateDialog started");
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_select_player, null);

        recyclerViewPlayers = view.findViewById(R.id.recyclerViewPlayers);
        recyclerViewPlayers.setLayoutManager(new LinearLayoutManager(getContext()));

        // Get list of available batsmen
        List<Player> availableBatsmen = getAvailableBatsmen();
        android.util.Log.d("SelectNextBatsmanDialog", "Available batsmen count: " + availableBatsmen.size());
        
        if (availableBatsmen.isEmpty()) {
            android.util.Log.e("SelectNextBatsmanDialog", "No batsmen available - dismissing dialog");
            Toast.makeText(getContext(), "No batsmen available", Toast.LENGTH_SHORT).show();
            dismiss();
            return builder.create();
        }

        adapter = new PlayerSelectionAdapter(availableBatsmen, player -> {
            if (listener != null) {
                listener.onBatsmanSelected(player);
            }
            dismiss();
        });
        
        recyclerViewPlayers.setAdapter(adapter);

        String title = isForQueue ? "Add Batsman to Queue" : "Select Next Batsman";
        builder.setView(view)
                .setTitle(title)
                .setNegativeButton("Cancel", (dialog, which) -> dismiss());

        Dialog dialog = builder.create();
        android.util.Log.d("SelectNextBatsmanDialog", "Dialog created successfully with " + availableBatsmen.size() + " batsmen");
        android.util.Log.d("SelectNextBatsmanDialog", "Dialog isShowing: " + dialog.isShowing());
        return dialog;
    }
    
    @Override
    public void onStart() {
        super.onStart();
        android.util.Log.d("SelectNextBatsmanDialog", "onStart called - dialog should be visible now");
    }
    
    @Override
    public void onResume() {
        super.onResume();
        android.util.Log.d("SelectNextBatsmanDialog", "onResume called");
    }
    
    @Override
    public void onDismiss(@NonNull android.content.DialogInterface dialog) {
        super.onDismiss(dialog);
        android.util.Log.d("SelectNextBatsmanDialog", "onDismiss called");
    }

    private List<Player> getAvailableBatsmen() {
        List<Player> availableBatsmen = new ArrayList<>();
        
        if (match == null || match.getTeams() == null || match.getTeams().isEmpty()) {
            android.util.Log.e("SelectNextBatsmanDialog", "Match or teams is null/empty");
            return availableBatsmen;
        }

        // Determine which team is currently batting
        String battingTeamId = getCurrentBattingTeamId();
        if (battingTeamId == null) {
            android.util.Log.e("SelectNextBatsmanDialog", "Batting team ID is null");
            return availableBatsmen;
        }

        // Find the batting team
        MatchTeam battingTeam = null;
        for (MatchTeam team : match.getTeams()) {
            if (team.getTeamId().equals(battingTeamId)) {
                battingTeam = team;
                break;
            }
        }

        if (battingTeam == null) {
            android.util.Log.e("SelectNextBatsmanDialog", "Batting team not found in match teams");
            return availableBatsmen;
        }
        
        if (battingTeam.getPlayers() == null) {
            android.util.Log.e("SelectNextBatsmanDialog", "Batting team has null players list");
            return availableBatsmen;
        }

        android.util.Log.d("SelectNextBatsmanDialog", "Batting team has " + battingTeam.getPlayers().size() + " players");

        // Filter available batsmen: not out, not currently batting
        String currentStrikerId = match.getCurrentStrikerId();
        String currentNonStrikerId = match.getCurrentNonStrikerId();
        
        for (Player player : battingTeam.getPlayers()) {
            // Exclude currently batting players
            if (player.getPlayerId().equals(currentStrikerId) || 
                player.getPlayerId().equals(currentNonStrikerId)) {
                android.util.Log.d("SelectNextBatsmanDialog", "Skipping currently batting player: " + player.getPlayerName());
                continue;
            }
            
            // Check if player is out using batsman stats
            BatsmanStats stats = match.getBatsmanStats(player.getPlayerId());
            if (stats != null && stats.isOut()) {
                android.util.Log.d("SelectNextBatsmanDialog", "Skipping out player: " + player.getPlayerName());
                continue; // Skip players who are already out
            }
            
            availableBatsmen.add(player);
            android.util.Log.d("SelectNextBatsmanDialog", "Added player: " + player.getPlayerName());
        }

        return availableBatsmen;
    }

    private String getCurrentBattingTeamId() {
        if (match == null || match.getInnings() == null || match.getInnings().isEmpty()) {
            android.util.Log.e("SelectNextBatsmanDialog", "Match or innings is null/empty");
            return null;
        }

        // Get current innings using getCurrentInnings() instead of manually indexing
        com.example.tournafy.domain.models.match.cricket.Innings currentInnings = match.getCurrentInnings();
        if (currentInnings != null) {
            String battingTeamId = currentInnings.getBattingTeamId();
            android.util.Log.d("SelectNextBatsmanDialog", "Current Batting Team ID: " + battingTeamId);
            return battingTeamId;
        }

        android.util.Log.e("SelectNextBatsmanDialog", "getCurrentInnings() returned null");
        return null;
    }
}
