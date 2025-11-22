package com.example.tournafy.domain.models.match.cricket;

public class BatsmanStats {
    private String playerId;
    private String playerName;
    private int runsScored;
    private int ballsFaced;
    private int fours;
    private int sixes;
    private boolean isOut;
    private String dismissalType; // "BOWLED", "CAUGHT", "RUN_OUT", etc.

    public BatsmanStats() {
        // Default constructor for Firestore
    }

    public BatsmanStats(String playerId, String playerName) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.runsScored = 0;
        this.ballsFaced = 0;
        this.fours = 0;
        this.sixes = 0;
        this.isOut = false;
    }

    // Methods to update stats
    public void addRuns(int runs) {
        this.runsScored += runs;
        if (runs == 4) {
            this.fours++;
        } else if (runs == 6) {
            this.sixes++;
        }
    }

    public void addBall() {
        this.ballsFaced++;
    }

    public void dismissBatsman(String dismissalType) {
        this.isOut = true;
        this.dismissalType = dismissalType;
    }

    // Calculate strike rate
    public double getStrikeRate() {
        if (ballsFaced == 0) {
            return 0.0;
        }
        return ((double) runsScored / ballsFaced) * 100;
    }

    // Getters and Setters
    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getRunsScored() {
        return runsScored;
    }

    public void setRunsScored(int runsScored) {
        this.runsScored = runsScored;
    }

    public int getBallsFaced() {
        return ballsFaced;
    }

    public void setBallsFaced(int ballsFaced) {
        this.ballsFaced = ballsFaced;
    }

    public int getFours() {
        return fours;
    }

    public void setFours(int fours) {
        this.fours = fours;
    }

    public int getSixes() {
        return sixes;
    }

    public void setSixes(int sixes) {
        this.sixes = sixes;
    }

    public boolean isOut() {
        return isOut;
    }

    public void setOut(boolean out) {
        isOut = out;
    }

    public String getDismissalType() {
        return dismissalType;
    }

    public void setDismissalType(String dismissalType) {
        this.dismissalType = dismissalType;
    }

    @Override
    public String toString() {
        return playerName + ": " + runsScored + "(" + ballsFaced + ") " + 
               "SR: " + String.format("%.2f", getStrikeRate());
    }
}
