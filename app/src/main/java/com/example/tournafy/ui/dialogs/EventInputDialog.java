package com.example.tournafy.ui.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tournafy.R;
import com.example.tournafy.domain.models.match.football.FootballEvent;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class EventInputDialog extends BottomSheetDialogFragment {

    public enum EventType { GOAL, CARD, SUB }

    private EventType eventType;
    private EventInputListener listener;
    
    // Data from parent
    private String teamAName, teamBName;
    private List<String> teamAPlayers, teamBPlayers;

    // UI
    private TextView tvDialogTitle;
    private ChipGroup chipGroupTeam, chipGroupCardType;
    private Chip chipTeamA, chipTeamB;
    private AutoCompleteTextView actvPlayer;
    private Button btnSaveEvent;

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
        chipGroupCardType = view.findViewById(R.id.chipGroupCardType);
        actvPlayer = view.findViewById(R.id.actvPlayer);
        btnSaveEvent = view.findViewById(R.id.btnSaveEvent);

        setupUI();
        setupListeners();
    }

    private void setupUI() {
        chipTeamA.setText(teamAName);
        chipTeamB.setText(teamBName);

        switch (eventType) {
            case GOAL:
                tvDialogTitle.setText("Record Goal");
                chipGroupCardType.setVisibility(View.GONE);
                break;
            case CARD:
                tvDialogTitle.setText("Record Card");
                chipGroupCardType.setVisibility(View.VISIBLE);
                break;
            case SUB:
                tvDialogTitle.setText("Record Substitution");
                chipGroupCardType.setVisibility(View.GONE);
                break;
        }
    }

    private void setupListeners() {
        chipGroupTeam.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            
            List<String> currentPlayers = (id == R.id.chipTeamA) ? teamAPlayers : teamBPlayers;
            
            // Update Dropdown
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), 
                    android.R.layout.simple_dropdown_item_1line, currentPlayers);
            actvPlayer.setAdapter(adapter);
            actvPlayer.setText("", false); // Clear previous selection
        });

        btnSaveEvent.setOnClickListener(v -> {
            if (listener != null) {
                String team = chipTeamA.isChecked() ? teamAName : teamBName;
                String player = actvPlayer.getText().toString();
                String detail = "";
                
                if (eventType == EventType.CARD) {
                    detail = (chipGroupCardType.getCheckedChipId() == R.id.chipRed) ? "RED" : "YELLOW";
                }

                listener.onEventCreated(team, player, detail);
            }
            dismiss();
        });
    }
}