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
import com.example.tournafy.domain.models.team.TournamentTeam;

import java.util.Locale;

/**
 * Adapter for displaying tournament points table/standings.
 * Shows: Position, Team Name, Played, Won, Lost, Drawn, Points, NRR/GD
 */
public class PointsTableAdapter extends ListAdapter<TournamentTeam, PointsTableAdapter.ViewHolder> {

    private final OnTeamClickListener listener;
    private final boolean isCricket; // For displaying NRR vs Goal Difference

    public interface OnTeamClickListener {
        void onTeamClick(TournamentTeam team);
    }

    public PointsTableAdapter(OnTeamClickListener listener, boolean isCricket) {
        super(new TeamDiffCallback());
        this.listener = listener;
        this.isCricket = isCricket;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_points_table_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TournamentTeam team = getItem(position);
        holder.bind(team, position + 1, isCricket, listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvPosition;
        private final TextView tvTeamName;
        private final TextView tvPlayed;
        private final TextView tvWon;
        private final TextView tvLost;
        private final TextView tvDrawn;
        private final TextView tvNrrGd;
        private final TextView tvPoints;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPosition = itemView.findViewById(R.id.tvPosition);
            tvTeamName = itemView.findViewById(R.id.tvTeamName);
            tvPlayed = itemView.findViewById(R.id.tvPlayed);
            tvWon = itemView.findViewById(R.id.tvWon);
            tvLost = itemView.findViewById(R.id.tvLost);
            tvDrawn = itemView.findViewById(R.id.tvDrawn);
            tvNrrGd = itemView.findViewById(R.id.tvNrrGd);
            tvPoints = itemView.findViewById(R.id.tvPoints);
        }

        public void bind(TournamentTeam team, int position, boolean isCricket, OnTeamClickListener listener) {
            tvPosition.setText(String.valueOf(position));
            tvTeamName.setText(team.getTeamName());
            tvPlayed.setText(String.valueOf(team.getMatchesPlayed()));
            tvWon.setText(String.valueOf(team.getMatchesWon()));
            tvLost.setText(String.valueOf(team.getMatchesLost()));
            tvDrawn.setText(String.valueOf(team.getMatchesDrawn()));
            tvPoints.setText(String.valueOf(team.getPoints()));

            // Display NRR for cricket, Goal Difference for football
            if (isCricket) {
                tvNrrGd.setText(String.format(Locale.getDefault(), "%.3f", team.getNetRunRate()));
            } else {
                int gd = team.getGoalsFor() - team.getGoalsAgainst();
                tvNrrGd.setText(gd > 0 ? "+" + gd : String.valueOf(gd));
            }

            // Highlight top positions (qualification spots)
            if (position <= 4) {
                itemView.setBackgroundResource(R.drawable.bg_qualified_row);
            } else {
                itemView.setBackgroundResource(R.drawable.bg_normal_row);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTeamClick(team);
                }
            });
        }
    }

    static class TeamDiffCallback extends DiffUtil.ItemCallback<TournamentTeam> {
        @Override
        public boolean areItemsTheSame(@NonNull TournamentTeam oldItem, @NonNull TournamentTeam newItem) {
            return oldItem.getTeamId().equals(newItem.getTeamId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull TournamentTeam oldItem, @NonNull TournamentTeam newItem) {
            return oldItem.getPoints() == newItem.getPoints() &&
                    oldItem.getMatchesPlayed() == newItem.getMatchesPlayed() &&
                    oldItem.getMatchesWon() == newItem.getMatchesWon() &&
                    oldItem.getNetRunRate() == newItem.getNetRunRate();
        }
    }
}
