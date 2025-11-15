package com.example.tournafy.domain.models.match.football;

import java.util.UUID;

/**
 * Data model for detailed information about a card event (Yellow/Red).
 * Maps to the FOOTBALL_CARD_DETAIL entity in the EERD.
 * This object is created by the EventDetailFactory.
 */
public class FootballCardDetail {

    private String cardDetailId;
    private String eventId; // FK to FootballEvent
    private String playerId; // FK to Player
    private String cardType; // Enum: YELLOW, RED, YELLOW_TO_RED
    private String cardReason; // Enum: FOUL, DISSENT, TIME_WASTING, etc.
    private boolean isSecondYellow;
    private int minuteIssued;
    private String cardDescription;

    public FootballCardDetail() {
        this.cardDetailId = UUID.randomUUID().toString();
    }

    // --- Getters and Setters ---

    public String getCardDetailId() {
        return cardDetailId;
    }

    public void setCardDetailId(String cardDetailId) {
        this.cardDetailId = cardDetailId;
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

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getCardReason() {
        return cardReason;
    }

    public void setCardReason(String cardReason) {
        this.cardReason = cardReason;
    }

    public boolean isSecondYellow() {
        return isSecondYellow;
    }

    public void setSecondYellow(boolean secondYellow) {
        isSecondYellow = secondYellow;
    }

    public int getMinuteIssued() {
        return minuteIssued;
    }

    public void setMinuteIssued(int minuteIssued) {
        this.minuteIssued = minuteIssued;
    }

    public String getCardDescription() {
        return cardDescription;
    }

    public void setCardDescription(String cardDescription) {
        this.cardDescription = cardDescription;
    }
}