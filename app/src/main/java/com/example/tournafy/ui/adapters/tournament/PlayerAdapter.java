package com.example.tournafy.ui.adapters.tournament;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tournafy.R;
import com.example.tournafy.domain.models.team.Player;
import com.google.android.material.button.MaterialButton;

/**
 * Adapter for displaying players in a team
 */
public class PlayerAdapter extends ListAdapter<Player, PlayerAdapter.ViewHolder> {

    private final OnPlayerRemoveListener listener;

    public interface OnPlayerRemoveListener {
        void onPlayerRemove(Player player);
    }

    public PlayerAdapter(OnPlayerRemoveListener listener) {
        super(new PlayerDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_player, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Player player = getItem(position);
        holder.bind(player, listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvPlayerName;
        private final TextView tvJerseyNumber;
        private final TextView tvRole;
        private final MaterialButton btnRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlayerName = itemView.findViewById(R.id.tvPlayerName);
            tvJerseyNumber = itemView.findViewById(R.id.tvJerseyNumber);
            tvRole = itemView.findViewById(R.id.tvRole);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }

        public void bind(Player player, OnPlayerRemoveListener listener) {
            tvPlayerName.setText(player.getPlayerName());
            tvJerseyNumber.setText("#" + player.getJerseyNumber());
            tvRole.setText(player.getRole());
            
            btnRemove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPlayerRemove(player);
                }
            });
        }
    }

    static class PlayerDiffCallback extends DiffUtil.ItemCallback<Player> {
        @Override
        public boolean areItemsTheSame(@NonNull Player oldItem, @NonNull Player newItem) {
            return oldItem.getPlayerId().equals(newItem.getPlayerId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Player oldItem, @NonNull Player newItem) {
            return oldItem.getPlayerName().equals(newItem.getPlayerName()) &&
                    oldItem.getJerseyNumber() == newItem.getJerseyNumber() &&
                    oldItem.getRole().equals(newItem.getRole());
        }
    }
}
