package com.example.tournafy.domain.models.base;

import java.util.Date;

/**
 * Abstract class for a Match Event 
 * Defines common properties for any in-game event.
 * This will be extended by CricketEvent and FootballEvent.
 * Maps to the MATCH_EVENT table in the EERD. 
 */
public abstract class MatchEvent {

    protected String eventId;
    protected String matchId;
    protected String eventType; // e.g., "BALL", "GOAL", "CARD"
    protected Date eventTime;
    protected String teamId;
    protected String playerId; // Primary player involved
    protected String description;

    public MatchEvent() {
        this.eventTime = new Date();
    }

    // Getters and Setters
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Date getEventTime() {
        return eventTime;
    }

    public void setEventTime(Date eventTime) {
        this.eventTime = eventTime;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}