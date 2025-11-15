package com.example.tournafy.domain.models.match.cricket;

import com.example.tournafy.domain.models.base.MatchConfig;

/**
 * Concrete configuration model for a Cricket match.
 * Extends the abstract MatchConfig.
 * [cite_start]This class holds all settings specific to cricket. 
 */
public class CricketMatchConfig extends MatchConfig {

    // Fields are based on the EERD and file structure 
    private int numberOfOvers;
    private boolean wideOn;
    private int numberOfBouncersInOneOver;
    private String formatType; // e.g., "T20", "ODI", "Custom"

    /**
     * Default constructor.
     * We can initialize some sane defaults.
     */
    public CricketMatchConfig() {
        super();
        this.numberOfOvers = 20;
        this.wideOn = true;
        this.numberOfBouncersInOneOver = 1;
        this.formatType = "Custom";
    }

    // --- Getters and Setters ---

    public int getNumberOfOvers() {
        return numberOfOvers;
    }

    public void setNumberOfOvers(int numberOfOvers) {
        this.numberOfOvers = numberOfOvers;
    }

    public boolean isWideOn() {
        return wideOn;
    }

    public void setWideOn(boolean wideOn) {
        this.wideOn = wideOn;
    }

    public int getNumberOfBouncersInOneOver() {
        return numberOfBouncersInOneOver;
    }

    public void setNumberOfBouncersInOneOver(int numberOfBouncersInOneOver) {
        this.numberOfBouncersInOneOver = numberOfBouncersInOneOver;
    }

    public String getFormatType() {
        return formatType;
    }

    public void setFormatType(String formatType) {
        this.formatType = formatType;
    }
}