package com.example.tournafy.domain.models.match.football;

import com.example.tournafy.domain.models.base.MatchConfig;

/**
 * Concrete configuration model for a Football or Futsal match.
 * This class holds all settings specific to football/futsal.
 * The ConfigFactory creates this object.
 */
public class FootballMatchConfig extends MatchConfig {

    // Fields are based on the EERD and file structure 
    private int matchDuration;       // Total match time in minutes
    private int playersPerSide;
    private boolean offsideOn;

    public FootballMatchConfig() {
        super();
        this.matchDuration = 90;   // Default to 90 minutes
        this.playersPerSide = 11;  // Default to 11
        this.offsideOn = true;
    }

    // --- Getters and Setters ---

    public int getMatchDuration() {
        return matchDuration;
    }

    public void setMatchDuration(int matchDuration) {
        this.matchDuration = matchDuration;
    }

    public int getPlayersPerSide() {
        return playersPerSide;
    }

    public void setPlayersPerSide(int playersPerSide) {
        this.playersPerSide = playersPerSide;
    }

    public boolean isOffsideOn() {
        return offsideOn;
    }

    public void setOffsideOn(boolean offsideOn) {
        this.offsideOn = offsideOn;
    }
}