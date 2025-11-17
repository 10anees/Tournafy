package com.example.tournafy.domain.models.base;

import com.example.tournafy.domain.models.base.HostedEntity;
import com.example.tournafy.domain.models.base.MatchEvent;
import com.example.tournafy.domain.models.base.MatchConfig;
import java.util.Date;

/**
 * Abstract class for a Match.
 */
public abstract class Match extends HostedEntity {

    protected String sportId;
    protected String matchFormat;
    protected MatchConfig matchConfig;
    protected Date matchDate;
    protected String venue;
    protected String winnerTeamId;
    protected String matchStatus;
    protected String tournamentId;
    protected String seriesId;

    public Match() {
        super();
        this.entityType = "MATCH";
    }

    public abstract void startMatch();
    public abstract void endMatch();
    public abstract void addEvent(MatchEvent event);

    // Getters and Setters
    public String getSportId() { return sportId; }
    public void setSportId(String sportId) { this.sportId = sportId; }

    public String getMatchFormat() { return matchFormat; }
    public void setMatchFormat(String matchFormat) { this.matchFormat = matchFormat; }

    public MatchConfig getMatchConfig() { return matchConfig; }
    public void setMatchConfig(MatchConfig matchConfig) { this.matchConfig = matchConfig; }

    public Date getMatchDate() { return matchDate; }
    public void setMatchDate(Date matchDate) { this.matchDate = matchDate; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public String getWinnerTeamId() { return winnerTeamId; }
    public void setWinnerTeamId(String winnerTeamId) { this.winnerTeamId = winnerTeamId; }

    public String getMatchStatus() { return matchStatus; }
    public void setMatchStatus(String matchStatus) { this.matchStatus = matchStatus; }

    public String getTournamentId() { return tournamentId; }
    public void setTournamentId(String tournamentId) { this.tournamentId = tournamentId; }

    public String getSeriesId() { return seriesId; }
    public void setSeriesId(String seriesId) { this.seriesId = seriesId; }

    // --- Abstract Builder ---
    // This allows child builders to chain methods defined here
    public static abstract class Builder<T extends Builder<T>> {
        protected String name;
        protected String hostUserId;
        protected String venue;
        protected Date matchDate;

        public Builder(String name, String hostUserId) {
            this.name = name;
            this.hostUserId = hostUserId;
            this.matchDate = new Date(); // Default
        }

        public T withVenue(String venue) {
            this.venue = venue;
            return self();
        }

        public T withMatchDate(Date matchDate) {
            this.matchDate = matchDate;
            return self();
        }

        // Required for fluent API in subclasses
        protected abstract T self();

        public abstract Match build();
    }
}