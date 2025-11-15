package com.example.tournafy.domain.models.match.football;

import java.util.UUID;

/**
 * Data model for detailed information about a substitution event.
 * Maps to the FOOTBALL_SUBSTITUTION_DETAIL entity in the EERD.
 * This object is created by the EventDetailFactory.
 */
public class FootballSubstitutionDetail {

    private String substitutionDetailId;
    private String eventId; // FK to FootballEvent
    private String playerOutId; // FK to Player
    private String playerInId; // FK to Player
    private String teamId; // FK to Team
    private int minuteSubstituted;
    private String substitutionReason; // Enum: TACTICAL, INJURY, PERFORMANCE
    private int substitutionNumber; // e.g., 1st, 2nd, 3rd sub

    public FootballSubstitutionDetail() {
        this.substitutionDetailId = UUID.randomUUID().toString();
    }

    // --- Getters and Setters ---

    public String getSubstitutionDetailId() {
        return substitutionDetailId;
    }

    public void setSubstitutionDetailId(String substitutionDetailId) {
        this.substitutionDetailId = substitutionDetailId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getPlayerOutId() {
        return playerOutId;
    }

    public void setPlayerOutId(String playerOutId) {
        this.playerOutId = playerOutId;
    }

    public String getPlayerInId() {
        return playerInId;
    }

    public void setPlayerInId(String playerInId) {
        this.playerInId = playerInId;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public int getMinuteSubstituted() {
        return minuteSubstituted;
    }

    public void setMinuteSubstituted(int minuteSubstituted) {
        this.minuteSubstituted = minuteSubstituted;
    }

    public String getSubstitutionReason() {
        return substitutionReason;
    }

    public void setSubstitutionReason(String substitutionReason) {
        this.substitutionReason = substitutionReason;
    }

    public int getSubstitutionNumber() {
        return substitutionNumber;
    }

    public void setSubstitutionNumber(int substitutionNumber) {
        this.substitutionNumber = substitutionNumber;
    }
}