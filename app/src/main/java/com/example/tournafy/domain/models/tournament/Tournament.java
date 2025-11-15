package com.example.tournafy.domain.models.tournament;

import com.example.tournafy.domain.models.base.HostedEntity;
import com.example.tournafy.domain.enums.EntityStatus; 
import java.util.Date;
import java.util.Map;
import java.util.List;
import com.example.tournafy.domain.models.team.TournamentTeam;
import com.example.tournafy.domain.models.tournament.TournamentStage;

/**
 * Domain Model for a Tournament (File: Tournament.java).
 * Inherits from HostedEntity.
 * Corresponds to TOURNAMENT in the EERD.
 * This class is constructed using the BUILDER PATTERN.
 */
public class Tournament extends HostedEntity {

    private String sportId;
    private String tournamentType; // e.g., KNOCKOUT, ROUND_ROBIN 
    private Map<String, Object> tournamentConfig; // For storing JSON config
    private Date startDate;
    private String winnerTeamId;

    // Relationships (will be loaded by the TournamentService/ViewModel)
    // private List<TournamentTeam> teams;
    // private List<TournamentStage> stages;

    /**
     * Private constructor.
     * This forces the use of the Builder.
     * @param builder 
     */
    private Tournament(Builder builder) {
        super();
        this.entityType = "TOURNAMENT"; 

        // --- Set HostedEntity fields from Builder ---
        this.name = builder.name;
        this.hostUserId = builder.hostUserId;
        this.status = EntityStatus.DRAFT.name(); // Default status
        this.createdAt = new Date();
        this.isOnline = false;

        // --- Set Tournament-specific fields from Builder ---
        this.sportId = builder.sportId;
        this.tournamentType = builder.tournamentType;
        this.tournamentConfig = builder.tournamentConfig; // Optional
        this.startDate = builder.startDate; // Optional
    }

    public Tournament() {
        super();
        this.entityType = "TOURNAMENT";
    }

    // --- Static Inner Builder Class ---

    public static class Builder {

        // --- Required fields ---
        private final String name;
        private final String hostUserId;
        private final String sportId;
        private final String tournamentType;

        // --- Optional fields ---
        private Map<String, Object> tournamentConfig;
        private Date startDate;

        /**
         * @param name           The name of the tournament.
         * @param hostUserId     The ID of the user hosting the tournament.
         * @param sportId        The ID of the sport.
         * @param tournamentType The type of tournament (KNOCKOUT, ROUND_ROBIN).
         */
        public Builder(String name, String hostUserId, String sportId, String tournamentType) {
            this.name = name;
            this.hostUserId = hostUserId;
            this.sportId = sportId;
            this.tournamentType = tournamentType;
            this.startDate = new Date(); // Default to now
        }

        /**
         * @param config The configuration map.
         * @return The Builder instance for chaining.
         */
        public Builder withTournamentConfig(Map<String, Object> config) {
            this.tournamentConfig = config;
            return this;
        }

        /**
         * @param startDate The date the tournament begins.
         * @return The Builder instance for chaining.
         */
        public Builder withStartDate(Date startDate) {
            this.startDate = startDate;
            return this;
        }

        /**
         * Creates and returns the final Tournament object.
         * This calls the private constructor of the Tournament class.
         *
         * @return A new instance of Tournament.
         */
        public Tournament build() {
            return new Tournament(this);
        }
    }

    // --- Getters and Setters ---
    // (Required for data binding and Firebase/Firestore serialization)

    public String getSportId() {
        return sportId;
    }

    public void setSportId(String sportId) {
        this.sportId = sportId;
    }

    public String getTournamentType() {
        return tournamentType;
    }

    public void setTournamentType(String tournamentType) {
        this.tournamentType = tournamentType;
    }

    public Map<String, Object> getTournamentConfig() {
        return tournamentConfig;
    }

    public void setTournamentConfig(Map<String, Object> tournamentConfig) {
        this.tournamentConfig = tournamentConfig;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getWinnerTeamId() {
        return winnerTeamId;
    }

    public void setWinnerTeamId(String winnerTeamId) {
        this.winnerTeamId = winnerTeamId;
    }
}