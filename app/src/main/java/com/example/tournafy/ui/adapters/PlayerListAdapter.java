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

public class PlayerListAdapter extends ListAdapter<Player, PlayerListAdapter.PlayerViewHolder> {

    private final OnPlayerRemoveListener listener;

    public interface OnPlayerRemoveListener {
        void onRemove(Player player);
    }

    public PlayerListAdapter(OnPlayerRemoveListener listener) {
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
        ImageButton btnRemove;

        public PlayerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvPlayerName);
            tvRole = itemView.findViewById(R.id.tvPlayerRole);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }

        public void bind(Player player, OnPlayerRemoveListener listener) {
            tvName.setText(player.getPlayerName());
            
            if (player.getRole() != null && !player.getRole().isEmpty()) {
                tvRole.setText(player.getRole());
                tvRole.setVisibility(View.VISIBLE);
            } else {
                tvRole.setVisibility(View.GONE);
            }

            btnRemove.setOnClickListener(v -> listener.onRemove(player));
        }
    }

    static class DiffCallback extends DiffUtil.ItemCallback<Player> {
        @Override
        public boolean areItemsTheSame(@NonNull Player oldItem, @NonNull Player newItem) {
            // Assuming Player has a unique ID. If creating new players locally,
            // ensure they have a temporary ID or compare by object reference for now.
            if (oldItem.getPlayerId() != null && newItem.getPlayerId() != null) {
                return oldItem.getPlayerId().equals(newItem.getPlayerId());
            }
            // Fallback for new objects without IDs
            return oldItem == newItem; 
        }

        @Override
        public boolean areContentsTheSame(@NonNull Player oldItem, @NonNull Player newItem) {
            return oldItem.getPlayerName().equals(newItem.getPlayerName()) &&
                   (oldItem.getRole() == null ? newItem.getRole() == null : oldItem.getRole().equals(newItem.getRole()));
        }
    }
}