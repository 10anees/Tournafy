package com.example.tournafy.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tournafy.R;
import com.example.tournafy.domain.models.team.Player;
import com.google.android.material.chip.Chip;

public class PlayerListAdapter extends ListAdapter<Player, PlayerListAdapter.PlayerViewHolder> {

    private final OnPlayerActionListener listener;

    public interface OnPlayerActionListener {
        void onRemove(Player player);
        void onStartingXIChanged(Player player, boolean isStartingXI);
    }

    public PlayerListAdapter(OnPlayerActionListener listener) {
        super(new DiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_player_list, parent, false);
        return new PlayerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayerViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class PlayerViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvRole;
        Chip chipStartingXI;
        ImageButton btnRemove;

        public PlayerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvPlayerName);
            tvRole = itemView.findViewById(R.id.tvPlayerRole);
            chipStartingXI = itemView.findViewById(R.id.chipStartingXI);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }

        public void bind(Player player, OnPlayerActionListener listener) {
            tvName.setText(player.getPlayerName());

            if (player.getRole() != null && !player.getRole().isEmpty()) {
                tvRole.setText(player.getRole());
                tvRole.setVisibility(View.VISIBLE);
            } else {
                tvRole.setVisibility(View.GONE);
            }

            // Update chip state
            chipStartingXI.setOnCheckedChangeListener(null); // prevent loop
            chipStartingXI.setChecked(player.isStartingXI());

            if (player.isStartingXI()) {
                chipStartingXI.setText("Starting XI");
                chipStartingXI.setChipBackgroundColorResource(R.color.md_theme_dark_primaryContainer);
                chipStartingXI.setTextColor(itemView.getResources().getColor(android.R.color.white));
            } else {
                chipStartingXI.setText("Substitute");
                chipStartingXI.setChipBackgroundColorResource(android.R.color.transparent);
                chipStartingXI.setTextColor(itemView.getResources().getColor(R.color.black));
            }

            // Handle chip toggle
            chipStartingXI.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onStartingXIChanged(player, isChecked);
                }
            });

            btnRemove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemove(player);
                }
            });
        }
    }

    static class DiffCallback extends DiffUtil.ItemCallback<Player> {
        @Override
        public boolean areItemsTheSame(@NonNull Player oldItem, @NonNull Player newItem) {
            return oldItem.getPlayerId() != null && oldItem.getPlayerId().equals(newItem.getPlayerId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Player oldItem, @NonNull Player newItem) {
            return oldItem.getPlayerName().equals(newItem.getPlayerName()) &&
                    ((oldItem.getRole() == null && newItem.getRole() == null) ||
                            (oldItem.getRole() != null && oldItem.getRole().equals(newItem.getRole()))) &&
                    oldItem.isStartingXI() == newItem.isStartingXI();
        }
    }
}
