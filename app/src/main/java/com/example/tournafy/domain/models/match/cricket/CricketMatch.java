package com.example.tournafy.domain.models.match.cricket;

import com.example.tournafy.domain.enums.MatchStatus;
import com.example.tournafy.domain.models.base.Match;
import com.example.tournafy.domain.models.base.MatchEvent;
import com.example.tournafy.domain.models.sport.SportTypeEnum;
import com.example.tournafy.domain.models.team.MatchTeam;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Concrete implementation of a Match for Cricket.
 * Extends the abstract Match class and provides cricket-specific logic.
 * This class also implements the Builder Pattern for easy construction.
 */
public class CricketMatch extends Match {

    // Cricket-specific relational data
    private List<Innings> innings;
    private List<CricketEvent> cricketEvents;
    private List<MatchTeam> teams; // List of teams participating

    /**
     * Private constructor to enforce the use of the Builder.
     */
    public CricketMatch() {
        super();
        this.entityType = "MATCH";
        this.sportId = SportTypeEnum.CRICKET.name();
        this.matchStatus = MatchStatus.SCHEDULED.name();

        // Initialize lists
        this.innings = new ArrayList<>();
        this.cricketEvents = new ArrayList<>();
        this.teams = new ArrayList<>();
    }

    // --- Abstract Method Implementations ---

    @Override
    public void startMatch() {
        // Set the match status to LIVE
        this.matchStatus = MatchStatus.LIVE.name();
        this.status = "ACTIVE";
    }

    @Override
    public void endMatch() {
        // Set the match status to COMPLETED
        this.matchStatus = MatchStatus.COMPLETED.name();
        this.status = "COMPLETED";

        // TODO: Implement result calculation logic
        // e.g., compare scores from innings(0) and innings(1)
        // and set the this.winnerTeamId
    }

    @Override
    public void addEvent(MatchEvent event) {
        if (event instanceof CricketEvent) {
            CricketEvent cricketEvent = (CricketEvent) event;
            this.cricketEvents.add(cricketEvent);

            // TODO: Implement logic to update innings/overs/balls
            // This is where the core scoring logic will live.
            // e.g., find current innings, find current over, add ball
        }
    }

    // --- Getters and Setters for Cricket-specific fields ---

    public List<Innings> getInnings() {
        return innings;
    }

    public void setInnings(List<Innings> innings) {
        this.innings = innings;
    }

    public List<CricketEvent> getCricketEvents() {
        return cricketEvents;
    }

    public void setCricketEvents(List<CricketEvent> cricketEvents) {
        this.cricketEvents = cricketEvents;
    }

    public List<MatchTeam> getTeams() {
        return teams;
    }

    public void setTeams(List<MatchTeam> teams) {
        this.teams = teams;
    }

    // --- --- --- --- --- --- --- --- ---
    //   BUILDER PATTERN IMPLEMENTATION
    // --- --- --- --- --- --- --- --- ---

    /**
     * Concrete Builder for creating a CricketMatch.
     * This extends the abstract Match.Builder to provide a fluent API.
     */
    public static class Builder extends Match.Builder<Builder> {

        private CricketMatchConfig config;
        private List<MatchTeam> teams;

        /**
         * Required constructor for the Builder.
         *
         * @param name       The name of the match (e.g., "Team A vs Team B").
         * @param hostUserId The ID of the user hosting this match.
         */
        public Builder(String name, String hostUserId) {
            super(name, hostUserId);
            this.teams = new ArrayList<>();
        }

        /**
         * Sets the cricket-specific configuration for this match.
         *
         * @param config A CricketMatchConfig object.
         * @return The builder instance for chaining.
         */
        public Builder withConfig(CricketMatchConfig config) {
            this.config = config;
            return this;
        }

        /**
         * Adds a team to the match.
         *
         * @param team A MatchTeam object.
         * @return The builder instance for chaining.
         */
        public Builder addTeam(MatchTeam team) {
            this.teams.add(team);
            return this;
        }

        /**
         * Sets the list of teams for this match.
         *
         * @param teams A List of MatchTeam objects.
         * @return The builder instance for chaining.
         */
        public Builder withTeams(List<MatchTeam> teams) {
            this.teams = teams;
            return this;
        }

        /**
         * This is required by the abstract builder to return the
         * concrete builder instance.
         */
        @Override
        protected Builder self() {
            return this;
        }

        /**
         * Constructs the final CricketMatch object.
         *
         * @return A new instance of CricketMatch.
         */
        @Override
        public CricketMatch build() {
            CricketMatch match = new CricketMatch();
            
            // Set fields from HostedEntity (via Match)
            match.entityId = UUID.randomUUID().toString();
            match.name = this.name;
            match.hostUserId = this.hostUserId;
            match.createdAt = new Date();
            match.isOnline = false; // Default to offline
            
            // Set fields from Match
            match.matchDate = this.matchDate;
            match.venue = this.venue;
            match.matchStatus = MatchStatus.SCHEDULED.name();
            match.status = "DRAFT"; // Set initial entity status

            // Set fields from CricketMatch
            match.setMatchConfig(this.config != null ? this.config : new CricketMatchConfig());
            match.setTeams(this.teams);

            return match;
        }
    }
}