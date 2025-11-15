package com.example.tournafy.domain.models.team;

/**
 * Maps to the TOURNAMENT_TEAM entity in the EERD.
 * This is a link table that also holds state (stats) for a team
 * in a tournament's group/table.
 */
public class TournamentTeam {

    private String tournamentTeamId;
    private String tournamentId;
    private String teamId;

    // Statistics fields
    private int points;
    private int matchesPlayed;
    private int matchesWon;
    private int matchesLost;
    private int matchesDrawn;
    private int goalsFor;
    private int goalsAgainst;
    private float netRunRate;

    public TournamentTeam() {
    }

    /**
     * @param tournamentId The ID of the tournament.
     * @param teamId       The ID of the participating team.
     */
    public TournamentTeam(String tournamentId, String teamId) {
        this.tournamentId = tournamentId;
        this.teamId = teamId;
        // Initialize all stats to zero
        this.points = 0;
        this.matchesPlayed = 0;
        this.matchesWon = 0;
        this.matchesLost = 0;
        this.matchesDrawn = 0;
        this.goalsFor = 0;
        this.goalsAgainst = 0;
        this.netRunRate = 0.0f;
    }

    // --- Getters and Setters ---

    public String getTournamentTeamId() {
        return tournamentTeamId;
    }

    public void setTournamentTeamId(String tournamentTeamId) {
        this.tournamentTeamId = tournamentTeamId;
    }

    public String getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(String tournamentId) {
        this.tournamentId = tournamentId;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getMatchesPlayed() {
        return matchesPlayed;
    }

    public void setMatchesPlayed(int matchesPlayed) {
        this.matchesPlayed = matchesPlayed;
    }

    public int getMatchesWon() {
        return matchesWon;
    }

    public void setMatchesWon(int matchesWon) {
        this.matchesWon = matchesWon;
    }

    public int getMatchesLost() {
        return matchesLost;
    }

    public void setMatchesLost(int matchesLost) {
        this.matchesLost = matchesLost;
    }

    public int getMatchesDrawn() {
        return matchesDrawn;
    }

    public void setMatchesDrawn(int matchesDrawn) {
        this.matchesDrawn = matchesDrawn;
    }

    public int getGoalsFor() {
        return goalsFor;
    }

    public void setGoalsFor(int goalsFor) {
        this.goalsFor = goalsFor;
    }

    public int getGoalsAgainst() {
        return goalsAgainst;
    }

    public void setGoalsAgainst(int goalsAgainst) {
        this.goalsAgainst = goalsAgainst;
    }

    public float getNetRunRate() {
        return netRunRate;
    }

    public void setNetRunRate(float netRunRate) {
        this.netRunRate = netRunRate;
    }
}