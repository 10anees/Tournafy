package com.example.tournafy.domain.models.team;

import java.util.UUID;

/**
 * Represents the linking table between a Match and a Team.
 * It holds match-specific information for that team, like score.
 * Maps to the MATCH_TEAM entity in the EERD.
 */
public class MatchTeam {

    private String matchTeamId;
    private String matchId; // FK to Match
    private String teamId; // FK to Team

    // Added to fix compilation error in FootballLiveScoreFragment
    private String teamName;

    private boolean isHomeTeam;
    private int score;

    public MatchTeam() {
        this.matchTeamId = UUID.randomUUID().toString();
        this.score = 0; // Default score
    }

    // --- Getters and Setters ---

    public String getMatchTeamId() {
        return matchTeamId;
    }

    public void setMatchTeamId(String matchTeamId) {
        this.matchTeamId = matchTeamId;
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    // FIX: Added this method required by FootballLiveScoreFragment
    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public boolean isHomeTeam() {
        return isHomeTeam;
    }

    public void setHomeTeam(boolean homeTeam) {
        isHomeTeam = homeTeam;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}