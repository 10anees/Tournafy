package com.example.tournafy.domain.models.statistics;

import java.util.Date;
import java.util.Map;

/**
 * Domain Model for Player Statistics.
 * Corresponds to PLAYER_STATISTICS in the EERD.
 * This stores aggregated stats for a player within a specific entity (Match, Tournament, or Series).
 */
public class PlayerStatistics {

    private String statId;
    private String playerId; // FK to Player
    private String entityId; // FK to Match, Tournament, or Series
    private String entityType; // "MATCH", "TOURNAMENT", "SERIES"
    
    // Using Map for flexible JSON-like storage
    private Map<String, Object> cricketStats;
    private Map<String, Object> footballStats;
    
    private Date lastUpdated;

    // No-arg constructor for Firestore
    public PlayerStatistics() {}

    public PlayerStatistics(String playerId, String entityId) {
        this.playerId = playerId;
        this.entityId = entityId;
    }

    // Getters and Setters
    public String getStatId() {
        return statId;
    }

    public void setStatId(String statId) {
        this.statId = statId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Map<String, Object> getCricketStats() {
        return cricketStats;
    }

    public void setCricketStats(Map<String, Object> cricketStats) {
        this.cricketStats = cricketStats;
    }

    public Map<String, Object> getFootballStats() {
        return footballStats;
    }

    public void setFootballStats(Map<String, Object> footballStats) {
        this.footballStats = footballStats;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}