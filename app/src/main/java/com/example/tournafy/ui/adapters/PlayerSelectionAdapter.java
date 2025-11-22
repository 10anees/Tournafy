package com.example.tournafy.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tournafy.R;
import com.example.tournafy.domain.models.team.Player;

import java.util.List;

public class PlayerSelectionAdapter extends RecyclerView.Adapter<PlayerSelectionAdapter.PlayerViewHolder> {

    private List<Player> players;
    private OnPlayerClickListener listener;

    public interface OnPlayerClickListener {
        void onPlayerClick(Player player);
    }

    public PlayerSelectionAdapter(List<Player> players, OnPlayerClickListener listener) {
        this.players = players;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_player_selection, parent, false);
        return new PlayerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayerViewHolder holder, int position) {
        Player player = players.get(position);
        holder.bind(player, listener);
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    static class PlayerViewHolder extends RecyclerView.ViewHolder {
        private TextView tvPlayerName;
        private TextView tvPlayerRole;

        public PlayerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlayerName = itemView.findViewById(R.id.tvPlayerName);
            tvPlayerRole = itemView.findViewById(R.id.tvPlayerRole);
        }

        public void bind(Player player, OnPlayerClickListener listener) {
            tvPlayerName.setText(player.getPlayerName());
            tvPlayerRole.setText(player.getRole() != null ? player.getRole() : "Player");
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPlayerClick(player);
                }
            });
        }
    }
}
