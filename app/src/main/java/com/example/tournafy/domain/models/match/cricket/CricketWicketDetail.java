package com.example.tournafy.domain.models.match.cricket;

import java.util.UUID;

/**
 * Data model for detailed information about a wicket event.
 * Maps to the CRICKET_WICKET_DETAIL entity in the EERD.
 * This object is created by the EventDetailFactory.
 */
public class CricketWicketDetail {

    private String wicketDetailId;
    private String eventId; // FK to CricketEvent
    private String dismissedBatsmanId; // FK to Player
    private String bowlerId; // FK to Player
    private String fielderId; // FK to Player (null if not applicable)
    private String wicketType; // Enum: BOWLED, CAUGHT, LBW, etc.
    private String wicketDescription;
    private int teamScoreAtWicket;
    private int teamWicketsFallen;

    public CricketWicketDetail() {
        this.wicketDetailId = UUID.randomUUID().toString();
    }

    // --- Getters and Setters ---

    public String getWicketDetailId() {
        return wicketDetailId;
    }

    public void setWicketDetailId(String wicketDetailId) {
        this.wicketDetailId = wicketDetailId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getDismissedBatsmanId() {
        return dismissedBatsmanId;
    }

    public void setDismissedBatsmanId(String dismissedBatsmanId) {
        this.dismissedBatsmanId = dismissedBatsmanId;
    }

    public String getBowlerId() {
        return bowlerId;
    }

    public void setBowlerId(String bowlerId) {
        this.bowlerId = bowlerId;
    }

    public String getFielderId() {
        return fielderId;
    }

    public void setFielderId(String fielderId) {
        this.fielderId = fielderId;
    }

    public String getWicketType() {
        return wicketType;
    }

    public void setWicketType(String wicketType) {
        this.wicketType = wicketType;
    }

    public String getWicketDescription() {
        return wicketDescription;
    }

    public void setWicketDescription(String wicketDescription) {
        this.wicketDescription = wicketDescription;
    }

    public int getTeamScoreAtWicket() {
        return teamScoreAtWicket;
    }

    public void setTeamScoreAtWicket(int teamScoreAtWicket) {
        this.teamScoreAtWicket = teamScoreAtWicket;
    }

    public int getTeamWicketsFallen() {
        return teamWicketsFallen;
    }

    public void setTeamWicketsFallen(int teamWicketsFallen) {
        this.teamWicketsFallen = teamWicketsFallen;
    }
}