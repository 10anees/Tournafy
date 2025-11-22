package com.example.tournafy.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tournafy.R;
import com.example.tournafy.domain.models.team.Player;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.UUID;

public class AddPlayerDialog extends BottomSheetDialogFragment {

    private static final String ARG_IS_TEAM_A = "is_team_a";
    private static final String ARG_SPORT_TYPE = "sport_type";

    private boolean isTeamA;
    private String sportType = "FOOTBALL"; // Default to football
    private AddPlayerListener listener;
    
    private TextInputLayout tilPlayerName;
    private TextInputEditText etPlayerName;
    private ChipGroup chipGroupRole;
    private com.google.android.material.materialswitch.MaterialSwitch switchStartingXI;
    private Button btnAdd, btnCancel;

    public interface AddPlayerListener {
        void onPlayerAdded(Player player, boolean isTeamA);
    }

    public static AddPlayerDialog newInstance(boolean isTeamA) {
        return newInstance(isTeamA, "FOOTBALL");
    }

    public static AddPlayerDialog newInstance(boolean isTeamA, String sportType) {
        AddPlayerDialog fragment = new AddPlayerDialog();
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_TEAM_A, isTeamA);
        args.putString(ARG_SPORT_TYPE, sportType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isTeamA = getArguments().getBoolean(ARG_IS_TEAM_A);
            sportType = getArguments().getString(ARG_SPORT_TYPE, "FOOTBALL");
        }
    }

    public void setListener(AddPlayerListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_player, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tilPlayerName = view.findViewById(R.id.tilPlayerName);
        etPlayerName = view.findViewById(R.id.etPlayerName);
        chipGroupRole = view.findViewById(R.id.chipGroupRole);
        switchStartingXI = view.findViewById(R.id.switchStartingXI);
        btnAdd = view.findViewById(R.id.btnAdd);
        btnCancel = view.findViewById(R.id.btnCancel);

        setupRoleChips();
        setupListeners();
    }
    
    private void setupRoleChips() {
        // Remove all existing chips
        chipGroupRole.removeAllViews();
        
        // Add sport-specific chips
        if ("CRICKET".equalsIgnoreCase(sportType)) {
            addRoleChip("Batsman");
            addRoleChip("Bowler");
            addRoleChip("All-Rounder");
            addRoleChip("Wicket Keeper");
        } else if ("FOOTBALL".equalsIgnoreCase(sportType)) {
            addRoleChip("Goalkeeper");
            addRoleChip("Defender");
            addRoleChip("Midfielder");
            addRoleChip("Forward");
        } else {
            // Default/generic role
            addRoleChip("Player");
        }
    }
    
    private void addRoleChip(String text) {
        com.google.android.material.chip.Chip chip = new com.google.android.material.chip.Chip(requireContext());
        chip.setId(View.generateViewId());
        chip.setText(text);
        chip.setCheckable(true);
        chip.setChipBackgroundColorResource(android.R.color.transparent);
        chip.setChipStrokeWidth(2f);
        chip.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
        chipGroupRole.addView(chip);
    }

    private void setupListeners() {
        btnCancel.setOnClickListener(v -> dismiss());

        btnAdd.setOnClickListener(v -> {
            String name = etPlayerName.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                tilPlayerName.setError("Name is required");
                return;
            }

            String role = getSelectedRole();
            boolean isStartingXI = switchStartingXI.isChecked();
            
            // Create a new Player object
            // Note: Generating a random UUID for local ID if not provided by backend
            Player player = new Player();
            player.setPlayerId(UUID.randomUUID().toString()); 
            player.setPlayerName(name);
            player.setRole(role);
            player.setStartingXI(isStartingXI);

            if (listener != null) {
                listener.onPlayerAdded(player, isTeamA);
            }
            dismiss();
        });
    }

    private String getSelectedRole() {
        int selectedId = chipGroupRole.getCheckedChipId();
        
        if (selectedId == View.NO_ID || selectedId == -1) {
            return "Player"; // Default if nothing selected
        }
        
        // Find the selected chip and get its text
        com.google.android.material.chip.Chip selectedChip = chipGroupRole.findViewById(selectedId);
        if (selectedChip != null) {
            return selectedChip.getText().toString();
        }
        
        return "Player"; // Default
    }
}