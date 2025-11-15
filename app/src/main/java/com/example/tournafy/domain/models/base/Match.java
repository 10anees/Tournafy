package com.example.tournafy.domain.models.base;

import com.example.tournafy.domain.models.match.MatchResult;
import com.example.tournafy.domain.models.team.MatchTeam;
import com.example.tournafy.domain.enums.MatchStatus;
import java.util.Date;
import java.util.List;

/**
 * Abstract class for a Match 
 * Inherits from HostedEntity.
 * Defines common properties and behaviors for any sport match.
 * Maps to the MATCH table in the EERD. 
 */
public abstract class Match extends HostedEntity {

    protected String sportId;
    protected String matchFormat;
    protected MatchConfig matchConfig; // Abstract config 
    protected Date matchDate;
    protected String venue;
    protected String winnerTeamId;
    protected String matchStatus; // Using String to align with MatchStatus enum 
    protected String tournamentId;
    protected String seriesId;

    // References to related entities
    // protected List<MatchTeam> teams;
    // protected List<MatchEvent> events;
    // protected MatchResult matchResult;

    public Match() {
        super();
        this.entityType = "MATCH";
        // this.matchStatus = MatchStatus.SCHEDULED.name();
    }

    // Abstract methods to be implemented by concrete classes (CricketMatch, FootballMatch)
    
    /**
     * Starts the match, changing its status to LIVE.
     */
    public abstract void startMatch();

    /**
     * Ends the match, changing its status to COMPLETED and calculating the result.
     */
    public abstract void endMatch();

    /**
     * Adds a new event to the match.
     * @param event The MatchEvent to add.
     */
    public abstract void addEvent(MatchEvent event);

    // Getters and Setters
    public String getSportId() {
        return sportId;
    }

    public void setSportId(String sportId) {
        this.sportId = sportId;
    }

    public String getMatchFormat() {
        return matchFormat;
    }

    public void setMatchFormat(String matchFormat) {
        this.matchFormat = matchFormat;
    }

    public MatchConfig getMatchConfig() {
        return matchConfig;
    }

    public void setMatchConfig(MatchConfig matchConfig) {
        this.matchConfig = matchConfig;
    }

    public Date getMatchDate() {
        return matchDate;
    }

    public void setMatchDate(Date matchDate) {
        this.matchDate = matchDate;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getWinnerTeamId() {
        return winnerTeamId;
    }

    public void setWinnerTeamId(String winnerTeamId) {
        this.winnerTeamId = winnerTeamId;
    }

    public String getMatchStatus() {
        return matchStatus;
    }

    public void setMatchStatus(String matchStatus) {
        this.matchStatus = matchStatus;
    }

    public String getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(String tournamentId) {
        this.tournamentId = tournamentId;
    }

    public String getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(String seriesId) {
        this.seriesId = seriesId;
    }

    // The concrete builders will be in CricketMatch.java and FootballMatch.java
    public static abstract class Builder<T extends Builder<T>> {
        protected String name;
        protected String hostUserId;
        protected String venue;
        protected Date matchDate;

        public Builder(String name, String hostUserId) {
            this.name = name;
            this.hostUserId = hostUserId;
            this.matchDate = new Date(); // Default to now
        }

        public T withVenue(String venue) {
            this.venue = venue;
            return self();
        }

        public T withMatchDate(Date matchDate) {
            this.matchDate = matchDate;
            return self();
        }
        
        // This is required for the builder to work with inheritance
        protected abstract T self();

        public abstract Match build();
    }
}