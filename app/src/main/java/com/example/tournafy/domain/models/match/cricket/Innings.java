// Path: app/src/main/java/com/tournafy/domain/models/match/cricket/Innings.java
package com.example.tournafy.domain.models.match.cricket;

/**
 * Domain Model for a cricket Innings.
 * Corresponds to INNINGS in the EERD.
 * This will be a top-level entity, linked to a Match.
 */
public class Innings {

    private String inningsId;
    private String matchId; // FK to Match
    private int inningsNumber; 
    private String battingTeamId; // FK to Team
    private String bowlingTeamId; // FK to Team
    private int totalRuns;
    private int wicketsFallen;
    private int oversCompleted; // Whole overs
    private boolean isCompleted;
    
    // Extras tracking
    private int byes;
    private int legByes;
    private int wides;
    private int noBalls;
    
    // Note: Overs will be stored in a separate 'Overs' collection
    // to avoid document size limits.

    // No-arg constructor for Firestore
    public Innings() {}

    // Getters and Setters
    public String getInningsId() {
        return inningsId;
    }

    public void setInningsId(String inningsId) {
        this.inningsId = inningsId;
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public int getInningsNumber() {
        return inningsNumber;
    }

    public void setInningsNumber(int inningsNumber) {
        this.inningsNumber = inningsNumber;
    }

    public String getBattingTeamId() {
        return battingTeamId;
    }

    public void setBattingTeamId(String battingTeamId) {
        this.battingTeamId = battingTeamId;
    }

    public String getBowlingTeamId() {
        return bowlingTeamId;
    }

    public void setBowlingTeamId(String bowlingTeamId) {
        this.bowlingTeamId = bowlingTeamId;
    }

    public int getTotalRuns() {
        return totalRuns;
    }

    public void setTotalRuns(int totalRuns) {
        this.totalRuns = totalRuns;
    }

    public int getWicketsFallen() {
        return wicketsFallen;
    }

    public void setWicketsFallen(int wicketsFallen) {
        this.wicketsFallen = wicketsFallen;
    }

    public int getOversCompleted() {
        return oversCompleted;
    }

    public void setOversCompleted(int oversCompleted) {
        this.oversCompleted = oversCompleted;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public int getByes() {
        return byes;
    }

    public void setByes(int byes) {
        this.byes = byes;
    }

    public int getLegByes() {
        return legByes;
    }

    public void setLegByes(int legByes) {
        this.legByes = legByes;
    }

    public int getWides() {
        return wides;
    }

    public void setWides(int wides) {
        this.wides = wides;
    }

    public int getNoBalls() {
        return noBalls;
    }

    public void setNoBalls(int noBalls) {
        this.noBalls = noBalls;
    }

    /**
     * Calculates total overs including partial overs
     * @return Total overs as decimal (e.g., 12.3 means 12 overs and 3 balls)
     */
    public double getTotalOvers() {
        return oversCompleted; // For now, returns complete overs only
        // TODO: Add partial over calculation if needed
    }
}