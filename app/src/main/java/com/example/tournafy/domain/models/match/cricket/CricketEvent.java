package com.example.tournafy.domain.models.match.cricket;

import com.example.tournafy.domain.models.base.MatchEvent;

/**
 * Concrete event model for a Cricket match.
 * Extends the abstract MatchEvent.
 * This class holds all event-related data specific to cricket.
 */
public class CricketEvent extends MatchEvent {

    // Fields are based on the EERD and file structure
    private int overNumber;
    private int ballNumber;
    private String batsmanStrikerId;
    private String batsmanNonStrikerId;
    private String bowlerId;
    private int runsScoredBat;
    private int runsScoredExtras;
    private int totalRuns;
    private boolean isWicket;
    private boolean isLegalDelivery;
    private String extrasType; // Enum: NONE, WIDE, NO_BALL, BYE, LEG_BYE, PENALTY
    private boolean isBoundary;
    private int boundaryType; // Enum: 0=NONE, 4=FOUR, 6=SIX
    private boolean isFreeHit; // <-- Field is named isFreeHit
    private String ballOutcome; // Enum: DOT, SINGLE, DOUBLE, TRIPLE, FOUR, SIX, WICKET, etc.

    // Reference to detailed objects (if any)
    private CricketWicketDetail wicketDetail;
    private CricketExtrasDetail extrasDetail;


    public CricketEvent() {
        super();
        this.eventType = "CRICKET_EVENT";
    }

    // --- Getters and Setters ---

    public int getOverNumber() {
        return overNumber;
    }

    public void setOverNumber(int overNumber) {
        this.overNumber = overNumber;
    }

    public int getBallNumber() {
        return ballNumber;
    }

    public void setBallNumber(int ballNumber) {
        this.ballNumber = ballNumber;
    }

    public String getBatsmanStrikerId() {
        return batsmanStrikerId;
    }

    public void setBatsmanStrikerId(String batsmanStrikerId) {
        this.batsmanStrikerId = batsmanStrikerId;
    }

    public String getBatsmanNonStrikerId() {
        return batsmanNonStrikerId;
    }

    public void setBatsmanNonStrikerId(String batsmanNonStrikerId) {
        this.batsmanNonStrikerId = batsmanNonStrikerId;
    }

    public String getBowlerId() {
        return bowlerId;
    }

    public void setBowlerId(String bowlerId) {
        this.bowlerId = bowlerId;
    }

    public int getRunsScoredBat() {
        return runsScoredBat;
    }

    public void setRunsScoredBat(int runsScoredBat) {
        this.runsScoredBat = runsScoredBat;
    }

    public int getRunsScoredExtras() {
        return runsScoredExtras;
    }

    public void setRunsScoredExtras(int runsScoredExtras) {
        this.runsScoredExtras = runsScoredExtras;
    }

    public int getTotalRuns() {
        return totalRuns;
    }

    public void setTotalRuns(int totalRuns) {
        this.totalRuns = totalRuns;
    }

    public boolean isWicket() {
        return isWicket;
    }

    public void setWicket(boolean wicket) {
        isWicket = wicket;
    }

    public boolean isLegalDelivery() {
        return isLegalDelivery;
    }

    public void setLegalDelivery(boolean legalDelivery) {
        isLegalDelivery = legalDelivery;
    }

    public String getExtrasType() {
        return extrasType;
    }

    public void setExtrasType(String extrasType) {
        this.extrasType = extrasType;
    }

    public boolean isBoundary() {
        return isBoundary;
    }

    public void setBoundary(boolean boundary) {
        isBoundary = boundary;
    }

    public int getBoundaryType() {
        return boundaryType;
    }

    public void setBoundaryType(int boundaryType) {
        this.boundaryType = boundaryType;
    }

    // --- This is the corrected section ---
    public boolean isFreeHit() {
        return isFreeHit;
    }

    public void setFreeHit(boolean freeHit) {
        // The field is this.isFreeHit, not this.freeHit
        this.isFreeHit = freeHit;
    }
    // --- End of corrected section ---

    public String getBallOutcome() {
        return ballOutcome;
    }

    public void setBallOutcome(String ballOutcome) {
        this.ballOutcome = ballOutcome;
    }

    public CricketWicketDetail getWicketDetail() {
        return wicketDetail;
    }

    public void setWicketDetail(CricketWicketDetail wicketDetail) {
        this.wicketDetail = wicketDetail;
    }

    public CricketExtrasDetail getExtrasDetail() {
        return extrasDetail;
    }

    public void setExtrasDetail(CricketExtrasDetail extrasDetail) {
        this.extrasDetail = extrasDetail;
    }
}