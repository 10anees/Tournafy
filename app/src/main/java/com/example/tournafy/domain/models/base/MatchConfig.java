package com.example.tournafy.domain.models.base;

/**
 * Abstract class for Match Configuration
 * Defines the base for sport-specific match rules.
 * This will be extended by CricketMatchConfig and FootballMatchConfig. 
 */
public abstract class MatchConfig {

    protected String configId;
    protected String matchId;

    public MatchConfig() {
    }

    // Getters and Setters
    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }
}