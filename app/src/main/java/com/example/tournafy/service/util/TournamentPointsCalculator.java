package com.example.tournafy.service.util;

import com.example.tournafy.domain.models.base.Match;
import com.example.tournafy.domain.models.match.cricket.CricketMatch;
import com.example.tournafy.domain.models.match.football.FootballMatch;
import com.example.tournafy.domain.models.team.TournamentTeam;

/**
 * Utility class for calculating tournament points and statistics.
 * Handles both Cricket (NRR) and Football (Goal Difference) calculations.
 */
public class TournamentPointsCalculator {

    // Cricket points system
    private static final int CRICKET_WIN_POINTS = 2;
    private static final int CRICKET_TIE_POINTS = 1;
    private static final int CRICKET_NO_RESULT_POINTS = 1;
    private static final int CRICKET_LOSS_POINTS = 0;

    // Football points system
    private static final int FOOTBALL_WIN_POINTS = 3;
    private static final int FOOTBALL_DRAW_POINTS = 1;
    private static final int FOOTBALL_LOSS_POINTS = 0;

    /**
     * Update tournament team stats after a match completion
     * @param team The tournament team to update
     * @param match The completed match
     * @param isWinner Whether this team won the match
     * @param isDraw Whether the match was a draw
     */
    public static void updateTeamStats(TournamentTeam team, Match match, boolean isWinner, boolean isDraw) {
        if (match == null || team == null) {
            return;
        }

        // Increment matches played
        team.setMatchesPlayed(team.getMatchesPlayed() + 1);

        if (match instanceof CricketMatch) {
            updateCricketStats(team, (CricketMatch) match, isWinner, isDraw);
        } else if (match instanceof FootballMatch) {
            updateFootballStats(team, (FootballMatch) match, isWinner, isDraw);
        }
    }

    /**
     * Update cricket-specific stats including NRR
     */
    private static void updateCricketStats(TournamentTeam team, CricketMatch match, boolean isWinner, boolean isDraw) {
        // Update win/loss/draw counts
        if (isWinner) {
            team.setMatchesWon(team.getMatchesWon() + 1);
            team.setPoints(team.getPoints() + CRICKET_WIN_POINTS);
        } else if (isDraw) {
            team.setMatchesDrawn(team.getMatchesDrawn() + 1);
            team.setPoints(team.getPoints() + CRICKET_TIE_POINTS);
        } else {
            team.setMatchesLost(team.getMatchesLost() + 1);
            team.setPoints(team.getPoints() + CRICKET_LOSS_POINTS);
        }

        // Calculate and update NRR
        // NRR = (Total runs scored / Total overs faced) - (Total runs conceded / Total overs bowled)
        // This is a simplified version - full implementation would track cumulative stats
        float currentNRR = team.getNetRunRate();
        // NRR calculation would require access to innings data
        // For now, we'll keep the existing NRR
        team.setNetRunRate(currentNRR);
    }

    /**
     * Update football-specific stats including goal difference
     */
    private static void updateFootballStats(TournamentTeam team, FootballMatch match, boolean isWinner, boolean isDraw) {
        // Get team's score from match
        // This requires looking up which MatchTeam corresponds to this team
        // Simplified version:
        
        if (isWinner) {
            team.setMatchesWon(team.getMatchesWon() + 1);
            team.setPoints(team.getPoints() + FOOTBALL_WIN_POINTS);
        } else if (isDraw) {
            team.setMatchesDrawn(team.getMatchesDrawn() + 1);
            team.setPoints(team.getPoints() + FOOTBALL_DRAW_POINTS);
        } else {
            team.setMatchesLost(team.getMatchesLost() + 1);
            team.setPoints(team.getPoints() + FOOTBALL_LOSS_POINTS);
        }

        // Update goals for/against and calculate goal difference
        // This would require actual match score data
        // Goal Difference = Goals For - Goals Against
    }

    /**
     * Calculate Net Run Rate for cricket
     * @param runsScored Total runs scored by team
     * @param oversFaced Total overs faced by team
     * @param runsConceded Total runs conceded by team
     * @param oversBowled Total overs bowled by team
     * @return Calculated NRR
     */
    public static float calculateNRR(int runsScored, float oversFaced, int runsConceded, float oversBowled) {
        if (oversFaced == 0 || oversBowled == 0) {
            return 0.0f;
        }
        
        float runRate = (float) runsScored / oversFaced;
        float concededRate = (float) runsConceded / oversBowled;
        
        return runRate - concededRate;
    }

    /**
     * Calculate Goal Difference for football
     */
    public static int calculateGoalDifference(int goalsFor, int goalsAgainst) {
        return goalsFor - goalsAgainst;
    }

    /**
     * Apply bonus points (optional, configurable)
     * e.g., bonus point for scoring 4+ tries in rugby, or batting bonus points in cricket
     */
    public static int applyBonusPoints(TournamentTeam team, Match match, int bonusPoints) {
        team.setPoints(team.getPoints() + bonusPoints);
        return team.getPoints();
    }

    /**
     * Sort teams by standings (points, then NRR/GD)
     * Returns comparison result for use in Comparator
     */
    public static int compareTeams(TournamentTeam t1, TournamentTeam t2) {
        // First compare by points
        int pointsCompare = Integer.compare(t2.getPoints(), t1.getPoints());
        if (pointsCompare != 0) {
            return pointsCompare;
        }
        
        // Then by NRR/Goal Difference
        return Float.compare(t2.getNetRunRate(), t1.getNetRunRate());
    }

    /**
     * Check if two teams are tied on points and NRR/GD
     */
    public static boolean areTeamsTied(TournamentTeam t1, TournamentTeam t2) {
        return t1.getPoints() == t2.getPoints() && 
               Float.compare(t1.getNetRunRate(), t2.getNetRunRate()) == 0;
    }

    /**
     * Calculate win percentage
     */
    public static float calculateWinPercentage(TournamentTeam team) {
        if (team.getMatchesPlayed() == 0) {
            return 0.0f;
        }
        return ((float) team.getMatchesWon() / team.getMatchesPlayed()) * 100;
    }
}
