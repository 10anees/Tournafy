package com.example.tournafy.ui.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tournafy.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * EventInputDialog - Enhanced dialog for football event input
 * 
 * Supports:
 * - Goal events with type selection (OPEN_PLAY, PENALTY, FREE_KICK, HEADER, OWN_GOAL)
 * - Card events with type selection (YELLOW, RED)
 * - Substitution events with dual player selection (player out + player in)
 * 
 * Follows Material 3 design with proper validation and user feedback.
 */
public class EventInputDialog extends BottomSheetDialogFragment {

    public enum EventType { GOAL, CARD, SUB }

    private EventType eventType;
    private EventInputListener listener;
    
    // Data from parent
    private String teamAName, teamBName;
    private List<String> teamAPlayers, teamBPlayers;

    // UI - Basic
    private TextView tvDialogTitle;
    private ChipGroup chipGroupTeam;
    private Chip chipTeamA, chipTeamB;
    private AutoCompleteTextView actvPlayer;
    private Button btnSaveEvent;
    
    // UI - Goal specific
    private TextView tvGoalTypeLabel;
    private ChipGroup chipGroupGoalType;
    
    // UI - Card specific
    private TextView tvCardTypeLabel;
    private ChipGroup chipGroupCardType;
    
    // UI - Substitution specific
    private AutoCompleteTextView actvPlayerIn;

    public interface EventInputListener {
        void onEventCreated(String teamName, String playerName, String detail);
    }

    public static EventInputDialog newInstance(EventType type, String teamA, String teamB, 
                                             ArrayList<String> playersA, ArrayList<String> playersB) {
        EventInputDialog fragment = new EventInputDialog();
        fragment.eventType = type;
        fragment.teamAName = teamA;
        fragment.teamBName = teamB;
        fragment.teamAPlayers = playersA;
        fragment.teamBPlayers = playersB;
        return fragment;
    }

    public void setListener(EventInputListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_event_input, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvDialogTitle = view.findViewById(R.id.tvDialogTitle);
        chipGroupTeam = view.findViewById(R.id.chipGroupTeam);
        chipTeamA = view.findViewById(R.id.chipTeamA);
        chipTeamB = view.findViewById(R.id.chipTeamB);
        tvGoalTypeLabel = view.findViewById(R.id.tvGoalTypeLabel);
        chipGroupGoalType = view.findViewById(R.id.chipGroupGoalType);
        tvCardTypeLabel = view.findViewById(R.id.tvCardTypeLabel);
        chipGroupCardType = view.findViewById(R.id.chipGroupCardType);
        actvPlayer = view.findViewById(R.id.actvPlayer);
        actvPlayerIn = view.findViewById(R.id.actvPlayerIn);
        btnSaveEvent = view.findViewById(R.id.btnSaveEvent);

        setupUI();
        setupListeners();
    }

    private void setupUI() {
        chipTeamA.setText(teamAName);
        chipTeamB.setText(teamBName);

        switch (eventType) {
            case GOAL:
                tvDialogTitle.setText("⚽ Record Goal");
                // Show goal type selection
                if (tvGoalTypeLabel != null) tvGoalTypeLabel.setVisibility(View.VISIBLE);
                if (chipGroupGoalType != null) chipGroupGoalType.setVisibility(View.VISIBLE);
                // Hide card type
                if (tvCardTypeLabel != null) tvCardTypeLabel.setVisibility(View.GONE);
                if (chipGroupCardType != null) chipGroupCardType.setVisibility(View.GONE);
                // Hide player in
                if (actvPlayerIn != null) actvPlayerIn.setVisibility(View.GONE);
                break;
                
            case CARD:
                tvDialogTitle.setText("🟨 Record Card");
                // Hide goal type
                if (tvGoalTypeLabel != null) tvGoalTypeLabel.setVisibility(View.GONE);
                if (chipGroupGoalType != null) chipGroupGoalType.setVisibility(View.GONE);
                // Show card type
                if (tvCardTypeLabel != null) tvCardTypeLabel.setVisibility(View.VISIBLE);
                if (chipGroupCardType != null) chipGroupCardType.setVisibility(View.VISIBLE);
                // Hide player in
                if (actvPlayerIn != null) actvPlayerIn.setVisibility(View.GONE);
                break;
                
            case SUB:
                tvDialogTitle.setText("🔄 Record Substitution");
                // Hide goal type
                if (tvGoalTypeLabel != null) tvGoalTypeLabel.setVisibility(View.GONE);
                if (chipGroupGoalType != null) chipGroupGoalType.setVisibility(View.GONE);
                // Hide card type
                if (tvCardTypeLabel != null) tvCardTypeLabel.setVisibility(View.GONE);
                if (chipGroupCardType != null) chipGroupCardType.setVisibility(View.GONE);
                // Show player in
                if (actvPlayerIn != null) actvPlayerIn.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void setupListeners() {
        chipGroupTeam.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            
            List<String> currentPlayers = (id == R.id.chipTeamA) ? teamAPlayers : teamBPlayers;
            
            // Update Player Out Dropdown
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), 
                    android.R.layout.simple_dropdown_item_1line, currentPlayers);
            actvPlayer.setAdapter(adapter);
            actvPlayer.setText("", false); // Clear previous selection
            
            // Update Player In Dropdown (for substitutions)
            if (actvPlayerIn != null && eventType == EventType.SUB) {
                actvPlayerIn.setAdapter(adapter);
                actvPlayerIn.setText("", false);
            }
        });

        btnSaveEvent.setOnClickListener(v -> {
            if (listener != null) {
                // Validate team selection
                if (!chipTeamA.isChecked() && !chipTeamB.isChecked()) {
                    Toast.makeText(getContext(), "Please select a team", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                String team = chipTeamA.isChecked() ? teamAName : teamBName;
                String player = actvPlayer.getText().toString();
                
                // Validate player selection
                if (player.isEmpty()) {
                    Toast.makeText(getContext(), "Please select a player", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                String detail = "";
                
                switch (eventType) {
                    case GOAL:
                        // Get goal type from chipGroupGoalType if available
                        if (chipGroupGoalType != null) {
                            int checkedId = chipGroupGoalType.getCheckedChipId();
                            if (checkedId == R.id.chipPenalty) {
                                detail = "PENALTY";
                            } else if (checkedId == R.id.chipFreeKick) {
                                detail = "FREE_KICK";
                            } else if (checkedId == R.id.chipHeader) {
                                detail = "HEADER";
                            } else if (checkedId == R.id.chipOwnGoal) {
                                detail = "OWN_GOAL";
                            } else {
                                detail = "OPEN_PLAY";
                            }
                        } else {
                            detail = "OPEN_PLAY";
                        }
                        break;
                        
                    case CARD:
                        // Validate card type selection
                        int cardId = chipGroupCardType.getCheckedChipId();
                        if (cardId == -1) {
                            Toast.makeText(getContext(), "Please select card type", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        detail = (cardId == R.id.chipRed) ? "RED" : "YELLOW";
                        break;
                        
                    case SUB:
                        // For substitutions, detail contains playerIn name
                        if (actvPlayerIn != null) {
                            detail = actvPlayerIn.getText().toString();
                            if (detail.isEmpty()) {
                                Toast.makeText(getContext(), "Please select substitute player", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            // Validate different players
                            if (detail.equals(player)) {
                                Toast.makeText(getContext(), "Player in must be different from player out", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        break;
                }

                listener.onEventCreated(team, player, detail);
            }
            dismiss();
        });
    }
}