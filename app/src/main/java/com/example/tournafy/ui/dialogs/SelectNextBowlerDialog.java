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
import com.example.tournafy.domain.models.match.cricket.CricketMatch;
import com.example.tournafy.domain.models.team.MatchTeam;
import com.example.tournafy.domain.models.team.Player;
import com.example.tournafy.ui.adapters.PlayerSelectionAdapter;

import java.util.ArrayList;
import java.util.List;

public class SelectNextBowlerDialog extends DialogFragment {

    private CricketMatch match;
    private OnBowlerSelectedListener listener;
    private RecyclerView recyclerViewPlayers;
    private PlayerSelectionAdapter adapter;
    private boolean isForQueue; // true if adding to queue, false if immediate replacement

    public interface OnBowlerSelectedListener {
        void onBowlerSelected(Player player);
    }

    public static SelectNextBowlerDialog newInstance(CricketMatch match, boolean isForQueue, OnBowlerSelectedListener listener) {
        SelectNextBowlerDialog dialog = new SelectNextBowlerDialog();
        dialog.match = match;
        dialog.isForQueue = isForQueue;
        dialog.listener = listener;
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        android.util.Log.d("SelectNextBowlerDialog", "onCreateDialog started");
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_select_player, null);

        recyclerViewPlayers = view.findViewById(R.id.recyclerViewPlayers);
        recyclerViewPlayers.setLayoutManager(new LinearLayoutManager(getContext()));

        // Get list of available bowlers
        List<Player> availableBowlers = getAvailableBowlers();
        android.util.Log.d("SelectNextBowlerDialog", "Available bowlers count: " + availableBowlers.size());
        
        if (availableBowlers.isEmpty()) {
            android.util.Log.e("SelectNextBowlerDialog", "No bowlers available - dismissing dialog");
            Toast.makeText(getContext(), "No bowlers available", Toast.LENGTH_SHORT).show();
            dismiss();
            return builder.create();
        }

        adapter = new PlayerSelectionAdapter(availableBowlers, player -> {
            if (listener != null) {
                listener.onBowlerSelected(player);
            }
            dismiss();
        });
        
        recyclerViewPlayers.setAdapter(adapter);

        String title = isForQueue ? "Add Bowler to Queue" : "Select Next Bowler";
        builder.setView(view)
                .setTitle(title)
                .setNegativeButton("Cancel", (dialog, which) -> dismiss());

        Dialog dialog = builder.create();
        android.util.Log.d("SelectNextBowlerDialog", "Dialog created successfully with " + availableBowlers.size() + " bowlers");
        android.util.Log.d("SelectNextBowlerDialog", "Dialog isShowing: " + dialog.isShowing());
        return dialog;
    }
    
    @Override
    public void onStart() {
        super.onStart();
        android.util.Log.d("SelectNextBowlerDialog", "onStart called - dialog should be visible now");
    }
    
    @Override
    public void onResume() {
        super.onResume();
        android.util.Log.d("SelectNextBowlerDialog", "onResume called");
    }
    
    @Override
    public void onDismiss(@NonNull android.content.DialogInterface dialog) {
        super.onDismiss(dialog);
        android.util.Log.d("SelectNextBowlerDialog", "onDismiss called");
    }

    private List<Player> getAvailableBowlers() {
        List<Player> availableBowlers = new ArrayList<>();
        
        if (match == null || match.getTeams() == null || match.getTeams().isEmpty()) {
            android.util.Log.e("SelectNextBowlerDialog", "Match or teams is null/empty");
            return availableBowlers;
        }

        // Determine which team is currently bowling
        String bowlingTeamId = getCurrentBowlingTeamId();
        if (bowlingTeamId == null) {
            android.util.Log.e("SelectNextBowlerDialog", "Bowling team ID is null");
            return availableBowlers;
        }

        // Find the bowling team
        MatchTeam bowlingTeam = null;
        for (MatchTeam team : match.getTeams()) {
            if (team.getTeamId().equals(bowlingTeamId)) {
                bowlingTeam = team;
                break;
            }
        }

        if (bowlingTeam == null) {
            android.util.Log.e("SelectNextBowlerDialog", "Bowling team not found in match teams");
            return availableBowlers;
        }
        
        if (bowlingTeam.getPlayers() == null) {
            android.util.Log.e("SelectNextBowlerDialog", "Bowling team has null players list");
            return availableBowlers;
        }

        android.util.Log.d("SelectNextBowlerDialog", "Bowling team has " + bowlingTeam.getPlayers().size() + " players");

        // Include all bowlers from the bowling team (removed starting XI restriction)
        for (Player player : bowlingTeam.getPlayers()) {
            availableBowlers.add(player);
            android.util.Log.d("SelectNextBowlerDialog", "Added player: " + player.getPlayerName());
        }

        return availableBowlers;
    }

    private String getCurrentBowlingTeamId() {
        if (match == null || match.getInnings() == null || match.getInnings().isEmpty()) {
            android.util.Log.e("SelectNextBowlerDialog", "Match or innings is null/empty");
            return null;
        }

        // Get current innings using getCurrentInnings() instead of manually indexing
        com.example.tournafy.domain.models.match.cricket.Innings currentInnings = match.getCurrentInnings();
        if (currentInnings != null) {
            String bowlingTeamId = currentInnings.getBowlingTeamId();
            android.util.Log.d("SelectNextBowlerDialog", "Current Bowling Team ID: " + bowlingTeamId);
            return bowlingTeamId;
        }

        android.util.Log.e("SelectNextBowlerDialog", "getCurrentInnings() returned null");
        return null;
    }
}
