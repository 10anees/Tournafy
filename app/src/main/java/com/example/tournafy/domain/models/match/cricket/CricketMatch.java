package com.example.tournafy.domain.models.match.cricket;

import com.example.tournafy.domain.enums.MatchStatus;
import com.example.tournafy.domain.interfaces.MatchObserver;
import com.example.tournafy.domain.models.base.Match;
import com.example.tournafy.domain.models.base.MatchEvent;
import com.example.tournafy.domain.models.match.MatchResult;
import com.example.tournafy.domain.models.sport.SportTypeEnum;
import com.example.tournafy.domain.models.team.MatchTeam;
import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Concrete implementation of a Match for Cricket.
 * Extends the abstract Match class and provides cricket-specific logic.
 */
public class CricketMatch extends Match {

    // Cricket-specific relational data
    private List<Innings> innings;
    private List<CricketEvent> cricketEvents; // For AddWicketCommand, AddExtrasCommand [cite: 99, 100]
    private List<MatchTeam> teams;

    // Toss information
    private String tossWinner; // Team name or ID that won the toss
    private String tossDecision; // "BAT" or "BOWL" - what winner chose

    // State tracking
    private int currentInningsNumber;
    private int targetScore;
    @Exclude
    private List<Over> currentOvers;
    
    // Player tracking (striker, non-striker, bowler)
    private String currentStrikerId;
    private String currentNonStrikerId;
    private String currentBowlerId;
    
    // Batting order queue - list of player IDs waiting to bat
    private List<String> battingOrderQueue;
    
    // Bowling order queue - list of player IDs in bowling rotation
    private List<String> bowlingOrderQueue;
    
    // Player statistics tracking
    private Map<String, BatsmanStats> batsmanStatsMap;
    private Map<String, BowlerStats> bowlerStatsMap;

    // Observer Pattern
    @Exclude
    private List<MatchObserver> observers;

    // Match Result
    private MatchResult matchResult;

    public CricketMatch() {
        super();
        this.entityType = "MATCH";
        this.sportId = SportTypeEnum.CRICKET.name();
        setMatchStatus(MatchStatus.SCHEDULED.name());

        this.innings = new ArrayList<>();
        this.cricketEvents = new ArrayList<>();
        this.teams = new ArrayList<>();
        this.currentOvers = new ArrayList<>();
        this.observers = new ArrayList<>();
        this.batsmanStatsMap = new HashMap<>();
        this.bowlerStatsMap = new HashMap<>();
        this.battingOrderQueue = new ArrayList<>();
        this.bowlingOrderQueue = new ArrayList<>();

        this.currentInningsNumber = 0;
        this.targetScore = 0;
    }

    // --- CORE LOGIC (processEvent) ---

    public void processEvent(MatchEvent event) {
        if (!(event instanceof CricketEvent)) {
            throw new IllegalArgumentException("Event must be a CricketEvent for CricketMatch");
        }

        CricketEvent cricketEvent = (CricketEvent) event;

        Innings currentInnings = getCurrentInnings();
        if (currentInnings == null) {
            throw new IllegalStateException("Cannot process event: No current innings. Did you start the match?");
        }

        Over currentOver = getCurrentOver();
        if (currentOver == null) {
            currentOver = createNewOver(currentInnings);
        }

        // Fill Foreign Keys
        if (cricketEvent.getMatchId() == null) cricketEvent.setMatchId(this.entityId);
        if (cricketEvent.getTeamId() == null) cricketEvent.setTeamId(currentInnings.getBattingTeamId());
        if (cricketEvent.getOverNumber() == 0) cricketEvent.setOverNumber(currentOver.getOverNumber());
        if (cricketEvent.getBallNumber() == 0) cricketEvent.setBallNumber(currentOver.getBalls() != null ? currentOver.getBalls().size() + 1 : 1);
        if (cricketEvent.getEventId() == null) cricketEvent.setEventId(UUID.randomUUID().toString());
        if (cricketEvent.getEventTime() == null) cricketEvent.setEventTime(new Date());

        this.cricketEvents.add(cricketEvent);

        // Update State
        Ball ball = createBallFromEvent(cricketEvent);
        if (currentOver.getBalls() == null) currentOver.setBalls(new ArrayList<>());
        currentOver.getBalls().add(ball);

        int runsToAdd = cricketEvent.getTotalRuns();
        currentInnings.setTotalRuns(currentInnings.getTotalRuns() + runsToAdd);
        currentOver.setRunsInOver(currentOver.getRunsInOver() + runsToAdd);

        // Swap strikers on odd runs (1, 3, 5) for legal deliveries
        if (cricketEvent.isLegalDelivery() && !cricketEvent.isWicket()) {
            int batRuns = cricketEvent.getRunsScoredBat();
            if (batRuns % 2 == 1) {  // Odd runs (1, 3, 5)
                swapStrikers();
            }
        }

        if (cricketEvent.isWicket()) {
            currentInnings.setWicketsFallen(currentInnings.getWicketsFallen() + 1);
            currentOver.setWicketsInOver(currentOver.getWicketsInOver() + 1);
            // TODO: Update striker to next batsman from team roster
        }

        if (cricketEvent.isLegalDelivery()) {
            int legalBallsInOver = countLegalBalls(currentOver);
            if (legalBallsInOver >= 6) {
                endOver(currentInnings, currentOver);
                // Swap strikers at end of over
                swapStrikers();
            }
        }

        // Check completion
        CricketMatchConfig config = (CricketMatchConfig) this.matchConfig;
        if (config == null) {
            throw new IllegalStateException("Cannot process event: Match config is null");
        }
        
        boolean inningsComplete = false;

        // Calculate max wickets based on config
        CricketMatchConfig cricketConfig = (CricketMatchConfig) this.matchConfig;
        int playersPerSide = cricketConfig.getPlayersPerSide();
        int maxWickets;
        
        if (cricketConfig.isLastManStanding()) {
            // Last man standing: the last batsman can bat alone, so all players can be out
            // maxWickets = playersPerSide (e.g., 5 players = 5 wickets)
            maxWickets = Math.min(10, playersPerSide);
        } else {
            // Standard cricket: need 2 batsmen on field, so innings ends when players - 1 are out
            // maxWickets = playersPerSide - 1 (e.g., 5 players = 4 wickets)
            maxWickets = Math.min(10, Math.max(1, playersPerSide - 1));
        }
        
        // Standard completion conditions
        if (currentInnings.getWicketsFallen() >= maxWickets) inningsComplete = true;
        if (currentInnings.getOversCompleted() >= config.getNumberOfOvers()) inningsComplete = true;

        // Second innings specific conditions
        if (currentInningsNumber == 2 && targetScore > 0) {
            if (currentInnings.getTotalRuns() >= targetScore) inningsComplete = true;
            else if (currentInnings.getWicketsFallen() >= maxWickets) inningsComplete = true;
        }

        if (inningsComplete) {
            endInnings(currentInnings);
        }

        notifyObservers();
        notifyEventAdded(cricketEvent);
    }

    // --- STATE METHODS (createNewOver, endOver, endInnings, determineWinner) ---

    private Over createNewOver(Innings innings) {
        Over over = new Over();
        over.setOverId(UUID.randomUUID().toString());
        over.setInningsId(innings.getInningsId());
        over.setOverNumber(innings.getOversCompleted() + 1);
        over.setBalls(new ArrayList<>());
        over.setCompleted(false);

        this.currentOvers.add(over);
        return over;
    }

    private void endOver(Innings innings, Over over) {
        over.setCompleted(true);
        innings.setOversCompleted(innings.getOversCompleted() + 1);
        if (!innings.isCompleted()) createNewOver(innings);
    }

    private void endInnings(Innings innings) {
        innings.setCompleted(true);
        
        // End the current over if it exists
        if (getCurrentOver() != null) {
            getCurrentOver().setCompleted(true);
        }
        
        if (currentInningsNumber == 1) {
            this.targetScore = innings.getTotalRuns() + 1;
            if (this.innings.size() > 1) {
                currentInningsNumber = 2;
                this.currentOvers.clear();
                // Create first over for second innings
                Innings secondInnings = this.innings.get(1);
                createNewOver(secondInnings);
                
                // CRITICAL FIX: Reinitialize players for second innings
                // Find the batting team for second innings
                MatchTeam battingTeam = null;
                MatchTeam bowlingTeam = null;
                if (teams != null && teams.size() >= 2) {
                    for (MatchTeam team : teams) {
                        if (team.getTeamId().equals(secondInnings.getBattingTeamId())) {
                            battingTeam = team;
                        } else if (team.getTeamId().equals(secondInnings.getBowlingTeamId())) {
                            bowlingTeam = team;
                        }
                    }
                    // Initialize strikers from new batting team
                    if (battingTeam != null) {
                        initializeStrikers(battingTeam);
                    }
                    // Initialize bowler from new bowling team
                    if (bowlingTeam != null) {
                        initializeBowler(bowlingTeam);
                    }
                }
            } else {
                determineWinner();
            }
        } else if (currentInningsNumber == 2) {
            determineWinner();
        }
    }

    private void determineWinner() {
        if (innings.size() < 2) {
            setMatchStatus(MatchStatus.COMPLETED.name());
            return;
        }

        Innings firstInnings = innings.get(0);
        Innings secondInnings = innings.get(1);

        this.matchResult = new MatchResult();
        this.matchResult.setMatchId(this.entityId);
        this.matchResult.setResultId(UUID.randomUUID().toString());

        if (secondInnings.getTotalRuns() >= firstInnings.getTotalRuns()) {
            this.winnerTeamId = secondInnings.getBattingTeamId();
            this.matchResult.setWinnerTeamId(secondInnings.getBattingTeamId());
            this.matchResult.setResultType("WIN");
            this.matchResult.setWinMargin((10 - secondInnings.getWicketsFallen()) + " wickets");
        } else {
            this.winnerTeamId = firstInnings.getBattingTeamId();
            this.matchResult.setWinnerTeamId(firstInnings.getBattingTeamId());
            this.matchResult.setResultType("WIN");
            this.matchResult.setWinMargin((firstInnings.getTotalRuns() - secondInnings.getTotalRuns()) + " runs");
        }

        setMatchStatus(MatchStatus.COMPLETED.name());
        notifyStatusChanged(MatchStatus.COMPLETED.name());
    }

    // --- OBSERVERS ---

    public void addObserver(MatchObserver observer) { if (!observers.contains(observer)) observers.add(observer); }
    public void removeObserver(MatchObserver observer) { observers.remove(observer); }
    public void notifyObservers() { for (MatchObserver o : observers) o.onMatchUpdated(this); }
    public void notifyEventAdded(MatchEvent e) { for (MatchObserver o : observers) o.onEventAdded(e); }
    public void notifyStatusChanged(String s) { for (MatchObserver o : observers) o.onMatchStatusChanged(s); }

    // --- HELPERS (getCurrentInnings, createBallFromEvent, etc.) ---

    public Innings getCurrentInnings() {
        if (currentInningsNumber == 0 || innings == null || innings.isEmpty()) return null;
        return innings.get(currentInningsNumber - 1);
    }

    public Over getCurrentOver() {
        if (currentOvers == null || currentOvers.isEmpty()) return null;
        return currentOvers.get(currentOvers.size() - 1);
    }

    private int countLegalBalls(Over over) {
        if (over.getBalls() == null) return 0;
        int count = 0;
        for (Ball b : over.getBalls()) {
            if (b.isLegalDelivery()) count++;
        }
        return count;
    }

    /**
     * Gets the number of players available in a team.
     * Used to determine max wickets for innings completion.
     * 
     * @param teamId The ID of the team
     * @return The number of players, or 11 as default if team not found
     */
    private int getTeamPlayersCount(String teamId) {
        if (teams == null || teamId == null) return 11;  // Default to 11 players
        
        for (com.example.tournafy.domain.models.team.MatchTeam team : teams) {
            if (teamId.equals(team.getTeamId())) {
                if (team.getPlayers() != null) {
                    return team.getPlayers().size();
                }
            }
        }
        
        return 11;  // Default if team or players not found
    }

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
        
        // Set foreign keys from current match state
        ball.setMatchId(this.getEntityId());
        Innings currentInnings = getCurrentInnings();
        if (currentInnings != null) {
            ball.setInningsId(currentInnings.getInningsId());
            ball.setInningsNumber(currentInnings.getInningsNumber());
        }
        Over currentOver = getCurrentOver();
        if (currentOver != null) {
            ball.setOverId(currentOver.getOverId());
            ball.setOverNumber(currentOver.getOverNumber());
        }
        
        return ball;
    }

    /**
     * Initializes striker and non-striker from the batting team's first 2 players.
     */
    private void initializeStrikers(MatchTeam battingTeam) {
        if (battingTeam != null && battingTeam.getPlayers() != null && battingTeam.getPlayers().size() >= 2) {
            this.currentStrikerId = battingTeam.getPlayers().get(0).getPlayerId();
            this.currentNonStrikerId = battingTeam.getPlayers().get(1).getPlayerId();
        } else if (battingTeam != null && battingTeam.getPlayers() != null && battingTeam.getPlayers().size() == 1) {
            // Only one player, set striker and non-striker to same player (edge case)
            this.currentStrikerId = battingTeam.getPlayers().get(0).getPlayerId();
            this.currentNonStrikerId = null;
        }
    }

    /**
     * Initializes bowler from the bowling team's first player.
     */
    private void initializeBowler(MatchTeam bowlingTeam) {
        if (bowlingTeam != null && bowlingTeam.getPlayers() != null && !bowlingTeam.getPlayers().isEmpty()) {
            this.currentBowlerId = bowlingTeam.getPlayers().get(0).getPlayerId();
        }
    }

    /**
     * Swaps striker and non-striker positions.
     */
    private void swapStrikers() {
        String temp = this.currentStrikerId;
        this.currentStrikerId = this.currentNonStrikerId;
        this.currentNonStrikerId = temp;
    }

    // --- ABSTRACT IMPL (startMatch, endMatch, addEvent, canStartMatch) ---

    @Override
    public void startMatch() {
        if (teams == null || teams.size() < 2) {
            throw new IllegalStateException("Cannot start match: Need at least 2 teams");
        }
        
        if (matchConfig == null) {
            throw new IllegalStateException("Cannot start match: Match config is null");
        }
        
        setMatchStatus(MatchStatus.LIVE.name());

        if (innings.isEmpty() && teams.size() >= 2) {
            // Determine who bats first based on toss result
            MatchTeam firstBattingTeam;
            MatchTeam firstBowlingTeam;
            
            if (tossWinner != null && tossDecision != null) {
                // Find the team that won the toss
                MatchTeam tossWinningTeam = null;
                MatchTeam tossLosingTeam = null;
                
                for (MatchTeam team : teams) {
                    if (team.getTeamName().equals(tossWinner)) {
                        tossWinningTeam = team;
                    } else {
                        tossLosingTeam = team;
                    }
                }
                
                // Determine batting order based on toss decision
                if (tossDecision.equals("BAT")) {
                    // Toss winner chose to bat first
                    firstBattingTeam = tossWinningTeam;
                    firstBowlingTeam = tossLosingTeam;
                } else {
                    // Toss winner chose to bowl first (or field)
                    firstBattingTeam = tossLosingTeam;
                    firstBowlingTeam = tossWinningTeam;
                }
            } else {
                // Fallback: if no toss result, default to teams.get(0) batting first
                firstBattingTeam = teams.get(0);
                firstBowlingTeam = teams.get(1);
            }
            
            // Create first innings
            Innings first = new Innings();
            first.setInningsId(UUID.randomUUID().toString());
            first.setMatchId(this.entityId);
            first.setInningsNumber(1);
            first.setBattingTeamId(firstBattingTeam.getTeamId());
            first.setBowlingTeamId(firstBowlingTeam.getTeamId());
            innings.add(first);
            currentInningsNumber = 1;

            // Create second innings (teams swap roles)
            Innings second = new Innings();
            second.setInningsId(UUID.randomUUID().toString());
            second.setMatchId(this.entityId);
            second.setInningsNumber(2);
            second.setBattingTeamId(firstBowlingTeam.getTeamId());
            second.setBowlingTeamId(firstBattingTeam.getTeamId());
            innings.add(second);
            
            // Initialize striker and non-striker from batting team
            initializeStrikers(firstBattingTeam);
            
            // Initialize bowler from bowling team
            initializeBowler(firstBowlingTeam);
        }
        notifyStatusChanged(MatchStatus.LIVE.name());
    }

    @Override
    public void endMatch() {
        setMatchStatus(MatchStatus.COMPLETED.name());
        if (this.matchResult == null) determineWinner();
        notifyStatusChanged(MatchStatus.COMPLETED.name());
    }

    @Override
    public void addEvent(MatchEvent event) {
        processEvent(event);
    }

    @Override
    public boolean canStartMatch() {
        return teams != null && teams.size() >= 2 &&
                matchConfig != null &&
                getMatchStatus().equals(MatchStatus.SCHEDULED.name());
    }

    // =========================================================================
    // COMMAND PATTERN IMPLEMENTATIONS (Required by Match.java)
    // =========================================================================

    /**
     * Implements Match.addMatchEvent(MatchEvent event) for Undo/Redo support.
     * This method is called by the Command to register the event for persistence.
     */
    @Override
    public void addMatchEvent(MatchEvent event) {
        if (event instanceof CricketEvent) {
            this.cricketEvents.add((CricketEvent) event);
        }
        // NOTE: We rely on the Command implementation to handle score/state updates
        // before calling this method, or we assume the event is already processed
        // and we are just logging it for persistence/redo.
    }

    /**
     * Implements Match.removeMatchEvent(MatchEvent event) for Undo/Redo support.
     * This method is called by the Command to un-register the event.
     */
    @Override
    public void removeMatchEvent(MatchEvent event) {
        this.cricketEvents.remove(event);
    }
    
    // --- COMMAND HELPERS: FOOTBALL SPECIFIC (UNSUPPORTED IN CRICKET) ---

    /**
     * Implements Match.setHomeScore(int score). Not applicable to event-based Cricket scoring.
     * @throws UnsupportedOperationException as Cricket uses processEvent for state changes.
     */
    @Override
    public void setHomeScore(int score) {
        throw new UnsupportedOperationException("setHomeScore is not supported in CricketMatch. Use addEvent/processEvent.");
    }

    /**
     * Implements Match.setAwayScore(int score). Not applicable to event-based Cricket scoring.
     * @throws UnsupportedOperationException as Cricket uses processEvent for state changes.
     */
    @Override
    public void setAwayScore(int score) {
        throw new UnsupportedOperationException("setAwayScore is not supported in CricketMatch. Use addEvent/processEvent.");
    }

    /**
     * Implements Match.getHomeTeamId(). Cricket tracks teams via innings order.
     * We return the ID of the team batting first as the 'Home' contextually.
     * This prevents AddGoalCommand from crashing when trying to access this method.
     */
    @Override
    public String getHomeTeamId() {
        if (teams != null && !teams.isEmpty()) {
            // Assume the first team added is the 'Home' team for ID lookup
            return teams.get(0).getTeamId();
        }
        return null;
    }

    /**
     * Implements Match.performSubstitution(). Not applicable to Cricket.
     * @throws UnsupportedOperationException
     */
    @Override
    public void performSubstitution(String playerOutId, String playerInId) {
        throw new UnsupportedOperationException("Player substitution is not supported in CricketMatch for team lineup changes.");
    }

    // --- GETTERS ---

    public List<Innings> getInnings() { return innings; }
    public void setInnings(List<Innings> innings) { this.innings = innings; }
    public List<CricketEvent> getCricketEvents() { return cricketEvents; }
    public void setCricketEvents(List<CricketEvent> cricketEvents) { this.cricketEvents = cricketEvents; }
    public List<MatchTeam> getTeams() { return teams; }
    public void setTeams(List<MatchTeam> teams) { this.teams = teams; }
    public int getCurrentInningsNumber() { return currentInningsNumber; }
    public int getTargetScore() { return targetScore; }
    public MatchResult getMatchResult() { return matchResult; }
    public List<Over> getCurrentOvers() { return currentOvers; }
    public String getCurrentStrikerId() { return currentStrikerId; }
    public void setCurrentStrikerId(String currentStrikerId) { this.currentStrikerId = currentStrikerId; }
    public String getCurrentNonStrikerId() { return currentNonStrikerId; }
    public void setCurrentNonStrikerId(String currentNonStrikerId) { this.currentNonStrikerId = currentNonStrikerId; }
    public String getCurrentBowlerId() { return currentBowlerId; }
    public void setCurrentBowlerId(String currentBowlerId) { this.currentBowlerId = currentBowlerId; }
    
    // Batting/Bowling order queue getters/setters
    public List<String> getBattingOrderQueue() { 
        return battingOrderQueue != null ? battingOrderQueue : new ArrayList<>(); 
    }
    public void setBattingOrderQueue(List<String> battingOrderQueue) { 
        this.battingOrderQueue = battingOrderQueue; 
    }
    public List<String> getBowlingOrderQueue() { 
        return bowlingOrderQueue != null ? bowlingOrderQueue : new ArrayList<>(); 
    }
    public void setBowlingOrderQueue(List<String> bowlingOrderQueue) { 
        this.bowlingOrderQueue = bowlingOrderQueue; 
    }
    
    // Toss getters/setters
    public String getTossWinner() { return tossWinner; }
    public void setTossWinner(String tossWinner) { this.tossWinner = tossWinner; }
    public String getTossDecision() { return tossDecision; }
    public void setTossDecision(String tossDecision) { this.tossDecision = tossDecision; }
    
    // Stats getters/setters
    public Map<String, BatsmanStats> getBatsmanStatsMap() { 
        return batsmanStatsMap != null ? batsmanStatsMap : new HashMap<>(); 
    }
    public void setBatsmanStatsMap(Map<String, BatsmanStats> batsmanStatsMap) { 
        this.batsmanStatsMap = batsmanStatsMap; 
    }
    public Map<String, BowlerStats> getBowlerStatsMap() { 
        return bowlerStatsMap != null ? bowlerStatsMap : new HashMap<>(); 
    }
    public void setBowlerStatsMap(Map<String, BowlerStats> bowlerStatsMap) { 
        this.bowlerStatsMap = bowlerStatsMap; 
    }
    
    // Helper methods for stats
    public BatsmanStats getBatsmanStats(String playerId) {
        if (batsmanStatsMap == null) {
            batsmanStatsMap = new HashMap<>();
        }
        return batsmanStatsMap.get(playerId);
    }
    
    public void updateBatsmanStats(String playerId, BatsmanStats stats) {
        if (batsmanStatsMap == null) {
            batsmanStatsMap = new HashMap<>();
        }
        batsmanStatsMap.put(playerId, stats);
    }
    
    public BowlerStats getBowlerStats(String playerId) {
        if (bowlerStatsMap == null) {
            bowlerStatsMap = new HashMap<>();
        }
        return bowlerStatsMap.get(playerId);
    }
    
    public void updateBowlerStats(String playerId, BowlerStats stats) {
        if (bowlerStatsMap == null) {
            bowlerStatsMap = new HashMap<>();
        }
        bowlerStatsMap.put(playerId, stats);
    }
    
    // Helper methods for batting order queue
    public void addToBattingOrder(String playerId) {
        if (battingOrderQueue == null) {
            battingOrderQueue = new ArrayList<>();
        }
        if (!battingOrderQueue.contains(playerId)) {
            battingOrderQueue.add(playerId);
            android.util.Log.d("CricketMatch", "Added player " + playerId + " to batting order. Queue size: " + battingOrderQueue.size());
        }
    }
    
    public String getNextBatsmanFromQueue() {
        if (battingOrderQueue == null || battingOrderQueue.isEmpty()) {
            android.util.Log.d("CricketMatch", "Batting order queue is empty");
            return null;
        }
        String nextBatsman = battingOrderQueue.remove(0);
        android.util.Log.d("CricketMatch", "Retrieved next batsman from queue: " + nextBatsman + ". Remaining in queue: " + battingOrderQueue.size());
        return nextBatsman;
    }
    
    public boolean hasBatsmanInQueue() {
        return battingOrderQueue != null && !battingOrderQueue.isEmpty();
    }
    
    public void clearBattingOrderQueue() {
        if (battingOrderQueue != null) {
            battingOrderQueue.clear();
        }
    }
    
    // Helper methods for bowling order queue
    public void addToBowlingOrder(String playerId) {
        if (bowlingOrderQueue == null) {
            bowlingOrderQueue = new ArrayList<>();
        }
        if (!bowlingOrderQueue.contains(playerId)) {
            bowlingOrderQueue.add(playerId);
            android.util.Log.d("CricketMatch", "Added player " + playerId + " to bowling order. Queue size: " + bowlingOrderQueue.size());
        }
    }
    
    public String getNextBowlerFromQueue() {
        if (bowlingOrderQueue == null || bowlingOrderQueue.isEmpty()) {
            android.util.Log.d("CricketMatch", "Bowling order queue is empty");
            return null;
        }
        String nextBowler = bowlingOrderQueue.remove(0);
        android.util.Log.d("CricketMatch", "Retrieved next bowler from queue: " + nextBowler + ". Remaining in queue: " + bowlingOrderQueue.size());
        return nextBowler;
    }
    
    public boolean hasBowlerInQueue() {
        return bowlingOrderQueue != null && !bowlingOrderQueue.isEmpty();
    }
    
    public void clearBowlingOrderQueue() {
        if (bowlingOrderQueue != null) {
            bowlingOrderQueue.clear();
        }
    }

    // --- COMMAND SUPPORT (Existing for Over management) ---

    public void startNewOver() {
        Innings curr = getCurrentInnings();
        if (curr != null && !curr.isCompleted()) createNewOver(curr);
    }

    public void removeLastOver() {
        if (!currentOvers.isEmpty()) {
            Over last = currentOvers.remove(currentOvers.size() - 1);
            Innings curr = getCurrentInnings();
            if (curr != null && last.isCompleted()) curr.setOversCompleted(curr.getOversCompleted() - 1);
        }
    }

    public void setCurrentOver(Over over) {
        if (!currentOvers.isEmpty()) currentOvers.set(currentOvers.size() - 1, over);
        else currentOvers.add(over);
    }

    public void endCurrentOver() {
        Innings curr = getCurrentInnings();
        Over over = getCurrentOver();
        if (curr != null && over != null) endOver(curr, over);
    }

    public boolean canEndInnings() {
        Innings curr = getCurrentInnings();
        return curr != null && !curr.isCompleted() && getMatchStatus().equals(MatchStatus.LIVE.name());
    }

    public void endCurrentInnings() {
        Innings curr = getCurrentInnings();
        if (curr != null && !curr.isCompleted()) endInnings(curr);
    }

    public String getMatchSummary() { return "Summary Logic Placeholder"; }

    // --- ABSTRACT METHOD IMPLEMENTATIONS (Required by Match.java) ---

    /**
     * Calculates the required run rate for the chasing team.
     * Only applicable in the second innings when a target is set.
     * @return Required run rate, or 0.0f if not in second innings or no target
     */
    @Override
    public float getRequiredRunRate() {
        if (currentInningsNumber != 2 || targetScore == 0) {
            return 0.0f;
        }

        Innings currentInnings = getCurrentInnings();
        if (currentInnings == null) {
            return 0.0f;
        }

        CricketMatchConfig config = (CricketMatchConfig) this.matchConfig;
        if (config == null) {
            return 0.0f;
        }

        int runsRequired = targetScore - currentInnings.getTotalRuns();
        if (runsRequired <= 0) {
            return 0.0f;
        }

        int remainingBalls = getRemainingBalls();
        if (remainingBalls <= 0) {
            return 0.0f;
        }

        float remainingOvers = remainingBalls / 6.0f;
        return runsRequired / remainingOvers;
    }

    /**
     * Calculates the number of balls remaining in the current innings.
     * Takes into account completed overs and balls in the current over.
     * @return Number of balls remaining
     */
    @Override
    public int getRemainingBalls() {
        Innings currentInnings = getCurrentInnings();
        if (currentInnings == null) {
            return 0;
        }

        CricketMatchConfig config = (CricketMatchConfig) this.matchConfig;
        if (config == null) {
            return 0;
        }

        int totalBalls = config.getNumberOfOvers() * 6;
        int ballsBowled = currentInnings.getOversCompleted() * 6;

        // Add legal balls in current incomplete over
        Over currentOver = getCurrentOver();
        if (currentOver != null && !currentOver.isCompleted()) {
            ballsBowled += countLegalBalls(currentOver);
        }

        return Math.max(0, totalBalls - ballsBowled);
    }

    /**
     * Calculates the total number of extras conceded in the current innings.
     * Includes wides, no-balls, byes, and leg-byes.
     * @return Total extras count
     */
    @Override
    public int getExtrasCount() {
        Innings currentInnings = getCurrentInnings();
        if (currentInnings == null) {
            return 0;
        }

        int extrasCount = 0;

        // Count extras from all cricket events in current innings
        for (CricketEvent event : cricketEvents) {
            // Only count events from current innings
            if (event.getOverNumber() > 0 && 
                event.getOverNumber() <= currentInnings.getOversCompleted() + 1) {
                
                String extrasType = event.getExtrasType();
                if (extrasType != null && !extrasType.equals("NONE")) {
                    extrasCount += event.getTotalRuns();
                }
            }
        }

        return extrasCount;
    }


    public static class Builder extends Match.Builder<Builder> {
        private CricketMatchConfig config;
        private List<MatchTeam> teams = new ArrayList<>();
        public Builder(String name, String hostUserId) { super(name, hostUserId); }
        public Builder withConfig(CricketMatchConfig config) { this.config = config; return this; }
        public Builder addTeam(MatchTeam team) { this.teams.add(team); return this; }
        @Override protected Builder self() { return this; }
        @Override public CricketMatch build() {
            CricketMatch m = new CricketMatch();
            m.setEntityId(UUID.randomUUID().toString());
            m.setName(this.name);
            m.setHostUserId(this.hostUserId);
            m.setSportId(SportTypeEnum.CRICKET.name());
            m.setMatchStatus(MatchStatus.SCHEDULED.name());
            m.setMatchConfig(this.config != null ? this.config : new CricketMatchConfig());
            m.setTeams(this.teams);
            return m;
        }
    }
}