package com.example.tournafy.ui.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.tournafy.command.MatchCommandManager;
import com.example.tournafy.data.repository.online.BallFirebaseRepository;
import com.example.tournafy.data.repository.online.FootballEventFirebaseRepository;
import com.example.tournafy.data.repository.online.InningsFirebaseRepository;
import com.example.tournafy.data.repository.online.MatchFirebaseRepository;
import com.example.tournafy.data.repository.offline.BallFirestoreRepository;
import com.example.tournafy.data.repository.offline.FootballEventFirestoreRepository;
import com.example.tournafy.data.repository.offline.InningsFirestoreRepository;
import com.example.tournafy.data.repository.offline.MatchFirestoreRepository;
import com.example.tournafy.di.RepositoryQualifiers.OfflineRepo;
import com.example.tournafy.di.RepositoryQualifiers.OnlineRepo;
import com.example.tournafy.domain.models.base.Match;
import com.example.tournafy.domain.models.match.cricket.Ball;
import com.example.tournafy.domain.models.match.cricket.CricketEvent;
import com.example.tournafy.domain.models.match.cricket.CricketMatch;
import com.example.tournafy.domain.models.match.cricket.CricketWicketDetail;
import com.example.tournafy.domain.models.match.cricket.Innings;
import com.example.tournafy.domain.models.match.cricket.Over;
import com.example.tournafy.domain.models.match.football.FootballEvent;
import com.example.tournafy.service.interfaces.IEventService;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MatchViewModel extends ViewModel {

    private final MatchFirestoreRepository offlineMatchRepo;
    private final InningsFirestoreRepository offlineInningsRepo;
    private final BallFirestoreRepository offlineBallRepo;
    private final com.example.tournafy.data.repository.offline.OverFirestoreRepository offlineOverRepo;
    private final FootballEventFirestoreRepository offlineFootballEventRepo;

    private final MatchFirebaseRepository onlineMatchRepo;
    private final InningsFirebaseRepository onlineInningsRepo;
    private final BallFirebaseRepository onlineBallRepo;
    private final FootballEventFirebaseRepository onlineFootballEventRepo;

    private final IEventService eventService;
    private final MatchCommandManager commandManager;

    private final MutableLiveData<String> _offlineMatchId = new MutableLiveData<>();
    private final MutableLiveData<String> _onlineMatchId = new MutableLiveData<>();
    private final MutableLiveData<String> _onlineOverId = new MutableLiveData<>();

    public final LiveData<Match> offlineMatch;
    public final LiveData<List<Innings>> offlineInningsList;

    public final LiveData<Match> onlineMatch;
    public final LiveData<List<FootballEvent>> onlineFootballEvents;
    public final LiveData<List<Ball>> onlineBallStream;
    public final LiveData<List<Innings>> onlineInningsList;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public final LiveData<String> errorMessage = _errorMessage;
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoading = _isLoading;

    @Inject
    public MatchViewModel(
            @OfflineRepo MatchFirestoreRepository offlineMatchRepo,
            @OfflineRepo InningsFirestoreRepository offlineInningsRepo,
            @OfflineRepo BallFirestoreRepository offlineBallRepo,
            @OfflineRepo com.example.tournafy.data.repository.offline.OverFirestoreRepository offlineOverRepo,
            @OfflineRepo FootballEventFirestoreRepository offlineFootballEventRepo,
            @OnlineRepo MatchFirebaseRepository onlineMatchRepo,
            @OnlineRepo InningsFirebaseRepository onlineInningsRepo,
            @OnlineRepo BallFirebaseRepository onlineBallRepo,
            @OnlineRepo FootballEventFirebaseRepository onlineFootballEventRepo,
            IEventService eventService,
            MatchCommandManager commandManager
    ) {
        this.offlineMatchRepo = offlineMatchRepo;
        this.offlineInningsRepo = offlineInningsRepo;
        this.offlineBallRepo = offlineBallRepo;
        this.offlineOverRepo = offlineOverRepo;
        this.offlineFootballEventRepo = offlineFootballEventRepo;

        this.onlineMatchRepo = onlineMatchRepo;
        this.onlineInningsRepo = onlineInningsRepo;
        this.onlineBallRepo = onlineBallRepo;
        this.onlineFootballEventRepo = onlineFootballEventRepo;

        this.eventService = eventService;
        this.commandManager = commandManager;

        this.offlineMatch = Transformations.switchMap(_offlineMatchId,
                matchId -> offlineMatchRepo.getById(matchId)
        );
        this.offlineInningsList = Transformations.switchMap(_offlineMatchId,
                matchId -> offlineInningsRepo.getInningsByMatchId(matchId)
        );

        this.onlineMatch = Transformations.switchMap(_onlineMatchId,
                matchId -> onlineMatchRepo.getById(matchId)
        );
        this.onlineFootballEvents = Transformations.switchMap(_onlineMatchId,
                matchId -> onlineFootballEventRepo.getEventsByMatchId(matchId)
        );
        this.onlineBallStream = Transformations.switchMap(_onlineOverId,
                overId -> onlineBallRepo.getBallsByOverId(overId)
        );
        this.onlineInningsList = Transformations.switchMap(_onlineMatchId,
                matchId -> onlineInningsRepo.getInningsByMatchId(matchId)
        );
    }

    /**
     * CRITICAL FIX: Registers FirebaseMatchObserver to enable automatic sync from Firestore (offline) to Firebase (online).
     * This method should be called whenever a match is loaded, created, or started.
     * 
     * DISABLED FOR OFFLINE-FIRST: Online sync disabled to improve offline performance.
     * Re-enable when implementing explicit sync feature.
     * 
     * @param match The CricketMatch to register the observer for
     */
    private void registerOnlineSync(CricketMatch match) {
        // DISABLED: Online sync causes performance issues for offline-first app
        // TODO: Re-enable with network check and user preference
        /*
        if (match != null && onlineMatchRepo != null) {
            // Create observer with Firebase repository and match ID
            com.example.tournafy.service.observers.FirebaseMatchObserver observer = 
                new com.example.tournafy.service.observers.FirebaseMatchObserver(
                    onlineMatchRepo, 
                    match.getEntityId()
                );
            
            // Register observer to receive notifications
            match.addObserver(observer);
            
            // Log for debugging
            android.util.Log.d("MatchViewModel", "Registered FirebaseMatchObserver for match: " + match.getEntityId());
        }
        */
        android.util.Log.d("MatchViewModel", "Online sync DISABLED for offline-first experience");
    }

    public void loadOfflineMatch(String matchId) {
        _offlineMatchId.setValue(matchId);
        
        // CRITICAL FIX: Register online sync observer when match is loaded
        offlineMatch.observeForever(match -> {
            if (match instanceof CricketMatch) {
                registerOnlineSync((CricketMatch) match);
            }
        });
    }

    public void loadOnlineMatch(String matchId) {
        _onlineMatchId.setValue(matchId);
    }

    public void loadOnlineBallStream(String overId) {
        _onlineOverId.setValue(overId);
    }

    public Match getOfflineMatch() {
        return offlineMatch.getValue();
    }

    // --- Cricket Helper Methods ---

    public String getTeamAName() {
        Match match = offlineMatch.getValue();
        if (match instanceof CricketMatch) {
            CricketMatch cm = (CricketMatch) match;
            Innings currentInnings = cm.getCurrentInnings();
            if (currentInnings != null && cm.getTeams() != null) {
                // Return the batting team name for current innings
                String battingTeamId = currentInnings.getBattingTeamId();
                for (com.example.tournafy.domain.models.team.MatchTeam team : cm.getTeams()) {
                    if (battingTeamId.equals(team.getTeamId())) {
                        return team.getTeamName();
                    }
                }
            }
            // Fallback to first team if no innings yet
            if (cm.getTeams() != null && !cm.getTeams().isEmpty()) {
                return cm.getTeams().get(0).getTeamName();
            }
        }
        return "Team A";
    }

    public String getTeamBName() {
        Match match = offlineMatch.getValue();
        if (match instanceof CricketMatch) {
            CricketMatch cm = (CricketMatch) match;
            Innings currentInnings = cm.getCurrentInnings();
            if (currentInnings != null && cm.getTeams() != null) {
                // Return the bowling team name for current innings
                String bowlingTeamId = currentInnings.getBowlingTeamId();
                for (com.example.tournafy.domain.models.team.MatchTeam team : cm.getTeams()) {
                    if (bowlingTeamId.equals(team.getTeamId())) {
                        return team.getTeamName();
                    }
                }
            }
            // Fallback to second team if no innings yet
            if (cm.getTeams() != null && cm.getTeams().size() > 1) {
                return cm.getTeams().get(1).getTeamName();
            }
        }
        return "Team B";
    }

    public float getCurrentRunRate() {
        Match match = offlineMatch.getValue();
        if (match instanceof CricketMatch) {
            CricketMatch cm = (CricketMatch) match;
            Innings innings = cm.getCurrentInnings();
            if (innings != null) {
                // CRR = Runs / Overs
                // Standard CRR uses balls: (Runs / Balls) * 6
                int totalBalls = (innings.getOversCompleted() * 6); 
                // Add current over balls
                Over currentOver = cm.getCurrentOver();
                if (currentOver != null && currentOver.getBalls() != null) {
                    // Count only legal deliveries
                    for (Ball ball : currentOver.getBalls()) {
                        if (ball.isLegalDelivery()) {
                            totalBalls++;
                        }
                    }
                }
                
                if (totalBalls > 0) {
                    return (float) innings.getTotalRuns() / totalBalls * 6;
                }
            }
        }
        return 0.0f;
    }

    public String getStrikerId() {
        Match match = offlineMatch.getValue();
        if (match instanceof CricketMatch) {
            CricketMatch cm = (CricketMatch) match;
            String strikerId = cm.getCurrentStrikerId();
            if (strikerId != null) {
                // Try to resolve player name from teams
                String playerName = getPlayerName(cm, strikerId);
                return playerName != null ? playerName : strikerId;
            }
        }
        return "Striker";
    }

    public String getNonStrikerId() {
        Match match = offlineMatch.getValue();
        if (match instanceof CricketMatch) {
            CricketMatch cm = (CricketMatch) match;
            String nonStrikerId = cm.getCurrentNonStrikerId();
            if (nonStrikerId != null) {
                // Try to resolve player name from teams
                String playerName = getPlayerName(cm, nonStrikerId);
                return playerName != null ? playerName : nonStrikerId;
            }
        }
        return "Non-Striker";
    }
    
    public String getCurrentBowlerId() {
        Match match = offlineMatch.getValue();
        if (match instanceof CricketMatch) {
            CricketMatch cm = (CricketMatch) match;
            String bowlerId = cm.getCurrentBowlerId();
            if (bowlerId != null) {
                // Try to resolve player name from teams
                String playerName = getPlayerName(cm, bowlerId);
                return playerName != null ? playerName : bowlerId;
            }
        }
        return "Bowler";
    }
    
    /**
     * Helper method to get player name from player ID by searching all teams.
     */
    private String getPlayerName(CricketMatch match, String playerId) {
        if (match.getTeams() != null) {
            for (com.example.tournafy.domain.models.team.MatchTeam team : match.getTeams()) {
                if (team.getPlayers() != null) {
                    for (com.example.tournafy.domain.models.team.Player player : team.getPlayers()) {
                        if (playerId.equals(player.getPlayerId())) {
                            return player.getPlayerName();
                        }
                    }
                }
            }
        }
        return null;
    }

    public Over getCurrentOver() {
        Match match = offlineMatch.getValue();
        if (match instanceof CricketMatch) {
            return ((CricketMatch) match).getCurrentOver();
        }
        return null;
    }

    // --- Cricket Action Methods ---

    /**
     * Adds a cricket ball (regular delivery) to the current match.
     * CRITICAL: Creates both CricketEvent AND Ball entity, populates all FKs, and persists to DB.
     * 
     * @param runs The number of runs scored on this ball (0-6)
     */
    public void addCricketBall(int runs) {
        _isLoading.setValue(true);
        
        // Get current match from LiveData
        Match currentMatch = offlineMatch.getValue();
        if (!(currentMatch instanceof CricketMatch)) {
            _errorMessage.setValue("Not a cricket match");
            _isLoading.setValue(false);
            return;
        }
        
        CricketMatch cricketMatch = (CricketMatch) currentMatch;
        
        // Get current innings (processEvent will create over if needed)
        Innings currentInnings = cricketMatch.getCurrentInnings();
        Over currentOver = cricketMatch.getCurrentOver(); // May be null, processEvent handles it
        
        if (currentInnings == null) {
            _errorMessage.setValue("Match not started: No current innings");
            _isLoading.setValue(false);
            return;
        }
        
        // --- STEP 1: Create CricketEvent with ALL Foreign Keys ---
        CricketEvent event = new CricketEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setMatchId(cricketMatch.getEntityId());
        event.setTeamId(currentInnings.getBattingTeamId());
        event.setEventType("BALL");
        // processEvent will fill these if currentOver is null:
        event.setOverNumber(currentOver != null ? currentOver.getOverNumber() : 0);
        event.setBallNumber(currentOver != null && currentOver.getBalls() != null ? currentOver.getBalls().size() + 1 : 0);
        event.setTotalRuns(runs);
        event.setRunsScoredBat(runs);
        event.setRunsScoredExtras(0);
        event.setLegalDelivery(true);
        event.setWicket(false);
        event.setExtrasType("NONE");
        event.setBoundary(runs == 4 || runs == 6);
        event.setBoundaryType(runs == 4 ? 4 : (runs == 6 ? 6 : 0));
        event.setEventTime(new java.util.Date());
        // Set player IDs
        event.setBatsmanStrikerId(cricketMatch.getCurrentStrikerId());
        event.setBatsmanNonStrikerId(cricketMatch.getCurrentNonStrikerId());
        event.setBowlerId(cricketMatch.getCurrentBowlerId());
        
        // --- STEP 2: Process event to update in-memory state (score) ---
        cricketMatch.processEvent(event);
        
        // --- Get current over AFTER processEvent (it may have created it) ---
        Over finalCurrentOver = cricketMatch.getCurrentOver();
        if (finalCurrentOver == null) {
            _errorMessage.setValue("Failed to create over");
            _isLoading.setValue(false);
            return;
        }
        
        // DEBUG: Log balls count before saving
        android.util.Log.d("MatchViewModel", "Before save - Balls in currentOver: " + 
            (finalCurrentOver.getBalls() != null ? finalCurrentOver.getBalls().size() : 0));
        android.util.Log.d("MatchViewModel", "Before save - Total runs: " + currentInnings.getTotalRuns());
        
        // --- SIMPLIFIED PERSISTENCE: Only update Match document ---
        // All nested data (innings, overs, balls) is stored within the match document
        // No need to persist Ball, Over, Innings separately
        
        offlineMatchRepo.update(cricketMatch).addOnCompleteListener(matchTask -> {
            if (matchTask.isSuccessful()) {
                // Trigger LiveData update to refresh UI by re-fetching from database
                // Add small delay to ensure Firestore write is fully committed
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    String currentId = _offlineMatchId.getValue();
                    if (currentId != null) {
                        _offlineMatchId.setValue(currentId);
                    }
                    _isLoading.setValue(false);
                }, 100); // 100ms delay
            } else {
                _errorMessage.setValue("Failed to save match update");
                _isLoading.setValue(false);
            }
        });
    }

    /**
     * Adds a cricket wicket to the current match.
     * CRITICAL: Creates both CricketEvent AND Ball entity with wicket data.
     * 
     * @param wicketType The type of wicket (e.g., "BOWLED", "CAUGHT", "LBW")
     */
    public void addCricketWicket(String wicketType) {
        _isLoading.setValue(true);
        
        Match currentMatch = offlineMatch.getValue();
        if (!(currentMatch instanceof CricketMatch)) {
            _errorMessage.setValue("Not a cricket match");
            _isLoading.setValue(false);
            return;
        }
        
        CricketMatch cricketMatch = (CricketMatch) currentMatch;
        
        Innings currentInnings = cricketMatch.getCurrentInnings();
        Over currentOver = cricketMatch.getCurrentOver(); // May be null, processEvent handles it
        
        if (currentInnings == null) {
            _errorMessage.setValue("Match not started: No current innings");
            _isLoading.setValue(false);
            return;
        }
        
        // --- Create CricketEvent with ALL FKs ---
        CricketEvent event = new CricketEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setMatchId(cricketMatch.getEntityId());
        event.setTeamId(currentInnings.getBattingTeamId());
        event.setEventType("WICKET");
        // processEvent will fill these if currentOver is null:
        event.setOverNumber(currentOver != null ? currentOver.getOverNumber() : 0);
        event.setBallNumber(currentOver != null && currentOver.getBalls() != null ? currentOver.getBalls().size() + 1 : 0);
        event.setTotalRuns(0); // Wickets usually score 0 runs unless run out
        event.setRunsScoredBat(0);
        event.setRunsScoredExtras(0);
        event.setLegalDelivery(true);
        event.setWicket(true);
        event.setExtrasType("NONE");
        event.setBoundary(false);
        event.setBoundaryType(0);
        event.setEventTime(new java.util.Date());
        // Set player IDs
        event.setBatsmanStrikerId(cricketMatch.getCurrentStrikerId());
        event.setBatsmanNonStrikerId(cricketMatch.getCurrentNonStrikerId());
        event.setBowlerId(cricketMatch.getCurrentBowlerId());
        
        // Create wicket detail
        CricketWicketDetail wicketDetail = new CricketWicketDetail();
        wicketDetail.setWicketType(wicketType);
        event.setWicketDetail(wicketDetail);
        
        // --- Process event ---
        cricketMatch.processEvent(event);
        
        // --- Get current over AFTER processEvent ---
        Over finalCurrentOver = cricketMatch.getCurrentOver();
        if (finalCurrentOver == null) {
            _errorMessage.setValue("Failed to create over");
            _isLoading.setValue(false);
            return;
        }
        
        // --- SIMPLIFIED PERSISTENCE: Only update Match document ---
        // All nested data (innings, overs, balls) is stored within the match document
        
        offlineMatchRepo.update(cricketMatch).addOnCompleteListener(matchTask -> {
            if (matchTask.isSuccessful()) {
                // Trigger LiveData update to refresh UI by re-fetching from database
                // Add small delay to ensure Firestore write is fully committed
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    String currentId = _offlineMatchId.getValue();
                    if (currentId != null) {
                        _offlineMatchId.setValue(currentId);
                    }
                    _isLoading.setValue(false);
                }, 100); // 100ms delay
            } else {
                _errorMessage.setValue("Failed to save match update");
                _isLoading.setValue(false);
            }
        });
    }

    /**
     * Adds a cricket extra (wide, no-ball, bye, leg-bye) to the current match.
     * CRITICAL: Creates both CricketEvent AND Ball entity with extras data.
     * 
     * @param extrasType The type of extra ("WIDE", "NO_BALL", "BYE", "LEG_BYE")
     */
    public void addCricketExtra(String extrasType) {
        _isLoading.setValue(true);
        
        Match currentMatch = offlineMatch.getValue();
        if (!(currentMatch instanceof CricketMatch)) {
            _errorMessage.setValue("Not a cricket match");
            _isLoading.setValue(false);
            return;
        }
        
        CricketMatch cricketMatch = (CricketMatch) currentMatch;
        
        Innings currentInnings = cricketMatch.getCurrentInnings();
        Over currentOver = cricketMatch.getCurrentOver(); // May be null, processEvent handles it
        
        if (currentInnings == null) {
            _errorMessage.setValue("Match not started: No current innings");
            _isLoading.setValue(false);
            return;
        }
        
        // Determine runs (Wide and No-Ball typically add 1 run automatically)
        int extrasRuns = (extrasType.equals("WIDE") || extrasType.equals("NO_BALL")) ? 1 : 0;
        boolean isLegal = extrasType.equals("BYE") || extrasType.equals("LEG_BYE");
        
        // --- Create CricketEvent ---
        CricketEvent event = new CricketEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setMatchId(cricketMatch.getEntityId());
        event.setTeamId(currentInnings.getBattingTeamId());
        event.setEventType("EXTRA");
        // processEvent will fill these if currentOver is null:
        event.setOverNumber(currentOver != null ? currentOver.getOverNumber() : 0);
        event.setBallNumber(currentOver != null && currentOver.getBalls() != null ? currentOver.getBalls().size() + 1 : 0);
        event.setTotalRuns(extrasRuns);
        event.setRunsScoredBat(0);
        event.setRunsScoredExtras(extrasRuns);
        event.setLegalDelivery(isLegal);
        event.setWicket(false);
        event.setExtrasType(extrasType);
        event.setBoundary(false);
        event.setBoundaryType(0);
        event.setEventTime(new java.util.Date());
        // Set player IDs
        event.setBatsmanStrikerId(cricketMatch.getCurrentStrikerId());
        event.setBatsmanNonStrikerId(cricketMatch.getCurrentNonStrikerId());
        event.setBowlerId(cricketMatch.getCurrentBowlerId());
        
        // --- Process event ---
        cricketMatch.processEvent(event);
        
        // --- Get current over AFTER processEvent ---
        Over finalCurrentOver = cricketMatch.getCurrentOver();
        if (finalCurrentOver == null) {
            _errorMessage.setValue("Failed to create over");
            _isLoading.setValue(false);
            return;
        }
        
        // --- Create Ball entity ---
        Ball ball = new Ball();
        ball.setBallId(java.util.UUID.randomUUID().toString());
        ball.setMatchId(cricketMatch.getEntityId());
        ball.setInningsId(currentInnings.getInningsId());
        ball.setOverId(finalCurrentOver.getOverId());
        ball.setInningsNumber(currentInnings.getInningsNumber());
        ball.setOverNumber(finalCurrentOver.getOverNumber());
        ball.setBallNumber(event.getBallNumber());
        ball.setRunsScored(extrasRuns);
        ball.setWicket(false);
        ball.setBoundary(false);
        ball.setExtrasType(extrasType);
        // Set player IDs from match state
        ball.setBatsmanId(cricketMatch.getCurrentStrikerId());
        ball.setBowlerId(cricketMatch.getCurrentBowlerId());
        
        // --- SIMPLIFIED PERSISTENCE: Only update Match document ---
        // All nested data (innings, overs, balls) is stored within the match document
        
        offlineMatchRepo.update(cricketMatch).addOnCompleteListener(matchTask -> {
            if (matchTask.isSuccessful()) {
                // Trigger LiveData update to refresh UI by re-fetching from database
                // Add small delay to ensure Firestore write is fully committed
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    String currentId = _offlineMatchId.getValue();
                    if (currentId != null) {
                        _offlineMatchId.setValue(currentId);
                    }
                    _isLoading.setValue(false);
                }, 100); // 100ms delay
            } else {
                _errorMessage.setValue("Failed to save match update");
                _isLoading.setValue(false);
            }
        });
    }

    /**
     * Manually ends the current over.
     * Called when the over is complete (6 legal balls) or host decides to end it.
     */
    public void endCurrentOver() {
        Match currentMatch = offlineMatch.getValue();
        if (!(currentMatch instanceof CricketMatch)) {
            _errorMessage.setValue("Not a cricket match");
            return;
        }
        
        CricketMatch cricketMatch = (CricketMatch) currentMatch;
        cricketMatch.endCurrentOver();
        
        // Persist the updated match
        offlineMatchRepo.update(cricketMatch).addOnCompleteListener(task -> {
            // DISABLED: Online sync disabled for offline-first performance
            // if (task.isSuccessful()) {
            //     cricketMatch.notifyObservers();
            // }
            if (!task.isSuccessful()) {
                _errorMessage.setValue("Failed to end over");
            }
        });
    }

    /**
     * Starts the match - initializes innings and sets status to LIVE.
     * CRITICAL FIX: Now registers FirebaseMatchObserver and triggers online sync after offline persistence.
     */
    public void startMatch() {
        _isLoading.setValue(true);
        
        Match currentMatch = offlineMatch.getValue();
        if (currentMatch == null) {
            _errorMessage.setValue("No match loaded");
            _isLoading.setValue(false);
            return;
        }
        
        if (currentMatch instanceof CricketMatch) {
            CricketMatch cricketMatch = (CricketMatch) currentMatch;
            
            // Enhanced validation with specific error messages
            if (cricketMatch.getTeams() == null || cricketMatch.getTeams().size() < 2) {
                _errorMessage.setValue("Cannot start match: Need at least 2 teams");
                _isLoading.setValue(false);
                return;
            }
            
            if (cricketMatch.getMatchConfig() == null) {
                _errorMessage.setValue("Cannot start match: Match configuration is missing");
                _isLoading.setValue(false);
                return;
            }
            
            if (!cricketMatch.getMatchStatus().equals(com.example.tournafy.domain.enums.MatchStatus.SCHEDULED.name())) {
                _errorMessage.setValue("Cannot start match: Match status is " + cricketMatch.getMatchStatus() + " (must be SCHEDULED)");
                _isLoading.setValue(false);
                return;
            }
            
            // CRITICAL FIX: Register observer BEFORE starting match
            registerOnlineSync(cricketMatch);
            
            try {
                cricketMatch.startMatch();
            } catch (IllegalStateException e) {
                _errorMessage.setValue(e.getMessage());
                _isLoading.setValue(false);
                return;
            }
            
            // Persist match and create first innings in DB
            offlineMatchRepo.update(cricketMatch).addOnCompleteListener(matchTask -> {
                if (matchTask.isSuccessful() && !cricketMatch.getInnings().isEmpty()) {
                    // Save innings to database
                    Innings firstInnings = cricketMatch.getInnings().get(0);
                    offlineInningsRepo.add(firstInnings).addOnCompleteListener(inningsTask -> {
                        if (inningsTask.isSuccessful()) {
                            // Create and save the first over
                            Over firstOver = cricketMatch.getCurrentOver();
                            if (firstOver != null) {
                                offlineOverRepo.add(firstOver).addOnCompleteListener(overTask -> {
                                    // DISABLED: Online sync disabled for offline-first performance
                                    // if (overTask.isSuccessful()) {
                                    //     cricketMatch.notifyStatusChanged(com.example.tournafy.domain.enums.MatchStatus.LIVE.name());
                                    // }
                                    _isLoading.setValue(false);
                                    if (!overTask.isSuccessful()) {
                                        _errorMessage.setValue("Failed to create first over");
                                    }
                                });
                            } else {
                                _isLoading.setValue(false);
                            }
                        } else {
                            _isLoading.setValue(false);
                            _errorMessage.setValue("Failed to save innings");
                        }
                    });
                } else {
                    _isLoading.setValue(false);
                    _errorMessage.setValue("Failed to start match");
                }
            });
        } else if (currentMatch instanceof com.example.tournafy.domain.models.match.football.FootballMatch) {
            // Football match
            com.example.tournafy.domain.models.match.football.FootballMatch footballMatch = 
                (com.example.tournafy.domain.models.match.football.FootballMatch) currentMatch;
            
            // Handle DRAFT status - convert to SCHEDULED
            String currentStatus = footballMatch.getMatchStatus();
            if (currentStatus == null || currentStatus.equals("DRAFT")) {
                footballMatch.setMatchStatus(com.example.tournafy.domain.enums.MatchStatus.SCHEDULED.name());
            }
            
            // Validate match can start
            if (footballMatch.getTeams() == null || footballMatch.getTeams().size() < 2) {
                _errorMessage.setValue("Cannot start match: Need at least 2 teams");
                _isLoading.setValue(false);
                return;
            }
            
            if (footballMatch.getMatchConfig() == null) {
                _errorMessage.setValue("Cannot start match: Match configuration is missing");
                _isLoading.setValue(false);
                return;
            }
            
            if (!footballMatch.getMatchStatus().equals(com.example.tournafy.domain.enums.MatchStatus.SCHEDULED.name())) {
                _errorMessage.setValue("Cannot start match: Match status is " + footballMatch.getMatchStatus() + " (must be SCHEDULED)");
                _isLoading.setValue(false);
                return;
            }
            
            // NOTE: Online sync is disabled for offline-first performance
            // Football doesn't need online sync registration like cricket's complex innings/over structure
            
            // Start the match (sets status to LIVE)
            try {
                footballMatch.startMatch();
            } catch (IllegalStateException e) {
                _errorMessage.setValue(e.getMessage());
                _isLoading.setValue(false);
                return;
            }
            
            // Persist match to Firestore
            offlineMatchRepo.update(footballMatch).addOnCompleteListener(task -> {
                _isLoading.setValue(false);
                if (!task.isSuccessful()) {
                    _errorMessage.setValue("Failed to start match");
                }
            });
        } else {
            // Other sport types
            currentMatch.startMatch();
            offlineMatchRepo.update(currentMatch).addOnCompleteListener(task -> {
                _isLoading.setValue(false);
                if (!task.isSuccessful()) {
                    _errorMessage.setValue("Failed to start match");
                }
            });
        }
    }

    /**
     * Ends the current innings manually.
     */
    public void endCurrentInnings() {
        Match currentMatch = offlineMatch.getValue();
        if (!(currentMatch instanceof CricketMatch)) {
            _errorMessage.setValue("Not a cricket match");
            return;
        }
        
        CricketMatch cricketMatch = (CricketMatch) currentMatch;
        
        if (!cricketMatch.canEndInnings()) {
            _errorMessage.setValue("Cannot end innings at this time");
            return;
        }
        
        cricketMatch.endCurrentInnings();
        
        // Persist updated match and innings
        offlineMatchRepo.update(cricketMatch).addOnCompleteListener(matchTask -> {
            if (matchTask.isSuccessful()) {
                // CRITICAL FIX: Trigger online sync after offline persistence succeeds
                cricketMatch.notifyObservers();
                
                // Update innings in database
                Innings currentInnings = cricketMatch.getCurrentInnings();
                if (currentInnings != null) {
                    offlineInningsRepo.update(currentInnings);
                }
            } else {
                _errorMessage.setValue("Failed to end innings");
            }
        });
    }

    /**
     * Ends the match completely.
     */
    public void endMatch() {
        _isLoading.setValue(true);
        
        Match currentMatch = offlineMatch.getValue();
        if (currentMatch == null) {
            _errorMessage.setValue("No match loaded");
            _isLoading.setValue(false);
            return;
        }
        
        currentMatch.endMatch();
        
        offlineMatchRepo.update(currentMatch).addOnCompleteListener(task -> {
            _isLoading.setValue(false);
            if (!task.isSuccessful()) {
                _errorMessage.setValue("Failed to end match");
            }
        });
    }

    /**
     * Gets the required run rate for the chasing team.
     */
    public float getRequiredRunRate() {
        Match match = offlineMatch.getValue();
        if (match instanceof CricketMatch) {
            return ((CricketMatch) match).getRequiredRunRate();
        }
        return 0.0f;
    }

    /**
     * Gets the target score for the second innings.
     */
    public int getTargetScore() {
        Match match = offlineMatch.getValue();
        if (match instanceof CricketMatch) {
            return ((CricketMatch) match).getTargetScore();
        }
        return 0;
    }

    /**
     * Gets the match summary text.
     */
    public String getMatchSummary() {
        Match match = offlineMatch.getValue();
        if (match instanceof CricketMatch) {
            return ((CricketMatch) match).getMatchSummary();
        }
        return "Match in progress";
    }

    /**
     * Gets the current innings number (1 or 2).
     */
    public int getCurrentInningsNumber() {
        Match match = offlineMatch.getValue();
        if (match instanceof CricketMatch) {
            return ((CricketMatch) match).getCurrentInningsNumber();
        }
        return 0;
    }

    /**
     * Gets the current score text (e.g., "125/3").
     */
    public String getCurrentScoreText() {
        Match match = offlineMatch.getValue();
        if (match instanceof CricketMatch) {
            CricketMatch cm = (CricketMatch) match;
            Innings innings = cm.getCurrentInnings();
            if (innings != null) {
                return innings.getTotalRuns() + "/" + innings.getWicketsFallen();
            }
        }
        return "0/0";
    }

    /**
     * Gets the current overs text (e.g., "12.3").
     */
    public String getCurrentOversText() {
        Match match = offlineMatch.getValue();
        if (match instanceof CricketMatch) {
            CricketMatch cm = (CricketMatch) match;
            Innings innings = cm.getCurrentInnings();
            Over currentOver = cm.getCurrentOver();
            
            if (innings != null) {
                int completed = innings.getOversCompleted();
                int balls = 0;
                
                if (currentOver != null && currentOver.getBalls() != null) {
                    // Count only legal deliveries
                    for (Ball ball : currentOver.getBalls()) {
                        if (ball.isLegalDelivery()) {
                            balls++;
                        }
                    }
                }
                
                if (balls > 0) {
                    return completed + "." + balls;
                } else {
                    return String.valueOf(completed);
                }
            }
        }
        return "0";
    }

    /**
     * Gets remaining balls in the innings.
     */
    public int getRemainingBalls() {
        Match match = offlineMatch.getValue();
        if (match instanceof CricketMatch) {
            return ((CricketMatch) match).getRemainingBalls();
        }
        return 0;
    }

    /**
     * Checks if match can be started.
     */
    public boolean canStartMatch() {
        Match match = offlineMatch.getValue();
        if (match instanceof CricketMatch) {
            return ((CricketMatch) match).canStartMatch();
        }
        return false;
    }

    /**
     * Checks if innings can be ended.
     */
    public boolean canEndInnings() {
        Match match = offlineMatch.getValue();
        if (match instanceof CricketMatch) {
            return ((CricketMatch) match).canEndInnings();
        }
        return false;
    }

    /**
     * Gets the match status.
     */
    public String getMatchStatus() {
        Match match = offlineMatch.getValue();
        if (match != null) {
            return match.getMatchStatus();
        }
        return "UNKNOWN";
    }

    /**
     * Checks if a valid over exists (needed before adding balls).
     */
    public boolean hasActiveOver() {
        Match match = offlineMatch.getValue();
        if (match instanceof CricketMatch) {
            return ((CricketMatch) match).getCurrentOver() != null;
        }
        return false;
    }

    /**
     * Gets the total extras in current innings.
     */
    public int getExtrasCount() {
        Match match = offlineMatch.getValue();
        if (match instanceof CricketMatch) {
            return ((CricketMatch) match).getExtrasCount();
        }
        return 0;
    }

    // --- Football Action Methods ---

    /**
     * Adds a football goal event to the current match.
     * Uses Command Pattern for undo/redo functionality.
     * 
     * @param teamId The ID of the team that scored
     * @param scorerId The ID of the player who scored
     * @param goalType The type of goal (OPEN_PLAY, PENALTY, FREE_KICK, etc.)
     * @param minute The match minute when the goal was scored
     */
    public void addFootballGoal(String teamId, String scorerId, String goalType, int minute) {
        _isLoading.setValue(true);
        
        Match currentMatch = offlineMatch.getValue();
        if (!(currentMatch instanceof com.example.tournafy.domain.models.match.football.FootballMatch)) {
            _errorMessage.setValue("Not a football match");
            _isLoading.setValue(false);
            return;
        }
        
        com.example.tournafy.domain.models.match.football.FootballMatch footballMatch = 
            (com.example.tournafy.domain.models.match.football.FootballMatch) currentMatch;
        
        // Create FootballEvent
        com.example.tournafy.domain.models.match.football.FootballEvent event = 
            new com.example.tournafy.domain.models.match.football.FootballEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setMatchId(footballMatch.getEntityId());
        event.setTeamId(teamId);
        event.setPlayerId(scorerId);
        event.setEventType("GOAL");
        event.setEventCategory("GOAL");
        event.setMatchMinute(minute);
        event.setMatchPeriod(footballMatch.getMatchPeriod());
        event.setEventTime(new java.util.Date());
        
        // Create GoalDetail
        com.example.tournafy.domain.models.match.football.FootballGoalDetail goalDetail = 
            new com.example.tournafy.domain.models.match.football.FootballGoalDetail();
        goalDetail.setEventId(event.getEventId());
        goalDetail.setScorerId(scorerId);
        goalDetail.setGoalType(goalType);
        goalDetail.setMinuteScored(minute);
        goalDetail.setPenalty(goalType.equals("PENALTY"));
        goalDetail.setOwnGoal(false);
        
        // Create and execute Command
        com.example.tournafy.command.football.AddGoalCommand command = 
            new com.example.tournafy.command.football.AddGoalCommand(footballMatch, event, goalDetail);
        commandManager.executeCommand(command);
        
        // Persist event and match
        offlineFootballEventRepo.add(event).addOnCompleteListener(eventTask -> {
            if (eventTask.isSuccessful()) {
                offlineMatchRepo.update(footballMatch).addOnCompleteListener(matchTask -> {
                    if (matchTask.isSuccessful()) {
                        // Trigger LiveData refresh
                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                            String currentId = _offlineMatchId.getValue();
                            if (currentId != null) {
                                _offlineMatchId.setValue(currentId);
                            }
                            _isLoading.setValue(false);
                        }, 100);
                    } else {
                        _errorMessage.setValue("Failed to save match update");
                        _isLoading.setValue(false);
                    }
                });
            } else {
                _errorMessage.setValue("Failed to save goal event");
                _isLoading.setValue(false);
            }
        });
    }

    /**
     * Adds a football card event (yellow or red card) to the current match.
     * Uses Command Pattern for undo/redo functionality.
     * 
     * @param teamId The ID of the team receiving the card
     * @param playerId The ID of the player receiving the card
     * @param cardType The type of card ("YELLOW" or "RED")
     * @param cardReason The reason for the card (FOUL, DISSENT, etc.)
     * @param minute The match minute when the card was issued
     */
    public void addFootballCard(String teamId, String playerId, String cardType, String cardReason, int minute) {
        _isLoading.setValue(true);
        
        Match currentMatch = offlineMatch.getValue();
        if (!(currentMatch instanceof com.example.tournafy.domain.models.match.football.FootballMatch)) {
            _errorMessage.setValue("Not a football match");
            _isLoading.setValue(false);
            return;
        }
        
        com.example.tournafy.domain.models.match.football.FootballMatch footballMatch = 
            (com.example.tournafy.domain.models.match.football.FootballMatch) currentMatch;
        
        // Create FootballEvent
        com.example.tournafy.domain.models.match.football.FootballEvent event = 
            new com.example.tournafy.domain.models.match.football.FootballEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setMatchId(footballMatch.getEntityId());
        event.setTeamId(teamId);
        event.setPlayerId(playerId);
        event.setEventType("CARD");
        event.setEventCategory("CARD");
        event.setMatchMinute(minute);
        event.setMatchPeriod(footballMatch.getMatchPeriod());
        event.setEventTime(new java.util.Date());
        
        // Create CardDetail
        com.example.tournafy.domain.models.match.football.FootballCardDetail cardDetail = 
            new com.example.tournafy.domain.models.match.football.FootballCardDetail();
        cardDetail.setEventId(event.getEventId());
        cardDetail.setPlayerId(playerId);
        cardDetail.setCardType(cardType);
        cardDetail.setCardReason(cardReason);
        cardDetail.setMinuteIssued(minute);
        cardDetail.setSecondYellow(false); // TODO: Track yellow cards per player
        
        // Create and execute Command
        com.example.tournafy.command.football.AddCardCommand command = 
            new com.example.tournafy.command.football.AddCardCommand(footballMatch, event, cardDetail);
        commandManager.executeCommand(command);
        
        // Persist event and match
        offlineFootballEventRepo.add(event).addOnCompleteListener(eventTask -> {
            if (eventTask.isSuccessful()) {
                offlineMatchRepo.update(footballMatch).addOnCompleteListener(matchTask -> {
                    if (matchTask.isSuccessful()) {
                        // Trigger LiveData refresh
                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                            String currentId = _offlineMatchId.getValue();
                            if (currentId != null) {
                                _offlineMatchId.setValue(currentId);
                            }
                            _isLoading.setValue(false);
                        }, 100);
                    } else {
                        _errorMessage.setValue("Failed to save match update");
                        _isLoading.setValue(false);
                    }
                });
            } else {
                _errorMessage.setValue("Failed to save card event");
                _isLoading.setValue(false);
            }
        });
    }

    /**
     * Adds a football substitution event to the current match.
     * Uses Command Pattern for undo/redo functionality.
     * 
     * @param teamId The ID of the team making the substitution
     * @param playerOutId The ID of the player leaving the field
     * @param playerInId The ID of the player entering the field
     * @param minute The match minute when the substitution occurred
     */
    public void addFootballSubstitution(String teamId, String playerOutId, String playerInId, int minute) {
        _isLoading.setValue(true);
        
        Match currentMatch = offlineMatch.getValue();
        if (!(currentMatch instanceof com.example.tournafy.domain.models.match.football.FootballMatch)) {
            _errorMessage.setValue("Not a football match");
            _isLoading.setValue(false);
            return;
        }
        
        com.example.tournafy.domain.models.match.football.FootballMatch footballMatch = 
            (com.example.tournafy.domain.models.match.football.FootballMatch) currentMatch;
        
        // Create FootballEvent
        com.example.tournafy.domain.models.match.football.FootballEvent event = 
            new com.example.tournafy.domain.models.match.football.FootballEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setMatchId(footballMatch.getEntityId());
        event.setTeamId(teamId);
        event.setPlayerId(playerOutId); // Primary player is the one going out
        event.setEventType("SUBSTITUTION");
        event.setEventCategory("SUBSTITUTION");
        event.setMatchMinute(minute);
        event.setMatchPeriod(footballMatch.getMatchPeriod());
        event.setEventTime(new java.util.Date());
        
        // Create SubstitutionDetail
        com.example.tournafy.domain.models.match.football.FootballSubstitutionDetail subDetail = 
            new com.example.tournafy.domain.models.match.football.FootballSubstitutionDetail();
        subDetail.setEventId(event.getEventId());
        subDetail.setPlayerOutId(playerOutId);
        subDetail.setPlayerInId(playerInId);
        subDetail.setTeamId(teamId);
        subDetail.setMinuteSubstituted(minute);
        subDetail.setSubstitutionReason("TACTICAL"); // Default to tactical
        
        // Create and execute Command
        com.example.tournafy.command.football.SubstitutePlayerCommand command = 
            new com.example.tournafy.command.football.SubstitutePlayerCommand(footballMatch, event, subDetail);
        commandManager.executeCommand(command);
        
        // Persist event and match
        offlineFootballEventRepo.add(event).addOnCompleteListener(eventTask -> {
            if (eventTask.isSuccessful()) {
                offlineMatchRepo.update(footballMatch).addOnCompleteListener(matchTask -> {
                    if (matchTask.isSuccessful()) {
                        // Trigger LiveData refresh
                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                            String currentId = _offlineMatchId.getValue();
                            if (currentId != null) {
                                _offlineMatchId.setValue(currentId);
                            }
                            _isLoading.setValue(false);
                        }, 100);
                    } else {
                        _errorMessage.setValue("Failed to save match update");
                        _isLoading.setValue(false);
                    }
                });
            } else {
                _errorMessage.setValue("Failed to save substitution event");
                _isLoading.setValue(false);
            }
        });
    }

    /**
     * Helper method to parse current minute from timer text "MM:SS"
     */
    private int parseCurrentMinute(String timerText) {
        try {
            String[] parts = timerText.split(":");
            return Integer.parseInt(parts[0]);
        } catch (Exception e) {
            return 0;
        }
    }

    // --- Common Persistence ---

    private void persistOfflineMatch(Match match) {
        // Update the repository
        // Note: FirestoreRepository.update returns a Task. 
        // We assume fire-and-forget for UI responsiveness here,
        // but in a robust app we'd handle success/failure.
        offlineMatchRepo.update(match);
    }

    public void undoLastEvent() {
        if (commandManager.canUndo()) {
            commandManager.undo();
            // After undo, persist the reverted state
            Match match = offlineMatch.getValue();
            if (match != null) {
                persistOfflineMatch(match);
            }
        }
    }

    public void redoLastEvent() {
        if (commandManager.canRedo()) {
            commandManager.redo();
            // After redo, persist the new state
            Match match = offlineMatch.getValue();
            if (match != null) {
                persistOfflineMatch(match);
            }
        }
    }

    public void clearErrorMessage() {
        _errorMessage.setValue(null);
    }

    /**
     * Gets observable for undo button state.
     */
    public boolean canUndo() {
        return commandManager.canUndo();
    }

    /**
     * Gets observable for redo button state.
     */
    public boolean canRedo() {
        return commandManager.canRedo();
    }
}