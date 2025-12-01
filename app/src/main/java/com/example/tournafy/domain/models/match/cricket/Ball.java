package com.example.tournafy.domain.models.match.cricket;

/**
 * Domain Model for a single Ball in an Over.
 * Corresponds to BALL in the EERD.
 */
public class Ball {

    private String ballId;

    // --- Hierarchy Foreign Keys ---
    private String matchId;      // Link to Match
    private String inningsId;    // Link to Innings
    private String overId;       // Link to Over

    // --- Sequencing ---
    private int inningsNumber;   // 1 or 2
    private int overNumber;      // 0, 1, 2...
    private int ballNumber;      // 1-6 (or more for extras)

    // --- Players ---
    private String batsmanId;    // FK to Player
    private String bowlerId;     // FK to Player

    // --- Outcome ---
    private int runsScored;
    private boolean isWicket;
    private boolean isBoundary;  // New field
    private String extrasType;   // e.g., "WIDE", "NO_BALL", "NONE"
    private String wicketType;   // e.g., "BOWLED", "CAUGHT"

    // No-arg constructor for Firestore
    public Ball() {}

    // --- Getters and Setters ---

    public String getBallId() { return ballId; }
    public void setBallId(String ballId) { this.ballId = ballId; }

    public String getMatchId() { return matchId; }
    public void setMatchId(String matchId) { this.matchId = matchId; }

    public String getInningsId() { return inningsId; }
    public void setInningsId(String inningsId) { this.inningsId = inningsId; }

    public String getOverId() { return overId; }
    public void setOverId(String overId) { this.overId = overId; }

    public int getInningsNumber() { return inningsNumber; }
    public void setInningsNumber(int inningsNumber) { this.inningsNumber = inningsNumber; }

    public int getOverNumber() { return overNumber; }
    public void setOverNumber(int overNumber) { this.overNumber = overNumber; }

    public int getBallNumber() { return ballNumber; }
    public void setBallNumber(int ballNumber) { this.ballNumber = ballNumber; }

    public String getBatsmanId() { return batsmanId; }
    public void setBatsmanId(String batsmanId) { this.batsmanId = batsmanId; }

    public String getBowlerId() { return bowlerId; }
    public void setBowlerId(String bowlerId) { this.bowlerId = bowlerId; }

    public int getRunsScored() { return runsScored; }
    public void setRunsScored(int runsScored) { this.runsScored = runsScored; }

    public boolean isWicket() { return isWicket; }
    public void setWicket(boolean wicket) { isWicket = wicket; }

    public boolean isBoundary() { return isBoundary; }
    public void setBoundary(boolean boundary) { isBoundary = boundary; }

    public String getExtrasType() { return extrasType; }
    public void setExtrasType(String extrasType) { this.extrasType = extrasType; }

    public String getWicketType() { return wicketType; }
    public void setWicketType(String wicketType) { this.wicketType = wicketType; }

    // --- Helper Methods ---

    /**
     * Checks if this ball is a legal delivery (counts towards the 6-ball over).
     * 
     * Legal deliveries:
     * - Normal balls (no extras)
     * - BYE - ball hit pad/body, batsman didn't hit it
     * - LEG_BYE - similar to bye but off the legs
     * 
     * Illegal deliveries (don't count towards over):
     * - WIDE - ball too wide to hit
     * - NO_BALL - bowler overstepped or illegal action
     * 
     * This is a calculated field and should not be serialized to Firestore.
     * 
     * @return true if the ball is legal, false otherwise
     */
    @com.google.firebase.firestore.Exclude
    public boolean isLegalDelivery() {
        // Wide and No-Ball are illegal deliveries (don't count towards over)
        if (extrasType != null && (extrasType.equals("WIDE") || extrasType.equals("NO_BALL"))) {
            return false;
        }
        // Everything else is legal (normal ball, bye, leg-bye)
        return true;
    }
}