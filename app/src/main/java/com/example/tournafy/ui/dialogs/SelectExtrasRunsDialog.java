package com.example.tournafy.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.tournafy.R;

/**
 * Dialog for selecting additional runs scored on No-Ball or Wide deliveries.
 * 
 * For No-Ball: 1 penalty run + selected runs (batsman runs)
 * For Wide: 1 penalty run + selected runs (additional wides if batsman runs)
 */
public class SelectExtrasRunsDialog extends DialogFragment {

    private String extrasType;
    private OnRunsSelectedListener listener;

    public interface OnRunsSelectedListener {
        void onRunsSelected(int additionalRuns);
    }

    public static SelectExtrasRunsDialog newInstance(String extrasType, OnRunsSelectedListener listener) {
        SelectExtrasRunsDialog dialog = new SelectExtrasRunsDialog();
        dialog.extrasType = extrasType;
        dialog.listener = listener;
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_select_extras_runs, null);

        TextView tvTitle = view.findViewById(R.id.tvExtrasTitle);
        TextView tvDescription = view.findViewById(R.id.tvExtrasDescription);
        
        // Set title and description based on extras type
        String title;
        String description;
        if ("NO_BALL".equals(extrasType)) {
            title = "No Ball";
            description = "1 penalty run will be added.\nSelect additional runs scored by batsman:";
        } else {
            title = "Wide";
            description = "1 penalty run will be added.\nSelect additional runs (if any):";
        }
        
        tvTitle.setText(title);
        tvDescription.setText(description);

        // Setup run buttons
        setupRunButton(view, R.id.btnRuns0, 0);
        setupRunButton(view, R.id.btnRuns1, 1);
        setupRunButton(view, R.id.btnRuns2, 2);
        setupRunButton(view, R.id.btnRuns3, 3);
        setupRunButton(view, R.id.btnRuns4, 4);
        setupRunButton(view, R.id.btnRuns5, 5);
        setupRunButton(view, R.id.btnRuns6, 6);

        builder.setView(view)
                .setNegativeButton("Cancel", (dialog, which) -> dismiss());

        return builder.create();
    }
    
    private void setupRunButton(View rootView, int buttonId, int runs) {
        Button button = rootView.findViewById(buttonId);
        if (button != null) {
            button.setText(String.valueOf(runs));
            button.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRunsSelected(runs);
                }
                dismiss();
            });
        }
    }
}
