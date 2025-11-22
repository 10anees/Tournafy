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
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_select_player, null);

        recyclerViewPlayers = view.findViewById(R.id.recyclerViewPlayers);
        recyclerViewPlayers.setLayoutManager(new LinearLayoutManager(getContext()));

        // Get list of available bowlers
        List<Player> availableBowlers = getAvailableBowlers();
        
        if (availableBowlers.isEmpty()) {
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

        return builder.create();
    }

    private List<Player> getAvailableBowlers() {
        List<Player> availableBowlers = new ArrayList<>();
        
        if (match == null || match.getTeams() == null || match.getTeams().isEmpty()) {
            return availableBowlers;
        }

        // Determine which team is currently bowling
        String bowlingTeamId = getCurrentBowlingTeamId();
        if (bowlingTeamId == null) {
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

        if (bowlingTeam == null || bowlingTeam.getPlayers() == null) {
            return availableBowlers;
        }

        // Include all bowlers from the bowling team (removed starting XI restriction)
        for (Player player : bowlingTeam.getPlayers()) {
            availableBowlers.add(player);
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
