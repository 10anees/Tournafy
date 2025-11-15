package com.example.tournafy.domain.models.match.football;

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
 * Concrete implementation of a Match for Football.
 * Extends the abstract Match class and provides football-specific logic.
 * This class also implements the Builder Pattern for easy construction.
 */
public class FootballMatch extends Match {

    private List<FootballEvent> footballEvents;
    private List<MatchTeam> teams; // List of teams participating

    public FootballMatch() {
        super();
        this.entityType = "MATCH";
        this.sportId = SportTypeEnum.FOOTBALL.name();
        this.matchStatus = MatchStatus.SCHEDULED.name();

        this.footballEvents = new ArrayList<>();
        this.teams = new ArrayList<>();
    }

    // --- Abstract Method Implementations ---

    @Override
    public void startMatch() {
        this.matchStatus = MatchStatus.LIVE.name();
        this.status = "ACTIVE";
    }

    @Override
    public void endMatch() {
        this.matchStatus = MatchStatus.COMPLETED.name();
        this.status = "COMPLETED";

        // TODO: Implement result calculation logic
        // e.g., compare scores from the two MatchTeam objects
        // and set the this.winnerTeamId
    }

    @Override
    public void addEvent(MatchEvent event) {
        if (event instanceof FootballEvent) {
            FootballEvent footballEvent = (FootballEvent) event;
            this.footballEvents.add(footballEvent);

            // TODO: Implement logic to update team scores
            // if(footballEvent.getEventCategory().equals("GOAL")) {
            //     // Find the correct MatchTeam and increment score
            // }
        }
    }

    // --- Getters and Setters for Football-specific fields ---

    public List<FootballEvent> getFootballEvents() {
        return footballEvents;
    }

    public void setFootballEvents(List<FootballEvent> footballEvents) {
        this.footballEvents = footballEvents;
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
     * Concrete Builder for creating a FootballMatch.
     * This extends the abstract Match.Builder to provide a fluent API.
     */
    public static class Builder extends Match.Builder<Builder> {

        private FootballMatchConfig config;
        private List<MatchTeam> teams;

        /**
         * @param name       The name of the match (e.g., "Team A vs Team B").
         * @param hostUserId The ID of the user hosting this match.
         */
        public Builder(String name, String hostUserId) {
            super(name, hostUserId);
            this.teams = new ArrayList<>();
        }

        /**
         * @param config A FootballMatchConfig object.
         * @return The builder instance for chaining.
         */
        public Builder withConfig(FootballMatchConfig config) {
            this.config = config;
            return this;
        }

        /**
         * @param team A MatchTeam object.
         * @return The builder instance for chaining.
         */
        public Builder addTeam(MatchTeam team) {
            this.teams.add(team);
            return this;
        }

        /**
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
         * Constructs the final FootballMatch object.
         *
         * @return A new instance of FootballMatch.
         */
        @Override
        public FootballMatch build() {
            FootballMatch match = new FootballMatch();
            
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

            // Set fields from FootballMatch
            match.setMatchConfig(this.config != null ? this.config : new FootballMatchConfig());
            match.setTeams(this.teams);

            return match;
        }
    }
}