package com.example.tournafy.ui.adapters.tournament;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tournafy.R;
import com.example.tournafy.domain.models.base.Match;
import com.example.tournafy.domain.models.match.cricket.CricketMatch;
import com.example.tournafy.domain.models.match.football.FootballMatch;
import com.example.tournafy.domain.models.team.MatchTeam;
import com.example.tournafy.domain.models.tournament.TournamentMatch;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying tournament matches with actual Match data.
 * Shows match details including teams, scores, venue, and status.
 */
public class TournamentMatchAdapter extends RecyclerView.Adapter<TournamentMatchAdapter.ViewHolder> {

    // Helper class to pair TournamentMatch with Match
    public static class TournamentMatchWithDetails {
        public final TournamentMatch tournamentMatch;
        public final Match match;
        
        public TournamentMatchWithDetails(TournamentMatch tournamentMatch, Match match) {
            this.tournamentMatch = tournamentMatch;
            this.match = match;
        }
    }

    private final OnMatchClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
    private List<TournamentMatchWithDetails> matchesWithDetails = new ArrayList<>();

    public interface OnMatchClickListener {
        void onMatchClick(TournamentMatch tournamentMatch, Match match);
    }

    public TournamentMatchAdapter(OnMatchClickListener listener) {
        this.listener = listener;
    }

    public void setMatchesWithDetails(List<TournamentMatchWithDetails> matchesWithDetails) {
        this.matchesWithDetails = matchesWithDetails != null ? matchesWithDetails : new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return matchesWithDetails.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tournament_match, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TournamentMatchWithDetails data = matchesWithDetails.get(position);
        holder.bind(data.tournamentMatch, data.match, listener, dateFormat);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final TextView tvMatchNumber;
        private final TextView tvTeamA;
        private final TextView tvTeamB;
        private final TextView tvScoreA;
        private final TextView tvScoreB;
        private final TextView tvVenue;
        private final TextView tvDateTime;
        private final Chip chipStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardMatch);
            tvMatchNumber = itemView.findViewById(R.id.tvMatchNumber);
            tvTeamA = itemView.findViewById(R.id.tvTeamA);
            tvTeamB = itemView.findViewById(R.id.tvTeamB);
            tvScoreA = itemView.findViewById(R.id.tvScoreA);
            tvScoreB = itemView.findViewById(R.id.tvScoreB);
            tvVenue = itemView.findViewById(R.id.tvVenue);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            chipStatus = itemView.findViewById(R.id.chipStatus);
        }

        public void bind(TournamentMatch tournamentMatch, Match match, OnMatchClickListener listener, SimpleDateFormat dateFormat) {
            tvMatchNumber.setText("Match " + tournamentMatch.getMatchOrder());
            
            if (match != null) {
                // Extract team names based on match type
                if (match instanceof CricketMatch) {
                    CricketMatch cricketMatch = (CricketMatch) match;
                    List<MatchTeam> teams = cricketMatch.getTeams();
                    if (teams != null && teams.size() >= 2) {
                        tvTeamA.setText(teams.get(0).getTeamName() != null ? teams.get(0).getTeamName() : "Team A");
                        tvTeamB.setText(teams.get(1).getTeamName() != null ? teams.get(1).getTeamName() : "Team B");
                    } else {
                        tvTeamA.setText("Team A");
                        tvTeamB.setText("Team B");
                    }
                } else if (match instanceof FootballMatch) {
                    FootballMatch footballMatch = (FootballMatch) match;
                    List<MatchTeam> teams = footballMatch.getTeams();
                    if (teams != null && teams.size() >= 2) {
                        tvTeamA.setText(teams.get(0).getTeamName() != null ? teams.get(0).getTeamName() : "Team A");
                        tvTeamB.setText(teams.get(1).getTeamName() != null ? teams.get(1).getTeamName() : "Team B");
                    } else {
                        tvTeamA.setText("Team A");
                        tvTeamB.setText("Team B");
                    }
                } else {
                    tvTeamA.setText("Team A");
                    tvTeamB.setText("Team B");
                }

                // Extract and display scores based on match type
                displayScores(match);

                // Venue
                if (match.getVenue() != null && !match.getVenue().isEmpty()) {
                    tvVenue.setText(match.getVenue());
                } else {
                    tvVenue.setText("Venue TBD");
                }

                // Date and time
                if (match.getMatchDate() != null) {
                    tvDateTime.setText(dateFormat.format(match.getMatchDate()));
                } else {
                    tvDateTime.setText("Time TBD");
                }

                // Status
                updateStatusChip(chipStatus, match.getMatchStatus());
            } else {
                // Fallback for missing match data
                tvTeamA.setText("Loading...");
                tvTeamB.setText("Loading...");
                tvScoreA.setVisibility(View.GONE);
                tvScoreB.setVisibility(View.GONE);
                tvVenue.setText("Venue TBD");
                tvDateTime.setText("Time TBD");
                updateStatusChip(chipStatus, "SCHEDULED");
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMatchClick(tournamentMatch, match);
                }
            });
        }

        private void displayScores(Match match) {
            if (match instanceof CricketMatch) {
                CricketMatch cricketMatch = (CricketMatch) match;
                // Display cricket scores
                // Format: TeamA: Runs/Wickets (Overs)
                // For simplicity, show placeholder if no innings data
                tvScoreA.setVisibility(View.VISIBLE);
                tvScoreB.setVisibility(View.VISIBLE);
                tvScoreA.setText("0/0 (0)");
                tvScoreB.setText("0/0 (0)");
                // TODO: Extract from innings data when needed
            } else if (match instanceof FootballMatch) {
                FootballMatch footballMatch = (FootballMatch) match;
                // Display football scores
                int homeScore = footballMatch.getHomeScore();
                int awayScore = footballMatch.getAwayScore();
                
                if (match.getMatchStatus() != null && 
                    (match.getMatchStatus().equals("COMPLETED") || 
                     match.getMatchStatus().equals("LIVE") ||
                     match.getMatchStatus().equals("IN_PROGRESS"))) {
                    tvScoreA.setVisibility(View.VISIBLE);
                    tvScoreB.setVisibility(View.VISIBLE);
                    tvScoreA.setText(String.valueOf(homeScore));
                    tvScoreB.setText(String.valueOf(awayScore));
                } else {
                    tvScoreA.setVisibility(View.GONE);
                    tvScoreB.setVisibility(View.GONE);
                }
            } else {
                // Unknown match type
                tvScoreA.setVisibility(View.GONE);
                tvScoreB.setVisibility(View.GONE);
            }
        }

        private void updateStatusChip(Chip chip, String status) {
            if (status == null) {
                chip.setText("Scheduled");
                chip.setChipBackgroundColorResource(R.color.status_scheduled);
                return;
            }

            switch (status.toUpperCase()) {
                case "SCHEDULED":
                    chip.setText("Scheduled");
                    chip.setChipBackgroundColorResource(R.color.status_scheduled);
                    break;
                case "LIVE":
                case "IN_PROGRESS":
                    chip.setText("Live");
                    chip.setChipBackgroundColorResource(R.color.status_live);
                    break;
                case "COMPLETED":
                    chip.setText("Completed");
                    chip.setChipBackgroundColorResource(R.color.status_completed);
                    break;
                default:
                    chip.setText(status);
                    chip.setChipBackgroundColorResource(R.color.status_draft);
            }
        }
    }

}
