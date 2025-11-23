package com.example.tournafy.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tournafy.R;
import com.example.tournafy.domain.models.match.cricket.BatsmanStats;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BattingScorecardAdapter extends RecyclerView.Adapter<BattingScorecardAdapter.BatsmanViewHolder> {

    private List<BatsmanStats> batsmanStatsList;

    public BattingScorecardAdapter() {
        this.batsmanStatsList = new ArrayList<>();
    }

    public void setBatsmanStats(List<BatsmanStats> stats) {
        this.batsmanStatsList = stats != null ? stats : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BatsmanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_scorecard_row, parent, false);
        return new BatsmanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BatsmanViewHolder holder, int position) {
        BatsmanStats stats = batsmanStatsList.get(position);
        holder.bind(stats);
    }

    @Override
    public int getItemCount() {
        return batsmanStatsList.size();
    }

    static class BatsmanViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvPlayerName;
        private final TextView tvDismissal;
        private final TextView tvRuns;
        private final TextView tvBalls;
        private final TextView tvFours;
        private final TextView tvSixes;
        private final TextView tvStrikeRate;

        public BatsmanViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlayerName = itemView.findViewById(R.id.tvPlayerName);
            tvDismissal = itemView.findViewById(R.id.tvDismissal);
            tvRuns = itemView.findViewById(R.id.tvRuns);
            tvBalls = itemView.findViewById(R.id.tvBalls);
            tvFours = itemView.findViewById(R.id.tvFours);
            tvSixes = itemView.findViewById(R.id.tvSixes);
            tvStrikeRate = itemView.findViewById(R.id.tvStrikeRate);
        }

        public void bind(BatsmanStats stats) {
            tvPlayerName.setText(stats.getPlayerName());
            
            // Dismissal info
            if (stats.isOut()) {
                String dismissalText = stats.getDismissalType() != null 
                    ? stats.getDismissalType() 
                    : "out";
                tvDismissal.setText(dismissalText);
                tvDismissal.setVisibility(View.VISIBLE);
            } else {
                tvDismissal.setText("not out");
                tvDismissal.setVisibility(View.VISIBLE);
            }
            
            // Stats
            tvRuns.setText(String.valueOf(stats.getRunsScored()));
            tvBalls.setText(String.valueOf(stats.getBallsFaced()));
            tvFours.setText(String.valueOf(stats.getFours()));
            tvSixes.setText(String.valueOf(stats.getSixes()));
            tvStrikeRate.setText(String.format(Locale.getDefault(), "%.2f", stats.getStrikeRate()));
        }
    }
}
