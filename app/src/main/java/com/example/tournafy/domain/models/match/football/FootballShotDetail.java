package com.example.tournafy.domain.models.match.football;

import java.util.UUID;

/**
 * Data model for detailed information about a shot event.
 * Maps to the FOOTBALL_SHOT_DETAIL entity in the EERD.
 * This object is created by the EventDetailFactory.
 */
public class FootballShotDetail {

    private String shotDetailId;
    private String eventId; // FK to FootballEvent
    private String playerId; // FK to Player
    private String shotType; // Enum: ON_TARGET, OFF_TARGET, BLOCKED, SAVED
    private String shotMethod; // Enum: SHOT, HEADER, VOLLEY
    private boolean isBigChance;
    private double xGValue; // expected goals value

    public FootballShotDetail() {
        this.shotDetailId = UUID.randomUUID().toString();
        this.xGValue = 0.0;
    }

    // --- Getters and Setters ---

    public String getShotDetailId() {
        return shotDetailId;
    }

    public void setShotDetailId(String shotDetailId) {
        this.shotDetailId = shotDetailId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getShotType() {
        return shotType;
    }

    public void setShotType(String shotType) {
        this.shotType = shotType;
    }

    public String getShotMethod() {
        return shotMethod;
    }

    public void setShotMethod(String shotMethod) {
        this.shotMethod = shotMethod;
    }

    public boolean isBigChance() {
        return isBigChance;
    }

    public void setBigChance(boolean bigChance) {
        isBigChance = bigChance;
    }

    public double getXGValue() {
        return xGValue;
    }

    public void setXGValue(double xGValue) {
        this.xGValue = xGValue;
    }
}