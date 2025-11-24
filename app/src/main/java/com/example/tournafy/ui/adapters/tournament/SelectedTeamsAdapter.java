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
import com.example.tournafy.domain.models.team.Team;
import com.google.android.material.button.MaterialButton;

/**
 * Adapter for displaying selected teams in tournament
 */
public class SelectedTeamsAdapter extends ListAdapter<Team, SelectedTeamsAdapter.ViewHolder> {

    public interface OnTeamInteractionListener {
        void onTeamClick(Team team);
        void onTeamRemove(Team team);
    }

    private final OnTeamInteractionListener listener;

    public SelectedTeamsAdapter(OnTeamInteractionListener listener) {
        super(new TeamDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_selected_team, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Team team = getItem(position);
        holder.bind(team, listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTeamName;
        private final TextView tvPlayerCount;
        private final TextView tvPlayerCountSubtitle;
        private final MaterialButton btnRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTeamName = itemView.findViewById(R.id.tvTeamName);
            tvPlayerCount = itemView.findViewById(R.id.tvPlayerCount);
            tvPlayerCountSubtitle = itemView.findViewById(R.id.tvPlayerCountSubtitle);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }

        public void bind(Team team, OnTeamInteractionListener listener) {
            tvTeamName.setText(team.getTeamName());
            
            // Display player count
            int playerCount = team.getPlayers() != null ? team.getPlayers().size() : 0;
            tvPlayerCount.setText(String.valueOf(playerCount));
            tvPlayerCountSubtitle.setText("Tap to manage players");

            // Handle card click to open player management
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTeamClick(team);
                }
            });

            // Handle remove button
            btnRemove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTeamRemove(team);
                }
            });
        }
    }

    static class TeamDiffCallback extends DiffUtil.ItemCallback<Team> {
        @Override
        public boolean areItemsTheSame(@NonNull Team oldItem, @NonNull Team newItem) {
            return oldItem.getTeamId().equals(newItem.getTeamId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Team oldItem, @NonNull Team newItem) {
            // Check if team name or player count changed
            boolean nameEquals = oldItem.getTeamName().equals(newItem.getTeamName());
            int oldPlayerCount = oldItem.getPlayers() != null ? oldItem.getPlayers().size() : 0;
            int newPlayerCount = newItem.getPlayers() != null ? newItem.getPlayers().size() : 0;
            return nameEquals && oldPlayerCount == newPlayerCount;
        }
    }
}
