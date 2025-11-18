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
 */
public class CricketMatch extends Match {

    // Cricket-specific relational data
    private List<Innings> innings;
    private List<CricketEvent> cricketEvents; // For AddWicketCommand, AddExtrasCommand [cite: 99, 100]
    private List<MatchTeam> teams;

    // State tracking
    private int currentInningsNumber;
    private int targetScore;
    private List<Over> currentOvers;

    // Observer Pattern
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
        if (currentInnings == null) return;

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

        if (cricketEvent.isWicket()) {
            currentInnings.setWicketsFallen(currentInnings.getWicketsFallen() + 1);
            currentOver.setWicketsInOver(currentOver.getWicketsInOver() + 1);
        }

        if (cricketEvent.isLegalDelivery()) {
            int legalBallsInOver = countLegalBalls(currentOver);
            if (legalBallsInOver >= 6) {
                endOver(currentInnings, currentOver);
            }
        }

        // Check completion
        CricketMatchConfig config = (CricketMatchConfig) this.matchConfig;
        boolean inningsComplete = false;

        if (currentInnings.getWicketsFallen() >= 10) inningsComplete = true;
        if (currentInnings.getOversCompleted() >= config.getNumberOfOvers()) inningsComplete = true;

        if (currentInningsNumber == 2 && targetScore > 0) {
            if (currentInnings.getTotalRuns() >= targetScore) inningsComplete = true;
            else if (currentInnings.getWicketsFallen() >= 10) inningsComplete = true;
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
        if (currentInningsNumber == 1) {
            this.targetScore = innings.getTotalRuns() + 1;
            if (this.innings.size() > 1) {
                currentInningsNumber = 2;
                this.currentOvers.clear();
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
    private void notifyObservers() { for (MatchObserver o : observers) o.onMatchUpdated(this); }
    private void notifyEventAdded(MatchEvent e) { for (MatchObserver o : observers) o.onEventAdded(e); }
    private void notifyStatusChanged(String s) { for (MatchObserver o : observers) o.onMatchStatusChanged(s); }

    // --- HELPERS (getCurrentInnings, createBallFromEvent, etc.) ---

    public Innings getCurrentInnings() {
        if (currentInningsNumber == 0 || innings.isEmpty()) return null;
        return innings.get(currentInningsNumber - 1);
    }

    public Over getCurrentOver() {
        if (currentOvers.isEmpty()) return null;
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

    // --- ABSTRACT IMPL (startMatch, endMatch, addEvent, canStartMatch) ---

    @Override
    public void startMatch() {
        setMatchStatus(MatchStatus.LIVE.name());

        if (innings.isEmpty() && teams.size() >= 2) {
            Innings first = new Innings();
            first.setInningsId(UUID.randomUUID().toString());
            first.setMatchId(this.entityId);
            first.setInningsNumber(1);
            first.setBattingTeamId(teams.get(0).getTeamId());
            first.setBowlingTeamId(teams.get(1).getTeamId());
            innings.add(first);
            currentInningsNumber = 1;

            Innings second = new Innings();
            second.setInningsId(UUID.randomUUID().toString());
            second.setMatchId(this.entityId);
            second.setInningsNumber(2);
            second.setBattingTeamId(teams.get(1).getTeamId());
            second.setBowlingTeamId(teams.get(0).getTeamId());
            innings.add(second);
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