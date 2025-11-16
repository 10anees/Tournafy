package com.example.tournafy.domain.models.match.football;

import com.example.tournafy.domain.enums.MatchStatus;
import com.example.tournafy.domain.enums.football.MatchPeriod;
import com.example.tournafy.domain.interfaces.MatchObserver;
import com.example.tournafy.domain.models.base.Match;
import com.example.tournafy.domain.models.base.MatchEvent;
import com.example.tournafy.domain.models.match.MatchResult;
import com.example.tournafy.domain.models.sport.SportTypeEnum;
import com.example.tournafy.domain.models.team.MatchTeam;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Concrete implementation of a Match for Football.
 * Extends the abstract Match class and provides football-specific logic.
 * This is the EVENT LOG for football matches - all state changes happen through processEvent().
 * This class implements the Builder Pattern for easy construction.
 */
public class FootballMatch extends Match {

    private List<FootballEvent> footballEvents;
    private List<MatchTeam> teams; // List of teams participating
    
    // State tracking
    private int homeScore;
    private int awayScore;
    private int currentMatchMinute;
    private String matchPeriod; // FIRST_HALF, SECOND_HALF, EXTRA_TIME_FIRST, EXTRA_TIME_SECOND, PENALTY_SHOOTOUT
    
    // Observer Pattern
    private List<MatchObserver> observers;
    
    // Match Result
    private MatchResult matchResult;

    public FootballMatch() {
        super();
        this.entityType = "MATCH";
        this.sportId = SportTypeEnum.FOOTBALL.name();
        this.matchStatus = MatchStatus.SCHEDULED.name();

        this.footballEvents = new ArrayList<>();
        this.teams = new ArrayList<>();
        this.observers = new ArrayList<>();
        
        this.homeScore = 0;
        this.awayScore = 0;
        this.currentMatchMinute = 0;
        this.matchPeriod = MatchPeriod.FIRST_HALF.name();
    }

    // --- --- --- --- --- --- --- --- ---
    //   CORE EVENT-DRIVEN LOGIC
    // --- --- --- --- --- --- --- --- ---

    /**
     * The ONLY public method for changing match state.
     * All goals, cards, substitutions, and time updates happen here.
     * This is the heart of the event-driven architecture.
     * 
     * @param event The MatchEvent to process (will be cast to FootballEvent)
     */
    public void processEvent(MatchEvent event) {
        if (!(event instanceof FootballEvent)) {
            throw new IllegalArgumentException("Event must be a FootballEvent for FootballMatch");
        }
        
        FootballEvent footballEvent = (FootballEvent) event;
        
        // Set current scores at the time of event
        footballEvent.setHomeScoreAtEvent(this.homeScore);
        footballEvent.setAwayScoreAtEvent(this.awayScore);
        
        // Add event to timeline
        this.footballEvents.add(footballEvent);
        
        // Update match time from event
        if (footballEvent.getMatchMinute() > 0) {
            this.currentMatchMinute = footballEvent.getMatchMinute();
        }
        
        // Update match period if specified in event
        if (footballEvent.getMatchPeriod() != null && !footballEvent.getMatchPeriod().isEmpty()) {
            this.matchPeriod = footballEvent.getMatchPeriod();
        }
        
        // --- PROCESS EVENT BY CATEGORY ---
        String eventCategory = footballEvent.getEventCategory();
        
        if (eventCategory == null) {
            return; // Invalid event
        }
        
        switch (eventCategory.toUpperCase()) {
            case "GOAL":
                handleGoalEvent(footballEvent);
                break;
                
            case "CARD":
                handleCardEvent(footballEvent);
                break;
                
            case "SUBSTITUTION":
                handleSubstitutionEvent(footballEvent);
                break;
                
            case "SHOT":
                handleShotEvent(footballEvent);
                break;
                
            case "SAVE":
                handleSaveEvent(footballEvent);
                break;
                
            case "PERIOD_END":
                handlePeriodEndEvent(footballEvent);
                break;
                
            case "KICK_OFF":
            case "FREE_KICK":
            case "CORNER":
            case "THROW_IN":
            case "OFFSIDE":
            case "FOUL":
                // These are logged but don't change score
                break;
                
            default:
                // Unknown event category - just log it
                break;
        }
        
        // Check if match should end based on time
        checkMatchCompletion();
        
        // --- Notify all observers ---
        notifyObservers();
        notifyEventAdded(footballEvent);
    }

    /**
     * Public method to update match time (for real-time clock).
     * This allows external clock/timer to advance the match time.
     * 
     * @param newMinute The new match minute
     */
    public void updateMatchTime(int newMinute) {
        this.currentMatchMinute = newMinute;
        
        // Check if period should end
        FootballMatchConfig config = (FootballMatchConfig) this.matchConfig;
        int halfDuration = config.getMatchDuration() / 2;
        
        if (this.matchPeriod.equals(MatchPeriod.FIRST_HALF.name()) && newMinute >= halfDuration) {
            // First half should end
            endMatchPeriod();
        } else if (this.matchPeriod.equals(MatchPeriod.SECOND_HALF.name()) && newMinute >= config.getMatchDuration()) {
            // Second half should end (full time)
            endMatchPeriod();
        }
        
        notifyObservers();
    }

    // --- --- --- --- --- --- --- --- ---
    //   EVENT HANDLERS
    // --- --- --- --- --- --- --- --- ---

    /**
     * Handles GOAL events - updates score based on team and own goal status.
     */
    private void handleGoalEvent(FootballEvent event) {
        FootballGoalDetail goalDetail = event.getGoalDetail();
        if (goalDetail == null) {
            return;
        }
        
        // Determine which team scored
        MatchTeam scoringTeam = findTeamById(event.getTeamId());
        if (scoringTeam == null) {
            return;
        }
        
        boolean isHomeTeam = scoringTeam.isHomeTeam();
        boolean isOwnGoal = goalDetail.isOwnGoal();
        
        // Update scores - own goals go to opposing team
        if (isOwnGoal) {
            // Own goal - credit to opposing team
            if (isHomeTeam) {
                this.awayScore++;
            } else {
                this.homeScore++;
            }
        } else {
            // Normal goal
            if (isHomeTeam) {
                this.homeScore++;
            } else {
                this.awayScore++;
            }
        }
        
        // Update MatchTeam scores
        updateTeamScores();
    }

    /**
     * Handles CARD events - logged for timeline/stats, no score change.
     */
    private void handleCardEvent(FootballEvent event) {
        FootballCardDetail cardDetail = event.getCardDetail();
        if (cardDetail == null) {
            return;
        }
        
        // Cards don't change score, but are important for stats
        // Could track red cards to check if match should be abandoned
        // (if a team has < 7 players, match typically ends)
    }

    /**
     * Handles SUBSTITUTION events - logged for timeline/stats.
     */
    private void handleSubstitutionEvent(FootballEvent event) {
        FootballSubstitutionDetail subDetail = event.getSubstitutionDetail();
        if (subDetail == null) {
            return;
        }
        
        // Substitutions don't change score, but are important for tracking
        // active players and statistics
    }

    /**
     * Handles SHOT events - logged for statistics.
     */
    private void handleShotEvent(FootballEvent event) {
        // Shots are tracked for statistics but don't change score
        // unless they result in a goal (which would be a separate GOAL event)
    }

    /**
     * Handles SAVE events - logged for goalkeeper statistics.
     */
    private void handleSaveEvent(FootballEvent event) {
        // Saves are tracked for goalkeeper statistics
    }

    /**
     * Handles PERIOD_END events - transitions between halves.
     */
    private void handlePeriodEndEvent(FootballEvent event) {
        endMatchPeriod();
    }

    // --- --- --- --- --- --- --- --- ---
    //   STATE MANAGEMENT METHODS
    // --- --- --- --- --- --- --- --- ---

    /**
     * Ends the current match period and transitions to the next.
     */
    private void endMatchPeriod() {
        if (this.matchPeriod.equals(MatchPeriod.FIRST_HALF.name())) {
            // Move to second half
            this.matchPeriod = MatchPeriod.SECOND_HALF.name();
            notifyStatusChanged("HALF_TIME");
            
        } else if (this.matchPeriod.equals(MatchPeriod.SECOND_HALF.name())) {
            // Full time - determine winner
            determineWinner();
            
        } else if (this.matchPeriod.equals(MatchPeriod.EXTRA_TIME_1.name())) {
            // Move to second half of extra time
            this.matchPeriod = MatchPeriod.EXTRA_TIME_2.name();
            
        } else if (this.matchPeriod.equals(MatchPeriod.EXTRA_TIME_2.name())) {
            // Extra time complete - check for penalties or end
            if (this.homeScore == this.awayScore) {
                this.matchPeriod = MatchPeriod.PENALTY_SHOOTOUT.name();
            } else {
                determineWinner();
            }
        }
    }

    /**
     * Checks if match time has reached full duration and should end.
     */
    private void checkMatchCompletion() {
        FootballMatchConfig config = (FootballMatchConfig) this.matchConfig;
        
        if (this.matchPeriod.equals(MatchPeriod.SECOND_HALF.name()) 
                && this.currentMatchMinute >= config.getMatchDuration()) {
            endMatchPeriod();
        }
    }

    /**
     * Compares final scores and determines the winner.
     * Updates matchStatus and creates MatchResult.
     */
    private void determineWinner() {
        // Create MatchResult
        this.matchResult = new MatchResult();
        this.matchResult.setMatchId(this.entityId);
        this.matchResult.setResultId(UUID.randomUUID().toString());
        this.matchResult.setWinnerScore(Math.max(homeScore, awayScore));
        this.matchResult.setLoserScore(Math.min(homeScore, awayScore));
        
        if (this.homeScore > this.awayScore) {
            // Home team wins
            MatchTeam homeTeam = getHomeTeam();
            MatchTeam awayTeam = getAwayTeam();
            
            if (homeTeam != null && awayTeam != null) {
                this.winnerTeamId = homeTeam.getTeamId();
                this.matchResult.setWinnerTeamId(homeTeam.getTeamId());
                this.matchResult.setLoserTeamId(awayTeam.getTeamId());
                this.matchResult.setResultType("WIN");
                
                int goalDifference = homeScore - awayScore;
                this.matchResult.setWinMargin(goalDifference + " goal" + (goalDifference > 1 ? "s" : ""));
            }
            
        } else if (this.awayScore > this.homeScore) {
            // Away team wins
            MatchTeam homeTeam = getHomeTeam();
            MatchTeam awayTeam = getAwayTeam();
            
            if (homeTeam != null && awayTeam != null) {
                this.winnerTeamId = awayTeam.getTeamId();
                this.matchResult.setWinnerTeamId(awayTeam.getTeamId());
                this.matchResult.setLoserTeamId(homeTeam.getTeamId());
                this.matchResult.setResultType("WIN");
                
                int goalDifference = awayScore - homeScore;
                this.matchResult.setWinMargin(goalDifference + " goal" + (goalDifference > 1 ? "s" : ""));
            }
            
        } else {
            // Draw
            this.matchResult.setResultType("DRAW");
            this.matchResult.setWinMargin("Match Drawn");
        }
        
        // Update match status
        this.matchStatus = MatchStatus.COMPLETED.name();
        this.status = "COMPLETED";
        
        // Notify observers of status change
        notifyStatusChanged(MatchStatus.COMPLETED.name());
    }

    /**
     * Updates the MatchTeam scores to reflect current state.
     */
    private void updateTeamScores() {
        for (MatchTeam team : teams) {
            if (team.isHomeTeam()) {
                team.setScore(this.homeScore);
            } else {
                team.setScore(this.awayScore);
            }
        }
    }

    // --- --- --- --- --- --- --- --- ---
    //   OBSERVER PATTERN IMPLEMENTATION
    // --- --- --- --- --- --- --- --- ---

    /**
     * Register a new observer to receive match updates.
     */
    public void addObserver(MatchObserver observer) {
        if (!this.observers.contains(observer)) {
            this.observers.add(observer);
        }
    }

    /**
     * Remove an observer.
     */
    public void removeObserver(MatchObserver observer) {
        this.observers.remove(observer);
    }

    /**
     * Notify all observers of a match state update.
     */
    private void notifyObservers() {
        for (MatchObserver observer : observers) {
            observer.onMatchUpdated(this);
        }
    }

    /**
     * Notify all observers of a new event.
     */
    private void notifyEventAdded(MatchEvent event) {
        for (MatchObserver observer : observers) {
            observer.onEventAdded(event);
        }
    }

    /**
     * Notify all observers of a status change.
     */
    private void notifyStatusChanged(String newStatus) {
        for (MatchObserver observer : observers) {
            observer.onMatchStatusChanged(newStatus);
        }
    }

    // --- --- --- --- --- --- --- --- ---
    //   HELPER METHODS
    // --- --- --- --- --- --- --- --- ---

    /**
     * Finds a team by ID from the teams list.
     */
    private MatchTeam findTeamById(String teamId) {
        for (MatchTeam team : teams) {
            if (team.getTeamId().equals(teamId)) {
                return team;
            }
        }
        return null;
    }

    /**
     * Gets the home team.
     */
    private MatchTeam getHomeTeam() {
        for (MatchTeam team : teams) {
            if (team.isHomeTeam()) {
                return team;
            }
        }
        return null;
    }

    /**
     * Gets the away team.
     */
    private MatchTeam getAwayTeam() {
        for (MatchTeam team : teams) {
            if (!team.isHomeTeam()) {
                return team;
            }
        }
        return null;
    }

    // --- Abstract Method Implementations ---

    @Override
    public void startMatch() {
        this.matchStatus = MatchStatus.LIVE.name();
        this.status = "ACTIVE";
        this.matchPeriod = MatchPeriod.FIRST_HALF.name();
        this.currentMatchMinute = 0;
        
        notifyStatusChanged(MatchStatus.LIVE.name());
    }

    @Override
    public void endMatch() {
        this.matchStatus = MatchStatus.COMPLETED.name();
        this.status = "COMPLETED";
        
        // Determine winner if not already done
        if (this.matchResult == null) {
            determineWinner();
        }
        
        notifyStatusChanged(MatchStatus.COMPLETED.name());
    }

    @Override
    public void addEvent(MatchEvent event) {
        // Delegate to processEvent for proper state management
        processEvent(event);
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

    public int getHomeScore() {
        return homeScore;
    }

    public int getAwayScore() {
        return awayScore;
    }

    public int getCurrentMatchMinute() {
        return currentMatchMinute;
    }

    public String getMatchPeriod() {
        return matchPeriod;
    }

    public MatchResult getMatchResult() {
        return matchResult;
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