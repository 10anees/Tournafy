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

/**
 * Adapter for displaying available teams that can be added to tournament
 */
public class AvailableTeamsAdapter extends ListAdapter<Team, AvailableTeamsAdapter.ViewHolder> {

    private final OnTeamSelectListener listener;

    public interface OnTeamSelectListener {
        void onTeamSelect(Team team);
    }

    public AvailableTeamsAdapter(OnTeamSelectListener listener) {
        super(new TeamDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_available_team, parent, false);
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTeamName = itemView.findViewById(R.id.tvTeamName);
            tvPlayerCount = itemView.findViewById(R.id.tvPlayerCount);
        }

        public void bind(Team team, OnTeamSelectListener listener) {
            tvTeamName.setText(team.getTeamName());
            
            int playerCount = team.getPlayers() != null ? team.getPlayers().size() : 0;
            tvPlayerCount.setText(playerCount + " players");

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTeamSelect(team);
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
            return oldItem.getTeamName().equals(newItem.getTeamName());
        }
    }
}
