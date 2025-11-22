package com.example.tournafy.domain.models.team;

/**
 * Domain Model for a Player.
 * Corresponds to PLAYER in the EERD.
 * This object will be stored as part of a list within a Team document.
 */
public class Player {

    private String playerId;
    private String teamId; // Foreign key back to the team
    private String playerName;
    private String role; // e.g., "Batsman", "Bowler", "Goalkeeper"
    private int jerseyNumber;
    private boolean isStartingXI; // true if player is in starting lineup, false if substitute

    // No-arg constructor for Firestore
    public Player() {}

    // Getters and Setters
    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getJerseyNumber() {
        return jerseyNumber;
    }

    public void setJerseyNumber(int jerseyNumber) {
        this.jerseyNumber = jerseyNumber;
    }

    public boolean isStartingXI() {
        return isStartingXI;
    }

    public void setStartingXI(boolean startingXI) {
        isStartingXI = startingXI;
    }
}