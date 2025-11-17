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

    private boolean isTeamA;
    private AddPlayerListener listener;
    
    private TextInputLayout tilPlayerName;
    private TextInputEditText etPlayerName;
    private ChipGroup chipGroupRole;
    private Button btnAdd, btnCancel;

    public interface AddPlayerListener {
        void onPlayerAdded(Player player, boolean isTeamA);
    }

    public static AddPlayerDialog newInstance(boolean isTeamA) {
        AddPlayerDialog fragment = new AddPlayerDialog();
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_TEAM_A, isTeamA);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isTeamA = getArguments().getBoolean(ARG_IS_TEAM_A);
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
        btnAdd = view.findViewById(R.id.btnAdd);
        btnCancel = view.findViewById(R.id.btnCancel);

        setupListeners();
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
            
            // Create a new Player object
            // Note: Generating a random UUID for local ID if not provided by backend
            Player player = new Player();
            player.setPlayerId(UUID.randomUUID().toString()); 
            player.setPlayerName(name);
            player.setRole(role);

            if (listener != null) {
                listener.onPlayerAdded(player, isTeamA);
            }
            dismiss();
        });
    }

    private String getSelectedRole() {
        int selectedId = chipGroupRole.getCheckedChipId();
        if (selectedId == R.id.chipBatsman) return "Batsman";
        if (selectedId == R.id.chipBowler) return "Bowler";
        if (selectedId == R.id.chipAllRounder) return "All-Rounder";
        if (selectedId == R.id.chipWicketKeeper) return "Wicket Keeper";
        return "Player"; // Default
    }
}