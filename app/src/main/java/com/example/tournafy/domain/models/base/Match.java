package com.example.tournafy.domain.models.base;

import java.util.Date;

/**
 * Abstract class for a Match.
 * UPDATED to include common statistic methods required by MatchViewModel.
 */
public abstract class Match extends HostedEntity {

    protected String sportId;
    protected String matchFormat;
    protected MatchConfig matchConfig;
    protected Date matchDate;
    protected String venue;
    protected String winnerTeamId;
    
    protected String tournamentId;
    protected String seriesId;

    public Match() {
        super();
        this.entityType = "MATCH";
    }

    // --- CORE ABSTRACT METHODS ---
    public abstract void startMatch();
    public abstract void endMatch();
    public abstract void addEvent(MatchEvent event);
    public abstract boolean canStartMatch();

    // --- COMMAND PATTERN SUPPORT METHODS ---
    public abstract void addMatchEvent(MatchEvent event);
    public abstract void removeMatchEvent(MatchEvent event);
    public abstract void setHomeScore(int score);
    public abstract void setAwayScore(int score);
    public abstract String getHomeTeamId();
    public abstract void performSubstitution(String playerOutId, String playerInId);

    // --- NEW: ABSTRACT STATISTIC/HELPER METHODS (Required by ViewModel) ---
    
    /**
     * Gets the required run rate for the chasing team (Cricket only).
     * Returns 0.0f for non-cricket matches.
     */
    public abstract float getRequiredRunRate();
    
    /**
     * Gets the number of balls remaining in the current innings (Cricket only).
     * Returns 0 for non-cricket matches.
     */
    public abstract int getRemainingBalls();
    
    /**
     * Gets the total number of extras conceded by the bowling side (Cricket only).
     * Returns 0 for non-cricket matches.
     */
    public abstract int getExtrasCount();
    
    // --- Getters and Setters (Omitted for brevity) ---
    // ...
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
    
    public String getMatchStatus() { return this.status; }
    public void setMatchStatus(String matchStatus) { this.status = matchStatus; }

    public String getTournamentId() { return tournamentId; }
    public void setTournamentId(String tournamentId) { this.tournamentId = tournamentId; }
    public String getSeriesId() { return seriesId; }
    public void setSeriesId(String seriesId) { this.seriesId = seriesId; }

    // --- Abstract Builder (Omitted for brevity) ---
    public static abstract class Builder<T extends Builder<T>> {
        protected String name;
        protected String hostUserId;
        protected String venue;
        protected Date matchDate;

        public Builder(String name, String hostUserId) {
            this.name = name;
            this.hostUserId = hostUserId;
            this.matchDate = new Date(); 
        }
        public T withVenue(String venue) { this.venue = venue; return self(); }
        public T withMatchDate(Date matchDate) { this.matchDate = matchDate; return self(); }
        protected abstract T self();
        public abstract Match build();
    }
}