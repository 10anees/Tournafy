package com.example.tournafy.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tournafy.R;
import com.example.tournafy.domain.models.team.Player;
import com.google.android.material.chip.Chip;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter to display player status (Playing vs Substitute) in live score view.
 */
public class PlayerStatusAdapter extends RecyclerView.Adapter<PlayerStatusAdapter.PlayerStatusViewHolder> {
    
    private List<Player> players = new ArrayList<>();
    
    public void setPlayers(List<Player> players) {
        this.players = players != null ? players : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public PlayerStatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_player_status, parent, false);
        return new PlayerStatusViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull PlayerStatusViewHolder holder, int position) {
        Player player = players.get(position);
        holder.bind(player);
    }
    
    @Override
    public int getItemCount() {
        return players.size();
    }
    
    static class PlayerStatusViewHolder extends RecyclerView.ViewHolder {
        
        private final View viewStatusIndicator;
        private final TextView tvPlayerName;
        private final TextView tvPlayerRole;
        private final Chip chipPlayerStatus;
        
        public PlayerStatusViewHolder(@NonNull View itemView) {
            super(itemView);
            viewStatusIndicator = itemView.findViewById(R.id.viewStatusIndicator);
            tvPlayerName = itemView.findViewById(R.id.tvPlayerName);
            tvPlayerRole = itemView.findViewById(R.id.tvPlayerRole);
            chipPlayerStatus = itemView.findViewById(R.id.chipPlayerStatus);
        }
        
        public void bind(Player player) {
            tvPlayerName.setText(player.getPlayerName());
            tvPlayerRole.setText(player.getRole() != null ? player.getRole() : "Player");
            
            int greenColor = itemView.getContext().getColor(R.color.playing_green);
            int greenBgColor = itemView.getContext().getColor(R.color.playing_green_bg);
            int greenTextColor = itemView.getContext().getColor(R.color.playing_green_text);
            int orangeColor = itemView.getContext().getColor(R.color.substitute_orange);
            int orangeBgColor = itemView.getContext().getColor(R.color.substitute_orange_bg);
            int orangeTextColor = itemView.getContext().getColor(R.color.substitute_orange_text);
            
            if (player.isStartingXI()) {
                // Playing - Green indicator
                viewStatusIndicator.setBackgroundColor(greenColor);
                chipPlayerStatus.setText("Playing");
                chipPlayerStatus.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(greenBgColor));
                chipPlayerStatus.setTextColor(greenTextColor);
                chipPlayerStatus.setChipIconResource(R.drawable.ic_launcher_foreground);
            } else {
                // Substitute - Orange indicator
                viewStatusIndicator.setBackgroundColor(orangeColor);
                chipPlayerStatus.setText("Substitute");
                chipPlayerStatus.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(orangeBgColor));
                chipPlayerStatus.setTextColor(orangeTextColor);
                chipPlayerStatus.setChipIconResource(R.drawable.ic_launcher_foreground);
            }
        }
    }
}
