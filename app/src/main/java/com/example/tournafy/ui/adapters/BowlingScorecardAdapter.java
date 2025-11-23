package com.example.tournafy.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tournafy.R;
import com.example.tournafy.domain.models.match.cricket.BowlerStats;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BowlingScorecardAdapter extends RecyclerView.Adapter<BowlingScorecardAdapter.BowlerViewHolder> {

    private List<BowlerStats> bowlerStatsList;

    public BowlingScorecardAdapter() {
        this.bowlerStatsList = new ArrayList<>();
    }

    public void setBowlerStats(List<BowlerStats> stats) {
        this.bowlerStatsList = stats != null ? stats : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BowlerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bowling_row, parent, false);
        return new BowlerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BowlerViewHolder holder, int position) {
        BowlerStats stats = bowlerStatsList.get(position);
        holder.bind(stats);
    }

    @Override
    public int getItemCount() {
        return bowlerStatsList.size();
    }

    static class BowlerViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvBowlerName;
        private final TextView tvOvers;
        private final TextView tvMaidens;
        private final TextView tvRunsConceded;
        private final TextView tvWickets;
        private final TextView tvEconomy;

        public BowlerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBowlerName = itemView.findViewById(R.id.tvBowlerName);
            tvOvers = itemView.findViewById(R.id.tvOvers);
            tvMaidens = itemView.findViewById(R.id.tvMaidens);
            tvRunsConceded = itemView.findViewById(R.id.tvRunsConceded);
            tvWickets = itemView.findViewById(R.id.tvWickets);
            tvEconomy = itemView.findViewById(R.id.tvEconomy);
        }

        public void bind(BowlerStats stats) {
            tvBowlerName.setText(stats.getPlayerName());
            tvOvers.setText(stats.getOversBowled());
            tvMaidens.setText(String.valueOf(stats.getMaidenOvers()));
            tvRunsConceded.setText(String.valueOf(stats.getRunsConceded()));
            tvWickets.setText(String.valueOf(stats.getWicketsTaken()));
            tvEconomy.setText(String.format(Locale.getDefault(), "%.2f", stats.getEconomyRate()));
        }
    }
}
