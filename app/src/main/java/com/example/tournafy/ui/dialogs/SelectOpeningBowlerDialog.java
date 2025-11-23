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

/**
 * Dialog for selecting the opening bowler at the start of an innings
 */
public class SelectOpeningBowlerDialog extends DialogFragment {

    private CricketMatch match;
    private OnOpeningBowlerSelectedListener listener;
    private RecyclerView recyclerViewPlayers;
    private PlayerSelectionAdapter adapter;

    public interface OnOpeningBowlerSelectedListener {
        void onOpeningBowlerSelected(Player player);
    }

    public static SelectOpeningBowlerDialog newInstance(CricketMatch match, OnOpeningBowlerSelectedListener listener) {
        SelectOpeningBowlerDialog dialog = new SelectOpeningBowlerDialog();
        dialog.match = match;
        dialog.listener = listener;
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        android.util.Log.d("SelectOpeningBowlerDialog", "onCreateDialog started");
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_select_player, null);

        recyclerViewPlayers = view.findViewById(R.id.recyclerViewPlayers);
        recyclerViewPlayers.setLayoutManager(new LinearLayoutManager(getContext()));

        // Get list of available bowlers
        List<Player> availableBowlers = getAvailableBowlers();
        android.util.Log.d("SelectOpeningBowlerDialog", "Available bowlers count: " + availableBowlers.size());
        
        if (availableBowlers.isEmpty()) {
            android.util.Log.e("SelectOpeningBowlerDialog", "No bowlers available - dismissing dialog");
            Toast.makeText(getContext(), "No bowlers available", Toast.LENGTH_SHORT).show();
            dismiss();
            return builder.create();
        }

        adapter = new PlayerSelectionAdapter(availableBowlers, player -> {
            android.util.Log.d("SelectOpeningBowlerDialog", "Opening bowler selected: " + player.getPlayerName());
            if (listener != null) {
                listener.onOpeningBowlerSelected(player);
            }
            dismiss();
        });
        
        recyclerViewPlayers.setAdapter(adapter);

        builder.setView(view)
                .setTitle("Select Opening Bowler")
                .setCancelable(false); // Force selection

        Dialog dialog = builder.create();
        android.util.Log.d("SelectOpeningBowlerDialog", "Dialog created successfully with " + availableBowlers.size() + " bowlers");
        return dialog;
    }
    
    @Override
    public void onStart() {
        super.onStart();
        android.util.Log.d("SelectOpeningBowlerDialog", "onStart called - dialog should be visible now");
    }
    
    @Override
    public void onResume() {
        super.onResume();
        android.util.Log.d("SelectOpeningBowlerDialog", "onResume called");
    }
    
    @Override
    public void onDismiss(@NonNull android.content.DialogInterface dialog) {
        super.onDismiss(dialog);
        android.util.Log.d("SelectOpeningBowlerDialog", "onDismiss called");
    }

    private List<Player> getAvailableBowlers() {
        List<Player> availableBowlers = new ArrayList<>();
        
        if (match == null || match.getTeams() == null || match.getTeams().isEmpty()) {
            android.util.Log.e("SelectOpeningBowlerDialog", "Match or teams is null/empty");
            return availableBowlers;
        }

        // Determine which team is currently bowling
        String bowlingTeamId = getCurrentBowlingTeamId();
        if (bowlingTeamId == null) {
            android.util.Log.e("SelectOpeningBowlerDialog", "Bowling team ID is null");
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
            android.util.Log.e("SelectOpeningBowlerDialog", "Bowling team not found in match teams");
            return availableBowlers;
        }
        
        if (bowlingTeam.getPlayers() == null) {
            android.util.Log.e("SelectOpeningBowlerDialog", "Bowling team has null players list");
            return availableBowlers;
        }

        android.util.Log.d("SelectOpeningBowlerDialog", "Bowling team has " + bowlingTeam.getPlayers().size() + " players");

        // All players from bowling team are available at innings start
        for (Player player : bowlingTeam.getPlayers()) {
            availableBowlers.add(player);
            android.util.Log.d("SelectOpeningBowlerDialog", "Added player: " + player.getPlayerName());
        }

        return availableBowlers;
    }

    private String getCurrentBowlingTeamId() {
        if (match == null) {
            android.util.Log.e("SelectOpeningBowlerDialog", "Match is null");
            return null;
        }

        // If innings exist, use them
        if (match.getInnings() != null && !match.getInnings().isEmpty()) {
            com.example.tournafy.domain.models.match.cricket.Innings currentInnings = match.getCurrentInnings();
            if (currentInnings != null) {
                String bowlingTeamId = currentInnings.getBowlingTeamId();
                android.util.Log.d("SelectOpeningBowlerDialog", "Current Bowling Team ID from innings: " + bowlingTeamId);
                return bowlingTeamId;
            }
        }

        // If no innings yet (match is SCHEDULED), determine bowling team from toss
        android.util.Log.d("SelectOpeningBowlerDialog", "No innings yet, determining bowling team from toss");
        
        if (match.getTeams() == null || match.getTeams().size() < 2) {
            android.util.Log.e("SelectOpeningBowlerDialog", "Not enough teams");
            return null;
        }

        String tossWinner = match.getTossWinner();
        String tossDecision = match.getTossDecision();

        if (tossWinner != null && tossDecision != null) {
            // Find toss winning and losing teams
            MatchTeam tossWinningTeam = null;
            MatchTeam tossLosingTeam = null;
            
            for (MatchTeam team : match.getTeams()) {
                if (team.getTeamName().equals(tossWinner)) {
                    tossWinningTeam = team;
                } else {
                    tossLosingTeam = team;
                }
            }
            
            if (tossWinningTeam != null && tossLosingTeam != null) {
                if (tossDecision.equals("BAT")) {
                    // Toss winner bats first, so loser bowls first
                    android.util.Log.d("SelectOpeningBowlerDialog", "Bowling team ID from toss (loser bowls): " + tossLosingTeam.getTeamId());
                    return tossLosingTeam.getTeamId();
                } else {
                    // Toss winner bowls first
                    android.util.Log.d("SelectOpeningBowlerDialog", "Bowling team ID from toss (winner bowls): " + tossWinningTeam.getTeamId());
                    return tossWinningTeam.getTeamId();
                }
            }
        }

        // Fallback: second team bowls (first team bats)
        android.util.Log.d("SelectOpeningBowlerDialog", "Using fallback - second team bowls");
        return match.getTeams().get(1).getTeamId();
    }
}
