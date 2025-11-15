package com.example.tournafy.domain.models.match.football;

import java.util.UUID;

/**
 * Data model for detailed information about a save event.
 * Maps to the FOOTBALL_SAVE_DETAIL entity in the EERD.
 * This object is created by the EventDetailFactory.
 */
public class FootballSaveDetail {

    private String saveDetailId;
    private String eventId; // FK to FootballEvent
    private String goalkeeperId; // FK to Player
    private String saveType; // Enum: REFLEX, DIVE, CATCH, PUNCH, PARRY
    private boolean isSpectacular;
    private String shotOriginPlayerId; // FK to Player (who took the shot)

    public FootballSaveDetail() {
        this.saveDetailId = UUID.randomUUID().toString();
    }

    // --- Getters and Setters ---

    public String getSaveDetailId() {
        return saveDetailId;
    }

    public void setSaveDetailId(String saveDetailId) {
        this.saveDetailId = saveDetailId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getGoalkeeperId() {
        return goalkeeperId;
    }

    public void setGoalkeeperId(String goalkeeperId) {
        this.goalkeeperId = goalkeeperId;
    }

    public String getSaveType() {
        return saveType;
    }

    public void setSaveType(String saveType) {
        this.saveType = saveType;
    }

    public boolean isSpectacular() {
        return isSpectacular;
    }

    public void setSpectacular(boolean spectacular) {
        isSpectacular = spectacular;
    }

    public String getShotOriginPlayerId() {
        return shotOriginPlayerId;
    }

    public void setShotOriginPlayerId(String shotOriginPlayerId) {
        this.shotOriginPlayerId = shotOriginPlayerId;
    }
}