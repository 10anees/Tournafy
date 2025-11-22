package com.example.tournafy.ui.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tournafy.R;
import com.example.tournafy.domain.enums.football.EventCategory;
import com.example.tournafy.domain.enums.football.GoalType;
import com.example.tournafy.domain.enums.football.CardType;
import com.example.tournafy.domain.models.match.football.FootballEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for displaying football match events in a timeline.
 * Shows chronological list of goals, cards, and substitutions with visual indicators.
 * 
 * Features:
 * - Event type icons (⚽ goal, 🟨 yellow card, 🟥 red card, 🔄 substitution)
 * - Minute badges with period indicators
 * - Team colors and badges
 * - Detailed event information (player names, goal types, etc.)
 * 
 * @author Tournafy Team
 * @version 1.0
 */
public class FootballEventAdapter extends RecyclerView.Adapter<FootballEventAdapter.EventViewHolder> {

    private List<FootballEvent> events = new ArrayList<>();
    private String homeTeamName = "Home";
    private String awayTeamName = "Away";
    private String homeTeamId;
    private String awayTeamId;

    /**
     * Sets the list of events to display in the timeline.
     * Events should be sorted chronologically (oldest to newest).
     * 
     * @param events List of FootballEvent objects
     */
    public void setEvents(List<FootballEvent> events) {
        this.events = events != null ? new ArrayList<>(events) : new ArrayList<>();
        android.util.Log.d("FootballEventAdapter", "setEvents called with " + this.events.size() + " events");
        notifyDataSetChanged();
    }

    /**
     * Sets the team names and IDs for display in event details.
     * 
     * @param homeTeam Name of the home team
     * @param awayTeam Name of the away team
     * @param homeId ID of the home team
     * @param awayId ID of the away team
     */
    public void setTeamNames(String homeTeam, String awayTeam, String homeId, String awayId) {
        this.homeTeamName = homeTeam;
        this.awayTeamName = awayTeam;
        this.homeTeamId = homeId;
        this.awayTeamId = awayId;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_football_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        FootballEvent event = events.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    /**
     * ViewHolder for individual event items in the timeline.
     */
    class EventViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMinute;
        private final ImageView ivEventIcon;
        private final TextView tvEventTitle;
        private final TextView tvEventDetails;
        private final TextView tvTeamBadge;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMinute = itemView.findViewById(R.id.tvMinute);
            ivEventIcon = itemView.findViewById(R.id.ivEventIcon);
            tvEventTitle = itemView.findViewById(R.id.tvEventTitle);
            tvEventDetails = itemView.findViewById(R.id.tvEventDetails);
            tvTeamBadge = itemView.findViewById(R.id.tvTeamBadge);
        }

        /**
         * Binds a FootballEvent to the view holder UI components.
         * Configures icons, colors, and text based on event type and details.
         * 
         * @param event The FootballEvent to display
         */
        public void bind(FootballEvent event) {
            android.util.Log.d("FootballEventAdapter", "Binding event: " + event.getEventCategory() + " at minute " + event.getMatchMinute());
            
            // Set minute
            String minute = event.getMatchMinute() + "'";
            tvMinute.setText(minute);
            
            // Parse event category
            EventCategory category = EventCategory.GOAL; // Default
            try {
                if (event.getEventCategory() != null) {
                    category = EventCategory.valueOf(event.getEventCategory());
                }
            } catch (IllegalArgumentException e) {
                // Use default if invalid
            }
            
            // Set minute badge color based on event category
            int badgeColor = Color.parseColor("#6200EE"); // Default purple
            switch (category) {
                case GOAL:
                    badgeColor = Color.parseColor("#2E7D32"); // Green
                    break;
                case CARD:
                    // Check if yellow or red card from String
                    if (event.getCardDetail() != null && "RED".equals(event.getCardDetail().getCardType())) {
                        badgeColor = Color.parseColor("#C62828"); // Red
                    } else {
                        badgeColor = Color.parseColor("#FBC02D"); // Yellow
                    }
                    break;
                case SUBSTITUTION:
                    badgeColor = Color.parseColor("#1565C0"); // Blue
                    break;
            }
            tvMinute.setBackgroundColor(badgeColor);

            // Set event icon (using placeholder - replace with actual icons)
            if (ivEventIcon != null) {
                ivEventIcon.setImageResource(R.drawable.ic_launcher_foreground);
                ivEventIcon.setColorFilter(badgeColor);
            }

            // Build event title and details
            String title = "";
            String details = "";
            String teamBadge = "";
            int teamColor = Color.GRAY;

            // Determine if home or away team by comparing event's teamId with stored team IDs
            boolean isHomeTeam = (homeTeamId != null && homeTeamId.equals(event.getTeamId()));
            String teamName = isHomeTeam ? homeTeamName : awayTeamName;
            teamBadge = isHomeTeam ? "HOME" : "AWAY";
            teamColor = isHomeTeam ? Color.parseColor("#2E7D32") : Color.parseColor("#1565C0");
            
            // Also show the score at the time of the event
            String scoreAtEvent = event.getHomeScoreAtEvent() + " - " + event.getAwayScoreAtEvent();

            switch (category) {
                case GOAL:
                    title = "⚽ GOAL";
                    if (event.getGoalDetail() != null && event.getGoalDetail().getGoalType() != null) {
                        try {
                            GoalType goalType = GoalType.valueOf(event.getGoalDetail().getGoalType());
                            title += " - " + formatGoalType(goalType);
                        } catch (IllegalArgumentException e) {
                            // Invalid goal type, just show GOAL
                        }
                    }
                    
                    // Build detailed description with scorer and assister
                    if (event.getDescription() != null) {
                        // Description format: "Scorer (Assist: Assister)" or just "Scorer"
                        details = event.getDescription() + "\n" + teamName + " • " + scoreAtEvent;
                    } else {
                        details = "Goal scored • " + teamName + " • " + scoreAtEvent;
                    }
                    break;

                case CARD:
                    if (event.getCardDetail() != null && "RED".equals(event.getCardDetail().getCardType())) {
                        title = "🟥 RED CARD";
                        teamColor = Color.parseColor("#C62828"); // Red for red card
                    } else {
                        title = "🟨 YELLOW CARD";
                        teamColor = Color.parseColor("#F9A825"); // Yellow-ish for yellow card
                    }
                    
                    // Build description with player and reason
                    StringBuilder cardDetails = new StringBuilder();
                    if (event.getDescription() != null) {
                        cardDetails.append(event.getDescription());
                    } else {
                        cardDetails.append("Card issued");
                    }
                    
                    // Add card reason if available
                    if (event.getCardDetail() != null && event.getCardDetail().getCardReason() != null) {
                        cardDetails.append(" (").append(formatCardReason(event.getCardDetail().getCardReason())).append(")");
                    }
                    cardDetails.append("\n").append(teamName);
                    details = cardDetails.toString();
                    break;

                case SUBSTITUTION:
                    title = "🔄 SUBSTITUTION";
                    
                    // Description format: "PlayerOut → PlayerIn"
                    if (event.getDescription() != null) {
                        // Make it more readable with labels
                        String subDesc = event.getDescription();
                        if (subDesc.contains("→")) {
                            String[] parts = subDesc.split("→");
                            if (parts.length == 2) {
                                details = "OUT: " + parts[0].trim() + "\nIN: " + parts[1].trim() + " • " + teamName;
                            } else {
                                details = subDesc + "\n" + teamName;
                            }
                        } else {
                            details = subDesc + "\n" + teamName;
                        }
                    } else {
                        details = "Substitution made\n" + teamName;
                    }
                    break;

                default:
                    title = category.name();
                    details = event.getDescription() != null ? event.getDescription() : teamName;
                    break;
            }

            tvEventTitle.setText(title);
            tvEventDetails.setText(details);
            tvTeamBadge.setText(teamBadge);
            tvTeamBadge.setTextColor(teamColor);
        }

        /**
         * Formats a GoalType enum into a human-readable string.
         * 
         * @param goalType The GoalType to format
         * @return Formatted string (e.g., "Open Play", "Penalty")
         */
        private String formatGoalType(GoalType goalType) {
            switch (goalType) {
                case OPEN_PLAY: return "Open Play";
                case PENALTY: return "Penalty";
                case FREE_KICK: return "Free Kick";
                case HEADER: return "Header";
                case OWN_GOAL: return "Own Goal";
                default: return goalType.name();
            }
        }
        
        /**
         * Formats a card reason into a human-readable string.
         * 
         * @param reason The card reason string
         * @return Formatted string (e.g., "Foul", "Dissent")
         */
        private String formatCardReason(String reason) {
            if (reason == null) return "";
            
            switch (reason.toUpperCase()) {
                case "FOUL": return "Foul";
                case "DISSENT": return "Dissent";
                case "SIMULATION": return "Simulation";
                case "TIMEWASTING": return "Time Wasting";
                case "UNSPORTING": return "Unsporting Behavior";
                case "HANDBALL": return "Handball";
                case "VIOLENT_CONDUCT": return "Violent Conduct";
                case "DANGEROUS_PLAY": return "Dangerous Play";
                default: return reason.substring(0, 1).toUpperCase() + reason.substring(1).toLowerCase();
            }
        }
    }
}
