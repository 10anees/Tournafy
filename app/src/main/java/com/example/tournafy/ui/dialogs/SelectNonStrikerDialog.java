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

/**
 * Dialog for selecting the non-striker (second batsman) at the start of an innings
 */
public class SelectNonStrikerDialog extends DialogFragment {

    private CricketMatch match;
    private String strikerId; // ID of already selected striker to exclude
    private OnNonStrikerSelectedListener listener;
    private RecyclerView recyclerViewPlayers;
    private PlayerSelectionAdapter adapter;

    public interface OnNonStrikerSelectedListener {
        void onNonStrikerSelected(Player player);
    }

    public static SelectNonStrikerDialog newInstance(CricketMatch match, String strikerId, OnNonStrikerSelectedListener listener) {
        SelectNonStrikerDialog dialog = new SelectNonStrikerDialog();
        dialog.match = match;
        dialog.strikerId = strikerId;
        dialog.listener = listener;
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        android.util.Log.d("SelectNonStrikerDialog", "onCreateDialog started, excluding striker: " + strikerId);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_select_player, null);

        recyclerViewPlayers = view.findViewById(R.id.recyclerViewPlayers);
        recyclerViewPlayers.setLayoutManager(new LinearLayoutManager(getContext()));

        // Get list of available batsmen (excluding already selected striker)
        List<Player> availableBatsmen = getAvailableBatsmen();
        android.util.Log.d("SelectNonStrikerDialog", "Available batsmen count: " + availableBatsmen.size());
        
        if (availableBatsmen.isEmpty()) {
            android.util.Log.e("SelectNonStrikerDialog", "No batsmen available - dismissing dialog");
            Toast.makeText(getContext(), "No more batsmen available", Toast.LENGTH_SHORT).show();
            dismiss();
            return builder.create();
        }

        adapter = new PlayerSelectionAdapter(availableBatsmen, player -> {
            android.util.Log.d("SelectNonStrikerDialog", "Non-striker selected: " + player.getPlayerName());
            if (listener != null) {
                listener.onNonStrikerSelected(player);
            }
            dismiss();
        });
        
        recyclerViewPlayers.setAdapter(adapter);

        builder.setView(view)
                .setTitle("Select Opening Non-Striker")
                .setCancelable(false); // Force selection

        Dialog dialog = builder.create();
        android.util.Log.d("SelectNonStrikerDialog", "Dialog created successfully with " + availableBatsmen.size() + " batsmen");
        return dialog;
    }
    
    @Override
    public void onStart() {
        super.onStart();
        android.util.Log.d("SelectNonStrikerDialog", "onStart called - dialog should be visible now");
    }
    
    @Override
    public void onResume() {
        super.onResume();
        android.util.Log.d("SelectNonStrikerDialog", "onResume called");
    }
    
    @Override
    public void onDismiss(@NonNull android.content.DialogInterface dialog) {
        super.onDismiss(dialog);
        android.util.Log.d("SelectNonStrikerDialog", "onDismiss called");
    }

    private List<Player> getAvailableBatsmen() {
        List<Player> availableBatsmen = new ArrayList<>();
        
        if (match == null || match.getTeams() == null || match.getTeams().isEmpty()) {
            android.util.Log.e("SelectNonStrikerDialog", "Match or teams is null/empty");
            return availableBatsmen;
        }

        // Determine which team is currently batting
        String battingTeamId = getCurrentBattingTeamId();
        if (battingTeamId == null) {
            android.util.Log.e("SelectNonStrikerDialog", "Batting team ID is null");
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
            android.util.Log.e("SelectNonStrikerDialog", "Batting team not found in match teams");
            return availableBatsmen;
        }
        
        if (battingTeam.getPlayers() == null) {
            android.util.Log.e("SelectNonStrikerDialog", "Batting team has null players list");
            return availableBatsmen;
        }

        android.util.Log.d("SelectNonStrikerDialog", "Batting team has " + battingTeam.getPlayers().size() + " players");

        // Get available batsmen, excluding the already selected striker
        for (Player player : battingTeam.getPlayers()) {
            // Exclude the striker
            if (strikerId != null && player.getPlayerId().equals(strikerId)) {
                android.util.Log.d("SelectNonStrikerDialog", "Skipping striker: " + player.getPlayerName());
                continue;
            }
            
            // Check if player is out (shouldn't be at innings start, but check anyway)
            BatsmanStats stats = match.getBatsmanStats(player.getPlayerId());
            if (stats != null && stats.isOut()) {
                android.util.Log.d("SelectNonStrikerDialog", "Skipping out player: " + player.getPlayerName());
                continue;
            }
            
            availableBatsmen.add(player);
            android.util.Log.d("SelectNonStrikerDialog", "Added player: " + player.getPlayerName());
        }

        return availableBatsmen;
    }

    private String getCurrentBattingTeamId() {
        if (match == null) {
            android.util.Log.e("SelectNonStrikerDialog", "Match is null");
            return null;
        }

        // If innings exist, use them
        if (match.getInnings() != null && !match.getInnings().isEmpty()) {
            com.example.tournafy.domain.models.match.cricket.Innings currentInnings = match.getCurrentInnings();
            if (currentInnings != null) {
                String battingTeamId = currentInnings.getBattingTeamId();
                android.util.Log.d("SelectNonStrikerDialog", "Current Batting Team ID from innings: " + battingTeamId);
                return battingTeamId;
            }
        }

        // If no innings yet (match is SCHEDULED), determine batting team from toss
        android.util.Log.d("SelectNonStrikerDialog", "No innings yet, determining batting team from toss");
        
        if (match.getTeams() == null || match.getTeams().size() < 2) {
            android.util.Log.e("SelectNonStrikerDialog", "Not enough teams");
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
                    // Toss winner bats first
                    android.util.Log.d("SelectNonStrikerDialog", "Batting team ID from toss (winner bats): " + tossWinningTeam.getTeamId());
                    return tossWinningTeam.getTeamId();
                } else {
                    // Toss winner bowls first (loser bats)
                    android.util.Log.d("SelectNonStrikerDialog", "Batting team ID from toss (loser bats): " + tossLosingTeam.getTeamId());
                    return tossLosingTeam.getTeamId();
                }
            }
        }

        // Fallback: first team bats
        android.util.Log.d("SelectNonStrikerDialog", "Using fallback - first team bats");
        return match.getTeams().get(0).getTeamId();
    }
}
