package com.example.tournafy.domain.models.match.cricket;

import java.util.List;

/**
 * Domain Model for a cricket Over.
 * Corresponds to OVER in the EERD.
 * This will be a top-level entity, linked to an Innings.
 */
public class Over {

    private String overId;
    private String inningsId; // FK to Innings
    private int overNumber; // 1, 2, 3...
    private String bowlerId; // FK to Player
    private int runsInOver;
    private int wicketsInOver;
    private boolean isCompleted;
    
    // Embedded list of all balls delivered in this over
    private List<Ball> balls;

    // No-arg constructor for Firestore
    public Over() {}

    // Getters and Setters
    public String getOverId() {
        return overId;
    }

    public void setOverId(String overId) {
        this.overId = overId;
    }

    public String getInningsId() {
        return inningsId;
    }

    public void setInningsId(String inningsId) {
        this.inningsId = inningsId;
    }

    public int getOverNumber() {
        return overNumber;
    }

    public void setOverNumber(int overNumber) {
        this.overNumber = overNumber;
    }

    public String getBowlerId() {
        return bowlerId;
    }

    public void setBowlerId(String bowlerId) {
        this.bowlerId = bowlerId;
    }

    public int getRunsInOver() {
        return runsInOver;
    }

    public void setRunsInOver(int runsInOver) {
        this.runsInOver = runsInOver;
    }

    public int getWicketsInOver() {
        return wicketsInOver;
    }

    public void setWicketsInOver(int wicketsInOver) {
        this.wicketsInOver = wicketsInOver;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public List<Ball> getBalls() {
        return balls;
    }

    public void setBalls(List<Ball> balls) {
        this.balls = balls;
    }
}