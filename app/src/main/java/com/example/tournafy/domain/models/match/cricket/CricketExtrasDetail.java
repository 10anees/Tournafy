package com.example.tournafy.domain.models.match.cricket;

import java.util.UUID;

/**
 * Data model for detailed information about an extras event.
 * Maps to the CRICKET_EXTRAS_DETAIL entity in the EERD.
 * This object is created by the EventDetailFactory.
 */
public class CricketExtrasDetail {

    private String extrasDetailId;
    private String eventId; // FK to CricketEvent
    private String extrasCategory; // Enum: WIDE, NO_BALL, BYE, LEG_BYE, PENALTY
    private int extrasRuns;
    private boolean runsAlsoScored; // e.g., 4 byes

    public CricketExtrasDetail() {
        this.extrasDetailId = UUID.randomUUID().toString();
    }

    // --- Getters and Setters ---

    public String getExtrasDetailId() {
        return extrasDetailId;
    }

    public void setExtrasDetailId(String extrasDetailId) {
        this.extrasDetailId = extrasDetailId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getExtrasCategory() {
        return extrasCategory;
    }

    public void setExtrasCategory(String extrasCategory) {
        this.extrasCategory = extrasCategory;
    }

    public int getExtrasRuns() {
        return extrasRuns;
    }

    public void setExtrasRuns(int extrasRuns) {
        this.extrasRuns = extrasRuns;
    }

    public boolean isRunsAlsoScored() {
        return runsAlsoScored;
    }

    public void setRunsAlsoScored(boolean runsAlsoScored) {
        this.runsAlsoScored = runsAlsoScored;
    }
}