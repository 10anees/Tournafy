package com.example.tournafy.domain.models.team;

import java.util.Date;
import java.util.List;

/**
 * Domain Model for a Team.
 * Corresponds to TEAM in the EERD.
 */
public class Team {

    private String teamId;
    private String teamName;
    private String createdBy; // FK to User
    private Date createdAt;
    
    // Embedded list of players
    private List<Player> players;

    // No-arg constructor for Firestore
    public Team() {}

    // Getters and Setters
    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }
}