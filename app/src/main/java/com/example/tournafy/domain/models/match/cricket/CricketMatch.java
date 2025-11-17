package com.example.tournafy.domain.models.match.cricket;

import com.example.tournafy.domain.enums.MatchStatus;
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
 * Concrete implementation of a Match for Cricket.
 * Extends the abstract Match class and provides cricket-specific logic.
 * This is the STATE MACHINE for cricket matches - all state changes happen through processEvent().
 * This class implements the Builder Pattern for easy construction.
 */
public class CricketMatch extends Match {

    // Cricket-specific relational data
    private List<Innings> innings;
    private List<CricketEvent> cricketEvents;
    private List<MatchTeam> teams; // List of teams participating
    
    // State tracking
    private int currentInningsNumber;
    private int targetScore; // For second innings
    private List<Over> currentOvers; // All overs in current innings
    
    // Observer Pattern
    private List<MatchObserver> observers;
    
    // Match Result
    private MatchResult matchResult;

    /**
     * Constructor initializes the match in SCHEDULED state.
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
        this.currentOvers = new ArrayList<>();
        this.observers = new ArrayList<>();
        
        this.currentInningsNumber = 0;
        this.targetScore = 0;
    }

    // --- --- --- --- --- --- --- --- ---
    //   CORE EVENT-DRIVEN LOGIC
    // --- --- --- --- --- --- --- --- ---

    /**
     * The ONLY public method for changing match state.
     * All scoring, wickets, and state transitions happen here.
     * This is the heart of the event-driven architecture.
     * 
     * @param event The MatchEvent to process (will be cast to CricketEvent)
     */
    public void processEvent(MatchEvent event) {
        if (!(event instanceof CricketEvent)) {
            throw new IllegalArgumentException("Event must be a CricketEvent for CricketMatch");
        }
        
        CricketEvent cricketEvent = (CricketEvent) event;
        
        // Add event to timeline
        this.cricketEvents.add(cricketEvent);
        
        // Get current innings and over
        Innings currentInnings = getCurrentInnings();
        if (currentInnings == null) {
            // Match hasn't started properly - can't process events yet
            return;
        }
        
        Over currentOver = getCurrentOver();
        if (currentOver == null) {
            // No over exists, create one
            currentOver = createNewOver(currentInnings);
        }
        
        // --- STEP 1: Create and add Ball to the Over ---
        Ball ball = createBallFromEvent(cricketEvent);
        if (currentOver.getBalls() == null) {
            currentOver.setBalls(new ArrayList<>());
        }
        currentOver.getBalls().add(ball);
        
        // --- STEP 2: Update Innings Score ---
        int runsToAdd = cricketEvent.getTotalRuns();
        currentInnings.setTotalRuns(currentInnings.getTotalRuns() + runsToAdd);
        currentOver.setRunsInOver(currentOver.getRunsInOver() + runsToAdd);
        
        // --- STEP 3: Handle Wickets ---
        if (cricketEvent.isWicket()) {
            currentInnings.setWicketsFallen(currentInnings.getWicketsFallen() + 1);
            currentOver.setWicketsInOver(currentOver.getWicketsInOver() + 1);
        }
        
        // --- STEP 4: Check if this is a legal delivery ---
        if (cricketEvent.isLegalDelivery()) {
            // Legal ball counts towards the over
            int legalBallsInOver = countLegalBalls(currentOver);
            
            // Check if over is complete (6 legal balls)
            if (legalBallsInOver >= 6) {
                endOver(currentInnings, currentOver);
            }
        }
        
        // --- STEP 5: Check if innings should end ---
        CricketMatchConfig config = (CricketMatchConfig) this.matchConfig;
        boolean inningsComplete = false;
        
        // Check all wickets down (assuming 10 wickets, or playersPerSide - 1)
        if (currentInnings.getWicketsFallen() >= 10) {
            inningsComplete = true;
        }
        
        // Check all overs bowled
        if (currentInnings.getOversCompleted() >= config.getNumberOfOvers()) {
            inningsComplete = true;
        }
        
        // For second innings: check if target achieved or impossible
        if (currentInningsNumber == 2 && targetScore > 0) {
            if (currentInnings.getTotalRuns() >= targetScore) {
                inningsComplete = true; // Target achieved
            } else if (currentInnings.getWicketsFallen() >= 10) {
                inningsComplete = true; // All out
            }
        }
        
        if (inningsComplete) {
            endInnings(currentInnings);
        }
        
        // --- STEP 6: Notify all observers ---
        notifyObservers();
        notifyEventAdded(cricketEvent);
    }

    // --- --- --- --- --- --- --- --- ---
    //   STATE MANAGEMENT METHODS
    // --- --- --- --- --- --- --- --- ---

    /**
     * Creates a new Over object and adds it to the current innings.
     */
    private Over createNewOver(Innings innings) {
        Over over = new Over();
        over.setOverId(UUID.randomUUID().toString());
        over.setInningsId(innings.getInningsId());
        over.setOverNumber(innings.getOversCompleted() + 1);
        over.setBalls(new ArrayList<>());
        over.setCompleted(false);
        over.setRunsInOver(0);
        over.setWicketsInOver(0);
        
        this.currentOvers.add(over);
        return over;
    }

    /**
     * Marks the current over as complete and increments overs_completed.
     * Creates a new over for the next set of deliveries.
     */
    private void endOver(Innings innings, Over over) {
        over.setCompleted(true);
        innings.setOversCompleted(innings.getOversCompleted() + 1);
        
        // Create a new over for next deliveries (if innings continues)
        if (!innings.isCompleted()) {
            createNewOver(innings);
        }
    }

    /**
     * Marks the current innings as complete.
     * If first innings, set target. If second innings, determine winner.
     */
    private void endInnings(Innings innings) {
        innings.setCompleted(true);
        
        if (currentInningsNumber == 1) {
            // First innings complete - set target for second innings
            this.targetScore = innings.getTotalRuns() + 1;
            
            // Check if there's a second innings to play
            if (this.innings.size() > 1) {
                currentInningsNumber = 2;
                this.currentOvers.clear(); // Reset overs for new innings
            } else {
                // No second innings defined - match ends
                determineWinner();
            }
        } else if (currentInningsNumber == 2) {
            // Second innings complete - determine winner
            determineWinner();
        }
    }

    /**
     * Compares final scores and determines the winner.
     * Updates matchStatus and creates MatchResult.
     */
    private void determineWinner() {
        if (innings.size() < 2) {
            // Can't determine winner without two innings
            this.matchStatus = MatchStatus.COMPLETED.name();
            this.status = "COMPLETED";
            return;
        }
        
        Innings firstInnings = innings.get(0);
        Innings secondInnings = innings.get(1);
        
        int firstScore = firstInnings.getTotalRuns();
        int secondScore = secondInnings.getTotalRuns();
        
        // Create MatchResult
        this.matchResult = new MatchResult();
        this.matchResult.setMatchId(this.entityId);
        this.matchResult.setResultId(UUID.randomUUID().toString());
        
        if (secondScore >= firstScore) {
            // Second batting team wins
            this.winnerTeamId = secondInnings.getBattingTeamId();
            this.matchResult.setWinnerTeamId(secondInnings.getBattingTeamId());
            this.matchResult.setLoserTeamId(firstInnings.getBattingTeamId());
            this.matchResult.setResultType("WIN");
            
            int wicketsRemaining = 10 - secondInnings.getWicketsFallen();
            this.matchResult.setWinMargin(wicketsRemaining + " wickets");
            
        } else if (firstScore > secondScore) {
            // First batting team wins
            this.winnerTeamId = firstInnings.getBattingTeamId();
            this.matchResult.setWinnerTeamId(firstInnings.getBattingTeamId());
            this.matchResult.setLoserTeamId(secondInnings.getBattingTeamId());
            this.matchResult.setResultType("WIN");
            
            int runDifference = firstScore - secondScore;
            this.matchResult.setWinMargin(runDifference + " runs");
            
        } else {
            // Tie
            this.matchResult.setResultType("TIE");
            this.matchResult.setWinMargin("Match Tied");
        }
        
        this.matchResult.setWinnerScore(firstScore > secondScore ? firstScore : secondScore);
        this.matchResult.setLoserScore(firstScore > secondScore ? secondScore : firstScore);
        
        // Update match status
        this.matchStatus = MatchStatus.COMPLETED.name();
        this.status = "COMPLETED";
        
        // Notify observers of status change
        notifyStatusChanged(MatchStatus.COMPLETED.name());
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
     * Gets the current active innings.
     */
    public Innings getCurrentInnings() {
        if (currentInningsNumber == 0 || innings.isEmpty()) {
            return null;
        }
        return innings.get(currentInningsNumber - 1);
    }

    /**
     * Gets the current active over.
     */
    public Over getCurrentOver() {
        if (currentOvers.isEmpty()) {
            return null;
        }
        // Return the last over (most recent)
        return currentOvers.get(currentOvers.size() - 1);
    }

    /**
     * Counts legal balls in an over (excludes wides, no-balls, etc.)
     */
    private int countLegalBalls(Over over) {
        if (over.getBalls() == null) {
            return 0;
        }
        
        int legalCount = 0;
        for (Ball ball : over.getBalls()) {
            if (ball.getExtrasType() == null || ball.getExtrasType().equals("NONE")) {
                legalCount++;
            }
        }
        return legalCount;
    }

    /**
     * Creates a Ball object from a CricketEvent.
     */
    private Ball createBallFromEvent(CricketEvent event) {
        Ball ball = new Ball();
        ball.setBallId(UUID.randomUUID().toString());
        ball.setBallNumber(event.getBallNumber());
        ball.setBatsmanId(event.getBatsmanStrikerId());
        ball.setBowlerId(event.getBowlerId());
        ball.setRunsScored(event.getTotalRuns());
        ball.setWicket(event.isWicket());
        ball.setExtrasType(event.getExtrasType());
        
        if (event.isWicket() && event.getWicketDetail() != null) {
            ball.setWicketType(event.getWicketDetail().getWicketType());
        }
        
        return ball;
    }

    // --- Abstract Method Implementations ---

    @Override
    public void startMatch() {
        // Set the match status to LIVE
        this.matchStatus = MatchStatus.LIVE.name();
        this.status = "ACTIVE";
        
        // Initialize first innings if not already done
        if (innings.isEmpty() && teams.size() >= 2) {
            // Create first innings
            Innings firstInnings = new Innings();
            firstInnings.setInningsId(UUID.randomUUID().toString());
            firstInnings.setMatchId(this.entityId);
            firstInnings.setInningsNumber(1);
            firstInnings.setBattingTeamId(teams.get(0).getTeamId());
            firstInnings.setBowlingTeamId(teams.get(1).getTeamId());
            firstInnings.setTotalRuns(0);
            firstInnings.setWicketsFallen(0);
            firstInnings.setOversCompleted(0);
            firstInnings.setCompleted(false);
            
            innings.add(firstInnings);
            currentInningsNumber = 1;
            
            // Create second innings placeholder
            Innings secondInnings = new Innings();
            secondInnings.setInningsId(UUID.randomUUID().toString());
            secondInnings.setMatchId(this.entityId);
            secondInnings.setInningsNumber(2);
            secondInnings.setBattingTeamId(teams.get(1).getTeamId());
            secondInnings.setBowlingTeamId(teams.get(0).getTeamId());
            secondInnings.setTotalRuns(0);
            secondInnings.setWicketsFallen(0);
            secondInnings.setOversCompleted(0);
            secondInnings.setCompleted(false);
            
            innings.add(secondInnings);
        }
        
        notifyStatusChanged(MatchStatus.LIVE.name());
    }

    @Override
    public void endMatch() {
        // Set the match status to COMPLETED
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

    public int getCurrentInningsNumber() {
        return currentInningsNumber;
    }

    public int getTargetScore() {
        return targetScore;
    }

    public MatchResult getMatchResult() {
        return matchResult;
    }

    public List<Over> getCurrentOvers() {
        return currentOvers;
    }

    // --- --- --- --- --- --- --- --- ---
    //   COMMAND PATTERN SUPPORT METHODS
    // --- --- --- --- --- --- --- --- ---

    /**
     * Starts a new over in the current innings.
     * Used by EndOverCommand to transition to next over.
     */
    public void startNewOver() {
        Innings currentInnings = getCurrentInnings();
        if (currentInnings != null && !currentInnings.isCompleted()) {
            createNewOver(currentInnings);
        }
    }

    /**
     * Removes the last over from the current innings.
     * Used by EndOverCommand undo operation.
     */
    public void removeLastOver() {
        if (!currentOvers.isEmpty()) {
            Over lastOver = currentOvers.remove(currentOvers.size() - 1);
            Innings currentInnings = getCurrentInnings();
            if (currentInnings != null && lastOver.isCompleted()) {
                currentInnings.setOversCompleted(currentInnings.getOversCompleted() - 1);
            }
        }
    }

    /**
     * Sets the current over.
     * Used by command pattern to restore state.
     */
    public void setCurrentOver(Over over) {
        if (!currentOvers.isEmpty()) {
            currentOvers.set(currentOvers.size() - 1, over);
        } else {
            currentOvers.add(over);
        }
    }

    /**
     * Adds a cricket event to the match timeline.
     * Used by command pattern for undo/redo support.
     */
    public void addMatchEvent(CricketEvent event) {
        if (!cricketEvents.contains(event)) {
            cricketEvents.add(event);
        }
    }

    /**
     * Removes a cricket event from the match timeline.
     * Used by command pattern undo operations.
     */
    public void removeMatchEvent(CricketEvent event) {
        cricketEvents.remove(event);
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