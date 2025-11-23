package com.example.tournafy.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tournafy.R;
import com.example.tournafy.domain.models.match.cricket.BatsmanStats;
import com.example.tournafy.domain.models.team.Player;
import com.example.tournafy.domain.models.match.cricket.BowlerStats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayingXIAdapter extends RecyclerView.Adapter<PlayingXIAdapter.PlayerViewHolder> {

    private List<Player> players;
    private Map<String, BatsmanStats> batsmanStatsMap;
    private Map<String, BowlerStats> bowlerStatsMap;
    private String currentStrikerId;
    private String currentNonStrikerId;
    private String currentBowlerId;
    private boolean isBattingTeam;
    private boolean showCricketStats; // Flag to show/hide cricket-specific stats

    public PlayingXIAdapter(boolean isBattingTeam) {
        this(isBattingTeam, true); // Default: show cricket stats
    }
    
    public PlayingXIAdapter(boolean isBattingTeam, boolean showCricketStats) {
        this.players = new ArrayList<>();
        this.batsmanStatsMap = new HashMap<>();
        this.bowlerStatsMap = new HashMap<>();
        this.isBattingTeam = isBattingTeam;
        this.showCricketStats = showCricketStats;
    }

    public void setPlayers(List<Player> players) {
        this.players = players != null ? players : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setBatsmanStats(Map<String, BatsmanStats> stats) {
        this.batsmanStatsMap = stats != null ? stats : new HashMap<>();
        android.util.Log.d("PlayingXIAdapter", "Batsman stats set: " + (stats != null ? stats.size() : 0) + " players");
        notifyDataSetChanged();
    }

    public void setBowlerStats(Map<String, BowlerStats> stats) {
        this.bowlerStatsMap = stats != null ? stats : new HashMap<>();
        android.util.Log.d("PlayingXIAdapter", "Bowler stats set: " + (stats != null ? stats.size() : 0) + " players");
        notifyDataSetChanged();
    }

    public void setCurrentPlayers(String strikerId, String nonStrikerId, String bowlerId) {
        this.currentStrikerId = strikerId;
        this.currentNonStrikerId = nonStrikerId;
        this.currentBowlerId = bowlerId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_playing_xi, parent, false);
        return new PlayerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayerViewHolder holder, int position) {
        Player player = players.get(position);
        holder.bind(player, isBattingTeam, showCricketStats);
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    class PlayerViewHolder extends RecyclerView.ViewHolder {
        private TextView tvPlayerName;
        private TextView tvPlayerRole;
        private TextView tvStatusIndicator;
        private LinearLayout layoutStats;
        private TextView tvBatsmanStats;
        private TextView tvBatsmanStrikeRate;
        private TextView tvBowlerStats;
        private TextView tvBowlerEconomy;

        public PlayerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlayerName = itemView.findViewById(R.id.tvPlayerName);
            tvPlayerRole = itemView.findViewById(R.id.tvPlayerRole);
            tvStatusIndicator = itemView.findViewById(R.id.tvStatusIndicator);
            layoutStats = itemView.findViewById(R.id.layoutStats);
            tvBatsmanStats = itemView.findViewById(R.id.tvBatsmanStats);
            tvBatsmanStrikeRate = itemView.findViewById(R.id.tvBatsmanStrikeRate);
            tvBowlerStats = itemView.findViewById(R.id.tvBowlerStats);
            tvBowlerEconomy = itemView.findViewById(R.id.tvBowlerEconomy);
        }

        public void bind(Player player, boolean isBattingTeam, boolean showCricketStats) {
            tvPlayerName.setText(player.getPlayerName());
            tvPlayerRole.setText(player.getRole() != null ? player.getRole() : "Player");

            String playerId = player.getPlayerId();
            
            android.util.Log.d("PlayingXIAdapter", "Binding player: " + player.getPlayerName() + 
                " (ID: " + playerId + ") isBatting: " + isBattingTeam);
            
            // Reset visibility
            tvStatusIndicator.setVisibility(View.GONE);
            tvBatsmanStats.setVisibility(View.GONE);
            tvBatsmanStrikeRate.setVisibility(View.GONE);
            tvBowlerStats.setVisibility(View.GONE);
            tvBowlerEconomy.setVisibility(View.GONE);
            
            // If cricket stats are disabled (e.g., for football), just show player info
            if (!showCricketStats) {
                return;
            }

            if (isBattingTeam) {
                // Show batting stats
                BatsmanStats stats = batsmanStatsMap.get(playerId);
                android.util.Log.d("PlayingXIAdapter", "Batsman stats for " + playerId + ": " + 
                    (stats != null ? stats.getRunsScored() + "(" + stats.getBallsFaced() + ")" : "null"));
                
                // Highlight current batsmen
                if (playerId.equals(currentStrikerId)) {
                    tvStatusIndicator.setVisibility(View.VISIBLE);
                    tvStatusIndicator.setText("*");
                    tvStatusIndicator.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
                } else if (playerId.equals(currentNonStrikerId)) {
                    tvStatusIndicator.setVisibility(View.VISIBLE);
                    tvStatusIndicator.setText("•");
                    tvStatusIndicator.setTextColor(itemView.getContext().getColor(android.R.color.holo_orange_dark));
                }

                // Display batting stats - ALWAYS show for debugging, even if null
                tvBatsmanStats.setVisibility(View.VISIBLE);
                tvBatsmanStrikeRate.setVisibility(View.VISIBLE);
                
                if (stats != null) {
                    String statsText = stats.getRunsScored() + "(" + stats.getBallsFaced() + ")";
                    if (stats.isOut()) {
                        statsText += " - " + (stats.getDismissalType() != null ? stats.getDismissalType() : "OUT");
                    }
                    tvBatsmanStats.setText(statsText);
                    tvBatsmanStrikeRate.setText(String.format("SR: %.2f", stats.getStrikeRate()));
                } else {
                    // Show placeholder when no stats available
                    tvBatsmanStats.setText("0(0)");
                    tvBatsmanStrikeRate.setText("SR: 0.00");
                }
            } else {
                // Show bowling stats
                BowlerStats stats = bowlerStatsMap.get(playerId);
                android.util.Log.d("PlayingXIAdapter", "Bowler stats for " + playerId + ": " + 
                    (stats != null ? stats.getOversBowled() + "-" + stats.getWicketsTaken() : "null"));
                
                // Highlight current bowler
                if (playerId.equals(currentBowlerId)) {
                    tvStatusIndicator.setVisibility(View.VISIBLE);
                    tvStatusIndicator.setText("⚾");
                    tvStatusIndicator.setTextColor(itemView.getContext().getColor(android.R.color.holo_red_dark));
                }

                // Display bowling stats - ALWAYS show for debugging, even if null
                tvBowlerStats.setVisibility(View.VISIBLE);
                tvBowlerEconomy.setVisibility(View.VISIBLE);
                
                if (stats != null) {
                    // Format: Overs-Maidens-Runs-Wickets
                    String statsText = stats.getOversBowled() + "-" + 
                                      stats.getMaidenOvers() + "-" + 
                                      stats.getRunsConceded() + "-" + 
                                      stats.getWicketsTaken();
                    tvBowlerStats.setText(statsText);
                    tvBowlerEconomy.setText(String.format("Econ: %.2f", stats.getEconomyRate()));
                } else {
                    // Show placeholder when no stats available
                    tvBowlerStats.setText("0-0-0-0");
                    tvBowlerEconomy.setText("Econ: 0.00");
                }
            }
        }
    }
}
