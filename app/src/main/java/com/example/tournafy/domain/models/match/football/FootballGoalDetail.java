package com.example.tournafy.domain.models.match.football;

import java.util.UUID;

/**
 * Data model for detailed information about a goal event.
 * Maps to the FOOTBALL_GOAL_DETAIL entity in the EERD.
 * This object is created by the EventDetailFactory.
 */
public class FootballGoalDetail {

    private String goalDetailId;
    private String eventId; // FK to FootballEvent
    private String scorerId; // FK to Player
    private String assistPlayerId; // FK to Player (null if no assist)
    private String goalType; // Enum: OPEN_PLAY, PENALTY, FREE_KICK, etc.
    private String bodyPart; // Enum: LEFT_FOOT, RIGHT_FOOT, HEAD, etc.
    private boolean isPenalty;
    private boolean isOwnGoal;
    private int minuteScored;
    private String goalDescription;

    public FootballGoalDetail() {
        this.goalDetailId = UUID.randomUUID().toString();
    }

    // --- Getters and Setters ---

    public String getGoalDetailId() {
        return goalDetailId;
    }

    public void setGoalDetailId(String goalDetailId) {
        this.goalDetailId = goalDetailId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getScorerId() {
        return scorerId;
    }

    public void setScorerId(String scorerId) {
        this.scorerId = scorerId;
    }

    public String getAssistPlayerId() {
        return assistPlayerId;
    }

    public void setAssistPlayerId(String assistPlayerId) {
        this.assistPlayerId = assistPlayerId;
    }

    public String getGoalType() {
        return goalType;
    }

    public void setGoalType(String goalType) {
        this.goalType = goalType;
    }

    public String getBodyPart() {
        return bodyPart;
    }

    public void setBodyPart(String bodyPart) {
        this.bodyPart = bodyPart;
    }

    public boolean isPenalty() {
        return isPenalty;
    }

    public void setPenalty(boolean penalty) {
        isPenalty = penalty;
    }

    public boolean isOwnGoal() {
        return isOwnGoal;
    }

    public void setOwnGoal(boolean ownGoal) {
        isOwnGoal = ownGoal;
    }

    public int getMinuteScored() {
        return minuteScored;
    }

    public void setMinuteScored(int minuteScored) {
        this.minuteScored = minuteScored;
    }

    public String getGoalDescription() {
        return goalDescription;
    }

    public void setGoalDescription(String goalDescription) {
        this.goalDescription = goalDescription;
    }
}