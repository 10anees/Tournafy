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
import com.example.tournafy.domain.models.statistics.PlayerStatistics;

import java.util.Locale;

/**
 * Adapter for displaying top players in tournament.
 * Can show batting, bowling, or fielding statistics based on category.
 */
public class TopPlayersAdapter extends ListAdapter<PlayerStatistics, TopPlayersAdapter.ViewHolder> {

    public enum Category {
        BATTING, BOWLING, FIELDING
    }

    private final OnPlayerClickListener listener;
    private final Category category;

    public interface OnPlayerClickListener {
        void onPlayerClick(PlayerStatistics player);
    }

    public TopPlayersAdapter(OnPlayerClickListener listener, Category category) {
        super(new PlayerDiffCallback());
        this.listener = listener;
        this.category = category;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_top_player, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlayerStatistics player = getItem(position);
        holder.bind(player, position + 1, category, listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvRank;
        private final TextView tvPlayerName;
        private final TextView tvTeamName;
        private final TextView tvStat1;
        private final TextView tvStat2;
        private final TextView tvStat3;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvPlayerName = itemView.findViewById(R.id.tvPlayerName);
            tvTeamName = itemView.findViewById(R.id.tvTeamName);
            tvStat1 = itemView.findViewById(R.id.tvStat1);
            tvStat2 = itemView.findViewById(R.id.tvStat2);
            tvStat3 = itemView.findViewById(R.id.tvStat3);
        }

        public void bind(PlayerStatistics player, int rank, Category category, OnPlayerClickListener listener) {
            tvRank.setText(String.valueOf(rank));
            
            // TODO: Extract player name and team from related entities
            // PlayerStatistics stores data in Map format, need to create display model
            tvPlayerName.setText("Player " + rank);
            tvTeamName.setText("Team TBD");

            // TODO: Extract stats from cricketStats/footballStats Maps
            tvStat1.setText("Stat 1: --");
            tvStat2.setText("Stat 2: --");
            tvStat3.setText("Stat 3: --");
            
            /*
            switch (category) {
                case BATTING:
                    tvStat1.setText(String.format(Locale.getDefault(), "Runs: %d", player.getTotalRuns()));
                    tvStat2.setText(String.format(Locale.getDefault(), "Avg: %.2f", player.getBattingAverage()));
                    tvStat3.setText(String.format(Locale.getDefault(), "SR: %.2f", player.getStrikeRate()));
                    break;
                    
                case BOWLING:
                    tvStat1.setText(String.format(Locale.getDefault(), "Wkts: %d", player.getTotalWickets()));
                    tvStat2.setText(String.format(Locale.getDefault(), "Avg: %.2f", player.getBowlingAverage()));
                    tvStat3.setText(String.format(Locale.getDefault(), "Econ: %.2f", player.getEconomy()));
                    break;
                    
                case FIELDING:
                    tvStat1.setText(String.format(Locale.getDefault(), "Catches: %d", player.getTotalCatches()));
                    tvStat2.setText(String.format(Locale.getDefault(), "Stumpings: %d", player.getTotalStumpings()));
                    tvStat3.setText(String.format(Locale.getDefault(), "Run Outs: %d", player.getTotalRunOuts()));
                    break;
            }
            */

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPlayerClick(player);
                }
            });
        }
    }

    static class PlayerDiffCallback extends DiffUtil.ItemCallback<PlayerStatistics> {
        @Override
        public boolean areItemsTheSame(@NonNull PlayerStatistics oldItem, @NonNull PlayerStatistics newItem) {
            return oldItem.getPlayerId().equals(newItem.getPlayerId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull PlayerStatistics oldItem, @NonNull PlayerStatistics newItem) {
            // TODO: Compare actual stats from Maps once display model is created
            return oldItem.getPlayerId().equals(newItem.getPlayerId());
        }
    }
}
