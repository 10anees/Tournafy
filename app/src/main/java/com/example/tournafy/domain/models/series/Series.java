package com.example.tournafy.domain.models.series;

import com.example.tournafy.domain.models.base.HostedEntity;
import com.example.tournafy.domain.enums.EntityStatus;
import com.example.tournafy.domain.interfaces.SeriesObserver;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import com.example.tournafy.domain.models.series.SeriesMatch;


/**
 * Inherits from HostedEntity.
 * Corresponds to SERIES in the EERD
 * This class is constructed using the BUILDER PATTERN.
 */
public class Series extends HostedEntity {

    private String sportId;
    private String teamAId;
    private String teamBId;
    private int totalMatches;
    private int teamAWins;
    private int teamBWins;

    // Relationships
    // private List<SeriesMatch> matches;

    // --- --- --- --- --- --- --- --- ---
    //   OBSERVER PATTERN (SUBJECT)
    // --- --- --- --- --- --- --- --- ---
    // Added 'transient' to prevent serialization
    private transient List<SeriesObserver> observers = new ArrayList<>();
    // --- --- --- --- --- --- --- --- ---

    /**
     * Private constructor.
     * This forces the use of the Builder.
     * @param builder The builder object containing all parameters.
     */
    private Series(Builder builder) {
        super();
        this.entityType = "SERIES"; // Set the entity type

        // --- Set HostedEntity fields from Builder ---
        this.name = builder.name;
        this.hostUserId = builder.hostUserId;
        this.status = EntityStatus.DRAFT.name(); // Default status
        this.createdAt = new Date();
        this.isOnline = false;

        // --- Set Series-specific fields from Builder ---
        this.sportId = builder.sportId;
        this.teamAId = builder.teamAId;
        this.teamBId = builder.teamBId;
        this.totalMatches = builder.totalMatches;

        // --- Set default values for a new series ---
        this.teamAWins = 0;
        this.teamBWins = 0;
    }


    public Series() {
        super();
        this.entityType = "SERIES";
    }

    // --- Static Inner Builder Class ---

    public static class Builder {

        private final String name;
        private final String hostUserId;
        private final String sportId;
        private final String teamAId;
        private final String teamBId;

        private int totalMatches = 3; // Default to 3 matches if not set

        /**
         * @param name       The name of the series.
         * @param hostUserId The ID of the user hosting the series.
         * @param sportId    The ID of the sport.
         * @param teamAId    The ID of the first team.
         * @param teamBId    The ID of the second team.
         */
        public Builder(String name, String hostUserId, String sportId, String teamAId, String teamBId) {
            this.name = name;
            this.hostUserId = hostUserId;
            this.sportId = sportId;
            this.teamAId = teamAId;
            this.teamBId = teamBId;
        }

        /**
         * @param totalMatches The total number of matches.
         * @return The Builder instance for chaining.
         */
        public Builder withTotalMatches(int totalMatches) {
            this.totalMatches = totalMatches;
            return this;
        }

        /**
         * Creates and returns the final Series object.
         * This calls the private constructor of the Series class.
         * @return A new instance of Series.
         */
        public Series build() {
            return new Series(this);
        }
    }

    // --- Getters and Setters ---
    public String getSportId() {
        return sportId;
    }

    public void setSportId(String sportId) {
        this.sportId = sportId;
    }

    public String getTeamAId() {
        return teamAId;
    }

    public void setTeamAId(String teamAId) {
        this.teamAId = teamAId;
    }

    public String getTeamBId() {
        return teamBId;
    }

    public void setTeamBId(String teamBId) {
        this.teamBId = teamBId;
    }

    public int getTotalMatches() {
        return totalMatches;
    }

    public void setTotalMatches(int totalMatches) {
        this.totalMatches = totalMatches;
    }

    public int getTeamAWins() {
        return teamAWins;
    }

    public void setTeamAWins(int teamAWins) {
        this.teamAWins = teamAWins;
    }

    public int getTeamBWins() {
        return teamBWins;
    }

    public void setTeamBWins(int teamBWins) {
        this.teamBWins = teamBWins;
    }
    
    // --- --- --- --- --- --- --- --- ---
    //   OBSERVER PATTERN IMPLEMENTATION (ADDED)
    // --- --- --- --- --- --- --- --- ---

    public void addObserver(SeriesObserver observer) {
        if (this.observers == null) {
            this.observers = new ArrayList<>();
        }
        if (!this.observers.contains(observer)) {
            this.observers.add(observer);
        }
    }

    public void removeObserver(SeriesObserver observer) {
        if (this.observers != null) {
            this.observers.remove(observer);
        }
    }

    /**
     * Notify all observers that the series score/state has been updated.
     * This will be called by the HostingService/SeriesService when a match completes.
     */
    public void notifySeriesScoreUpdated() {
        if (this.observers == null) return;
        // Create a copy to avoid ConcurrentModificationException
        for (SeriesObserver observer : new ArrayList<>(this.observers)) {
            observer.onSeriesScoreUpdated(this);
        }
    }
}