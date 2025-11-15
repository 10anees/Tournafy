package com.example.tournafy.domain.models.match.cricket;

/**
 * Domain Model for a single Ball in an Over.
 * Corresponds to BALL in the EERD.
 * This will be stored as part of a list within an Over document.
 */
public class Ball {

    private String ballId;
    private int ballNumber; // 1-6 (or more for no-balls)
    private String batsmanId; // FK to Player
    private String bowlerId; // FK to Player
    private int runsScored;
    private boolean isWicket;
    private String extrasType; // e.g., "WIDE", "NO_BALL"
    private String wicketType; // e.g., "BOWLED", "CAUGHT"

    // No-arg constructor for Firestore
    public Ball() {}

    // Getters and Setters
    public String getBallId() {
        return ballId;
    }

    public void setBallId(String ballId) {
        this.ballId = ballId;
    }

    public int getBallNumber() {
        return ballNumber;
    }

    public void setBallNumber(int ballNumber) {
        this.ballNumber = ballNumber;
    }

    public String getBatsmanId() {
        return batsmanId;
    }

    public void setBatsmanId(String batsmanId) {
        this.batsmanId = batsmanId;
    }

    public String getBowlerId() {
        return bowlerId;
    }

    public void setBowlerId(String bowlerId) {
        this.bowlerId = bowlerId;
    }

    public int getRunsScored() {
        return runsScored;
    }

    public void setRunsScored(int runsScored) {
        this.runsScored = runsScored;
    }

    public boolean isWicket() {
        return isWicket;
    }

    public void setWicket(boolean wicket) {
        isWicket = wicket;
    }

    public String getExtrasType() {
        return extrasType;
    }

    public void setExtrasType(String extrasType) {
        this.extrasType = extrasType;
    }

    public String getWicketType() {
        return wicketType;
    }

    public void setWicketType(String wicketType) {
        this.wicketType = wicketType;
    }
}