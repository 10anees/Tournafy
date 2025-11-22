package com.example.tournafy.domain.models.match.football;

import com.example.tournafy.domain.enums.MatchStatus;
import com.example.tournafy.domain.enums.football.MatchPeriod;
import com.example.tournafy.domain.interfaces.MatchObserver;
import com.example.tournafy.domain.models.base.Match;
import com.example.tournafy.domain.models.base.MatchEvent;
import com.example.tournafy.domain.models.match.MatchResult;
import com.example.tournafy.domain.models.sport.SportTypeEnum;
import com.example.tournafy.domain.models.team.MatchTeam;
import com.example.tournafy.domain.models.team.Player;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Concrete implementation of a Match for Football (Futsal).
 * Extends the abstract Match class and provides football-specific logic.
 */
public class FootballMatch extends Match {

    private List<FootballEvent> footballEvents;
    private List<MatchTeam> teams; 
    
    private int homeScore;
    private int awayScore;
    private int currentMatchMinute;
    private String matchPeriod; 
    
    // Timer state for persistence
    private long elapsedTimeMillis; // Total elapsed time in milliseconds
    private boolean timerRunning; // Whether timer is currently running
    
    private List<MatchObserver> observers; 
    
    private MatchResult matchResult;

    public FootballMatch() {
        super();
        this.entityType = "MATCH";
        this.sportId = SportTypeEnum.FOOTBALL.name();
        setMatchStatus(MatchStatus.SCHEDULED.name());

        this.footballEvents = new ArrayList<>();
        this.teams = new ArrayList<>();
        this.observers = new ArrayList<>();
        
        this.homeScore = 0;
        this.awayScore = 0;
        this.currentMatchMinute = 0;
        this.matchPeriod = MatchPeriod.FIRST_HALF.name();
    }

    // --- CORE EVENT-DRIVEN LOGIC (processEvent, updateMatchTime, Handlers) ---

    public void processEvent(MatchEvent event) {
        if (!(event instanceof FootballEvent)) {
            throw new IllegalArgumentException("Event must be a FootballEvent for FootballMatch");
        }
        
        FootballEvent footballEvent = (FootballEvent) event;
        
        footballEvent.setHomeScoreAtEvent(this.homeScore);
        footballEvent.setAwayScoreAtEvent(this.awayScore);
        
        this.footballEvents.add(footballEvent);
        
        if (footballEvent.getMatchMinute() > 0) {
            this.currentMatchMinute = footballEvent.getMatchMinute();
        }
        
        if (footballEvent.getMatchPeriod() != null && !footballEvent.getMatchPeriod().isEmpty()) {
            this.matchPeriod = footballEvent.getMatchPeriod();
        }
        
        String eventCategory = footballEvent.getEventCategory();
        
        if (eventCategory == null) return;
        
        switch (eventCategory.toUpperCase()) {
            case "GOAL":
                handleGoalEvent(footballEvent);
                break;
            case "PERIOD_END":
                handlePeriodEndEvent(footballEvent);
                break;
            default:
                break;
        }
        
        checkMatchCompletion();
        notifyObservers();
        notifyEventAdded(footballEvent);
    }

    public void updateMatchTime(int newMinute) {
        this.currentMatchMinute = newMinute;
        
        FootballMatchConfig config = (FootballMatchConfig) this.matchConfig;
        int halfDuration = config.getMatchDuration() / 2;
        
        if (this.matchPeriod.equals(MatchPeriod.FIRST_HALF.name()) && newMinute >= halfDuration) {
            endMatchPeriod();
        } else if (this.matchPeriod.equals(MatchPeriod.SECOND_HALF.name()) && newMinute >= config.getMatchDuration()) {
            endMatchPeriod();
        }
        
        notifyObservers();
    }
    
    private void handleGoalEvent(FootballEvent event) {
        if (event.getGoalDetail() == null) return;
        
        MatchTeam scoringTeam = findTeamById(event.getTeamId());
        if (scoringTeam == null) return;
        
        boolean isHomeTeam = scoringTeam.isHomeTeam();
        boolean isOwnGoal = event.getGoalDetail().isOwnGoal();
        
        if (isOwnGoal) {
            if (isHomeTeam) this.awayScore++; else this.homeScore++;
        } else {
            if (isHomeTeam) this.homeScore++; else this.awayScore++;
        }
        updateTeamScores();
    }
    
    private void handlePeriodEndEvent(FootballEvent event) { endMatchPeriod(); }
    
    private void endMatchPeriod() {
        if (this.matchPeriod.equals(MatchPeriod.FIRST_HALF.name())) {
            this.matchPeriod = MatchPeriod.SECOND_HALF.name();
            notifyStatusChanged("HALF_TIME");
        } else if (this.matchPeriod.equals(MatchPeriod.SECOND_HALF.name())) {
            determineWinner();
        }
    }

    private void checkMatchCompletion() {
        FootballMatchConfig config = (FootballMatchConfig) this.matchConfig;
        
        if (this.matchPeriod.equals(MatchPeriod.SECOND_HALF.name()) 
                && this.currentMatchMinute >= config.getMatchDuration()) {
            endMatchPeriod();
        }
    }

    private void determineWinner() {
        this.matchResult = new MatchResult();
        this.matchResult.setMatchId(this.entityId);
        this.matchResult.setResultId(UUID.randomUUID().toString());
        this.matchResult.setWinnerScore(Math.max(homeScore, awayScore));
        this.matchResult.setLoserScore(Math.min(homeScore, awayScore));
        
        MatchTeam homeTeam = getHomeTeam();
        MatchTeam awayTeam = getAwayTeam();
        
        if (this.homeScore > this.awayScore && homeTeam != null && awayTeam != null) {
            this.winnerTeamId = homeTeam.getTeamId();
            this.matchResult.setWinnerTeamId(homeTeam.getTeamId());
            this.matchResult.setLoserTeamId(awayTeam.getTeamId());
            this.matchResult.setResultType("WIN");
            int goalDifference = homeScore - awayScore;
            this.matchResult.setWinMargin(goalDifference + " goal" + (goalDifference > 1 ? "s" : ""));
        } else if (this.awayScore > this.homeScore && homeTeam != null && awayTeam != null) {
            this.winnerTeamId = awayTeam.getTeamId();
            this.matchResult.setWinnerTeamId(awayTeam.getTeamId());
            this.matchResult.setLoserTeamId(homeTeam.getTeamId());
            this.matchResult.setResultType("WIN");
            int goalDifference = awayScore - homeScore;
            this.matchResult.setWinMargin(goalDifference + " goal" + (goalDifference > 1 ? "s" : ""));
        } else {
            this.matchResult.setResultType("DRAW");
            this.matchResult.setWinMargin("Match Drawn");
        }
        
        setMatchStatus(MatchStatus.COMPLETED.name());
        this.status = "COMPLETED";
        
        notifyStatusChanged(MatchStatus.COMPLETED.name());
    }

    private void updateTeamScores() {
        for (MatchTeam team : teams) {
            if (team.isHomeTeam()) team.setScore(this.homeScore);
            else team.setScore(this.awayScore);
        }
    }

    // --- OBSERVER PATTERN IMPLEMENTATION ---
    public void addObserver(MatchObserver observer) {
        if (!this.observers.contains(observer)) this.observers.add(observer);
    }
    public void removeObserver(MatchObserver observer) { this.observers.remove(observer); }
    private void notifyObservers() { for (MatchObserver o : observers) o.onMatchUpdated(this); }
    private void notifyEventAdded(MatchEvent event) { for (MatchObserver o : observers) o.onEventAdded(event); }
    private void notifyStatusChanged(String newStatus) { for (MatchObserver o : observers) o.onMatchStatusChanged(newStatus); }

    // --- ABSTRACT METHOD IMPLEMENTATIONS (CORE) ---

    @Override
    public void startMatch() {
        setMatchStatus(MatchStatus.LIVE.name());
        this.status = MatchStatus.LIVE.name(); // Keep status in sync with matchStatus
        this.matchPeriod = MatchPeriod.FIRST_HALF.name();
        this.currentMatchMinute = 0;
        
        notifyStatusChanged(MatchStatus.LIVE.name());
    }

    @Override
    public void endMatch() {
        setMatchStatus(MatchStatus.COMPLETED.name());
        this.status = MatchStatus.COMPLETED.name(); // Keep status in sync with matchStatus
        
        if (this.matchResult == null) determineWinner();
        
        notifyStatusChanged(MatchStatus.COMPLETED.name());
    }

    @Override
    public void addEvent(MatchEvent event) {
        processEvent(event);
    }

    @Override
    public boolean canStartMatch() {
        android.util.Log.d("FootballMatch", "=== canStartMatch() START ===");
        android.util.Log.d("FootballMatch", "Match ID: " + getEntityId());
        android.util.Log.d("FootballMatch", "Match Status: " + getMatchStatus());
        
        // Log teams object reference
        android.util.Log.d("FootballMatch", "teams object: " + (teams != null ? teams.toString() : "NULL"));
        
        if (teams == null || teams.size() < 2 || matchConfig == null) {
            android.util.Log.d("FootballMatch", "Basic validation failed - teams: " + (teams != null ? teams.size() : "null") + 
                              ", config: " + (matchConfig != null ? "present" : "null"));
            return false;
        }
        
        if (!getMatchStatus().equals(MatchStatus.SCHEDULED.name())) {
            android.util.Log.d("FootballMatch", "Match status is not SCHEDULED: " + getMatchStatus());
            return false;
        }
        
        // Check minimum players requirement
        if (matchConfig instanceof FootballMatchConfig) {
            FootballMatchConfig config = (FootballMatchConfig) matchConfig;
            int minPlayers = config.getPlayersPerSide();
            
            android.util.Log.d("FootballMatch", "Minimum players required: " + minPlayers);
            
            // Verify each team has minimum required players
            int teamIndex = 0;
            for (MatchTeam team : teams) {
                android.util.Log.d("FootballMatch", "Checking team " + teamIndex);
                android.util.Log.d("FootballMatch", "  - Team ID: " + team.getTeamId());
                android.util.Log.d("FootballMatch", "  - Team Name: " + team.getTeamName());
                android.util.Log.d("FootballMatch", "  - Players list: " + (team.getPlayers() != null ? "NOT NULL" : "NULL"));
                
                int playerCount = (team.getPlayers() != null) ? team.getPlayers().size() : 0;
                android.util.Log.d("FootballMatch", "  - Player count: " + playerCount);
                
                // Log each player
                if (team.getPlayers() != null) {
                    for (int i = 0; i < team.getPlayers().size(); i++) {
                        Player p = team.getPlayers().get(i);
                        android.util.Log.d("FootballMatch", "    Player " + i + ": " + 
                            (p != null ? (p.getPlayerName() + " (ID: " + p.getPlayerId() + ")") : "NULL"));
                    }
                }
                
                if (team.getPlayers() == null || team.getPlayers().size() < minPlayers) {
                    android.util.Log.d("FootballMatch", "Team " + teamIndex + " does not have enough players (need " + minPlayers + ", have " + playerCount + ")");
                    return false;
                }
                teamIndex++;
            }
        }
        
        android.util.Log.d("FootballMatch", "=== canStartMatch() returning TRUE ===");
        return true;
    }

    // =========================================================================
    // COMMAND PATTERN IMPLEMENTATIONS (Required by Match.java)
    // =========================================================================

    @Override
    public void addMatchEvent(MatchEvent event) {
        if (event instanceof FootballEvent) {
            this.footballEvents.add((FootballEvent) event);
        }
    }

    @Override
    public void removeMatchEvent(MatchEvent event) {
        this.footballEvents.remove(event);
    }

    @Override
    public void setHomeScore(int score) {
        this.homeScore = score;
        updateTeamScores();
    }

    @Override
    public void setAwayScore(int score) {
        this.awayScore = score;
        updateTeamScores();
    }

    @Override
    public String getHomeTeamId() {
        MatchTeam homeTeam = getHomeTeam();
        return homeTeam != null ? homeTeam.getTeamId() : null;
    }

    @Override
    public void performSubstitution(String playerOutId, String playerInId) {
        // Placeholder implementation for Command Pattern execution
    }

    // =========================================================================
    // NEW: CRICKET-SPECIFIC METHODS (Returning Safe Defaults for ISP/LSP)
    // =========================================================================
    
    /**
     * Implements Match.getRequiredRunRate(). Not applicable to Football.
     */
    @Override
    public float getRequiredRunRate() {
        return 0.0f; 
    }

    /**
     * Implements Match.getRemainingBalls(). Not applicable to Football.
     */
    @Override
    public int getRemainingBalls() {
        return 0;
    }

    /**
     * Implements Match.getExtrasCount(). Not applicable to Football.
     */
    @Override
    public int getExtrasCount() {
        return 0;
    }

    // --- GETTERS, SETTERS, HELPERS, BUILDER ---

    private MatchTeam findTeamById(String teamId) {
        for (MatchTeam team : teams) if (team.getTeamId().equals(teamId)) return team;
        return null;
    }
    private MatchTeam getHomeTeam() {
        for (MatchTeam team : teams) if (team.isHomeTeam()) return team;
        return null;
    }
    private MatchTeam getAwayTeam() {
        for (MatchTeam team : teams) if (!team.isHomeTeam()) return team;
        return null;
    }
    
    public List<FootballEvent> getFootballEvents() { return footballEvents; }
    public void setFootballEvents(List<FootballEvent> footballEvents) { this.footballEvents = footballEvents; }
    public List<MatchTeam> getTeams() { return teams; }
    public void setTeams(List<MatchTeam> teams) { this.teams = teams; }
    public int getHomeScore() { return homeScore; }
    public int getAwayScore() { return awayScore; }
    public int getCurrentMatchMinute() { return currentMatchMinute; }
    public String getMatchPeriod() { return matchPeriod; }
    public MatchResult getMatchResult() { return matchResult; }
    
    // Timer state getters/setters
    public long getElapsedTimeMillis() { return elapsedTimeMillis; }
    public void setElapsedTimeMillis(long elapsedTimeMillis) { this.elapsedTimeMillis = elapsedTimeMillis; }
    public boolean isTimerRunning() { return timerRunning; }
    public void setTimerRunning(boolean timerRunning) { this.timerRunning = timerRunning; }


    public static class Builder extends Match.Builder<Builder> {
        private FootballMatchConfig config;
        private List<MatchTeam> teams;

        public Builder(String name, String hostUserId) {
            super(name, hostUserId);
            this.teams = new ArrayList<>();
        }

        public Builder withConfig(FootballMatchConfig config) {
            this.config = config;
            return this;
        }

        public Builder addTeam(MatchTeam team) {
            this.teams.add(team);
            return this;
        }

        public Builder withTeams(List<MatchTeam> teams) {
            this.teams = teams;
            return this;
        }

        @Override
        protected Builder self() { return this; }

        @Override
        public FootballMatch build() {
            FootballMatch match = new FootballMatch();
            
            match.setEntityId(UUID.randomUUID().toString());
            match.setName(this.name);
            match.setHostUserId(this.hostUserId);
            match.setCreatedAt(new Date());
            match.setOnline(false);
            
            match.setSportId(SportTypeEnum.FOOTBALL.name());
            
            match.setMatchDate(this.matchDate);
            match.setVenue(this.venue);
            match.setMatchStatus(MatchStatus.SCHEDULED.name());
            match.setStatus("DRAFT");

            match.setMatchConfig(this.config != null ? this.config : new FootballMatchConfig());
            match.setTeams(this.teams);

            return match;
        }
    }
}