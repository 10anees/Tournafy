package com.example.tournafy.domain.models.match.cricket;

public class BowlerStats {
    private String playerId;
    private String playerName;
    private int ballsBowled;
    private int runsConceded;
    private int wicketsTaken;
    private int wides;
    private int noBalls;
    private int maidenOvers;

    public BowlerStats() {
        // Default constructor for Firestore
    }

    public BowlerStats(String playerId, String playerName) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.ballsBowled = 0;
        this.runsConceded = 0;
        this.wicketsTaken = 0;
        this.wides = 0;
        this.noBalls = 0;
        this.maidenOvers = 0;
    }

    // Methods to update stats
    public void addBall(int runs) {
        this.ballsBowled++;
        this.runsConceded += runs;
    }

    public void addWicket() {
        this.wicketsTaken++;
    }

    public void addWide(int runs) {
        this.wides++;
        this.runsConceded += (1 + runs); // Wide + any runs off it
    }

    public void addNoBall(int runs) {
        this.noBalls++;
        this.runsConceded += (1 + runs); // No ball + any runs off it
    }

    public void addMaidenOver() {
        this.maidenOvers++;
    }

    // Calculate overs bowled (e.g., 3.2 means 3 overs and 2 balls)
    public String getOversBowled() {
        int completeOvers = ballsBowled / 6;
        int extraBalls = ballsBowled % 6;
        return completeOvers + "." + extraBalls;
    }

    public double getOversBowledDecimal() {
        int completeOvers = ballsBowled / 6;
        int extraBalls = ballsBowled % 6;
        return completeOvers + (extraBalls / 10.0);
    }

    // Calculate economy rate (runs per over)
    public double getEconomyRate() {
        if (ballsBowled == 0) {
            return 0.0;
        }
        int completeOvers = ballsBowled / 6;
        int extraBalls = ballsBowled % 6;
        double totalOvers = completeOvers + (extraBalls / 6.0);
        
        if (totalOvers == 0) {
            return 0.0;
        }
        return runsConceded / totalOvers;
    }

    // Calculate bowling average (runs per wicket)
    public double getBowlingAverage() {
        if (wicketsTaken == 0) {
            return 0.0;
        }
        return (double) runsConceded / wicketsTaken;
    }

    // Calculate strike rate (balls per wicket)
    public double getBowlingStrikeRate() {
        if (wicketsTaken == 0) {
            return 0.0;
        }
        return (double) ballsBowled / wicketsTaken;
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

    public int getBallsBowled() {
        return ballsBowled;
    }

    public void setBallsBowled(int ballsBowled) {
        this.ballsBowled = ballsBowled;
    }

    public int getRunsConceded() {
        return runsConceded;
    }

    public void setRunsConceded(int runsConceded) {
        this.runsConceded = runsConceded;
    }

    public int getWicketsTaken() {
        return wicketsTaken;
    }

    public void setWicketsTaken(int wicketsTaken) {
        this.wicketsTaken = wicketsTaken;
    }

    public int getWides() {
        return wides;
    }

    public void setWides(int wides) {
        this.wides = wides;
    }

    public int getNoBalls() {
        return noBalls;
    }

    public void setNoBalls(int noBalls) {
        this.noBalls = noBalls;
    }

    public int getMaidenOvers() {
        return maidenOvers;
    }

    public void setMaidenOvers(int maidenOvers) {
        this.maidenOvers = maidenOvers;
    }

    @Override
    public String toString() {
        return playerName + ": " + getOversBowled() + "-" + maidenOvers + "-" + 
               runsConceded + "-" + wicketsTaken + " " + 
               "Econ: " + String.format("%.2f", getEconomyRate());
    }
}
