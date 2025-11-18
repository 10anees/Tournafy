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

    public void loadOfflineMatch(String matchId) {
        _offlineMatchId.setValue(matchId);
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
            if (innings != null && innings.getOversCompleted() > 0) {
                // CRR = Runs / Overs
                // Note: 1.3 overs = 1.5 in decimal for math? 
                // Standard CRR uses balls: (Runs / Balls) * 6
                int totalBalls = (innings.getOversCompleted() * 6); 
                // Add current over balls
                Over currentOver = cm.getCurrentOver();
                if (currentOver != null && currentOver.getBalls() != null) {
                    totalBalls += currentOver.getBalls().size();
                }
                
                if (totalBalls > 0) {
                    return (float) innings.getTotalRuns() / totalBalls * 6;
                }
            }
        }
        return 0.0f;
    }

    public String getStrikerId() {
        // Placeholder: In real app, track striker in CricketMatch state
        return "Striker";
    }

    public String getNonStrikerId() {
        return "Non-Striker";
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
        
        // Get current innings and over
        Innings currentInnings = cricketMatch.getCurrentInnings();
        Over currentOver = cricketMatch.getCurrentOver();
        
        if (currentInnings == null || currentOver == null) {
            _errorMessage.setValue("Match not started or no active over");
            _isLoading.setValue(false);
            return;
        }
        
        // --- STEP 1: Create CricketEvent with ALL Foreign Keys ---
        CricketEvent event = new CricketEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setMatchId(cricketMatch.getEntityId());
        event.setTeamId(currentInnings.getBattingTeamId());
        event.setEventType("BALL");
        event.setOverNumber(currentOver.getOverNumber());
        event.setBallNumber(currentOver.getBalls() != null ? currentOver.getBalls().size() + 1 : 1);
        event.setTotalRuns(runs);
        event.setRunsScoredBat(runs);
        event.setRunsScoredExtras(0);
        event.setLegalDelivery(true);
        event.setWicket(false);
        event.setExtrasType("NONE");
        event.setBoundary(runs == 4 || runs == 6);
        event.setBoundaryType(runs == 4 ? 4 : (runs == 6 ? 6 : 0));
        event.setEventTime(new java.util.Date());
        
        // --- STEP 2: Process event to update in-memory state (score) ---
        cricketMatch.processEvent(event);
        
        // --- STEP 3: Create Ball entity with ALL Foreign Keys ---
        Ball ball = new Ball();
        ball.setBallId(java.util.UUID.randomUUID().toString());
        ball.setMatchId(cricketMatch.getEntityId());
        ball.setInningsId(currentInnings.getInningsId());
        ball.setOverId(currentOver.getOverId());
        ball.setInningsNumber(currentInnings.getInningsNumber());
        ball.setOverNumber(currentOver.getOverNumber());
        ball.setBallNumber(event.getBallNumber());
        ball.setRunsScored(runs);
        ball.setWicket(false);
        ball.setBoundary(runs == 4 || runs == 6);
        ball.setExtrasType("NONE");
        // TODO: Set batsmanId and bowlerId from match state when available
        
        // --- STEP 4: Persist Ball to database ---
        offlineBallRepo.add(ball).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // --- STEP 5: Update Over in database (includes the new ball) ---
                offlineOverRepo.update(currentOver).addOnCompleteListener(overTask -> {
                    if (overTask.isSuccessful()) {
                        // --- STEP 6: Update Innings in database (updated score) ---
                        offlineInningsRepo.update(currentInnings).addOnCompleteListener(inningsTask -> {
                            if (inningsTask.isSuccessful()) {
                                // --- STEP 7: Update Match in database ---
                                offlineMatchRepo.update(cricketMatch).addOnCompleteListener(matchTask -> {
                                    _isLoading.setValue(false);
                                    if (!matchTask.isSuccessful()) {
                                        _errorMessage.setValue("Failed to save match update");
                                    }
                                });
                            } else {
                                _isLoading.setValue(false);
                                _errorMessage.setValue("Failed to save innings update");
                            }
                        });
                    } else {
                        _isLoading.setValue(false);
                        _errorMessage.setValue("Failed to save over update");
                    }
                });
            } else {
                _isLoading.setValue(false);
                _errorMessage.setValue("Failed to save ball");
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
        Over currentOver = cricketMatch.getCurrentOver();
        
        if (currentInnings == null || currentOver == null) {
            _errorMessage.setValue("Match not started or no active over");
            _isLoading.setValue(false);
            return;
        }
        
        // --- Create CricketEvent with ALL FKs ---
        CricketEvent event = new CricketEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setMatchId(cricketMatch.getEntityId());
        event.setTeamId(currentInnings.getBattingTeamId());
        event.setEventType("WICKET");
        event.setOverNumber(currentOver.getOverNumber());
        event.setBallNumber(currentOver.getBalls() != null ? currentOver.getBalls().size() + 1 : 1);
        event.setTotalRuns(0); // Wickets usually score 0 runs unless run out
        event.setRunsScoredBat(0);
        event.setRunsScoredExtras(0);
        event.setLegalDelivery(true);
        event.setWicket(true);
        event.setExtrasType("NONE");
        event.setBoundary(false);
        event.setBoundaryType(0);
        event.setEventTime(new java.util.Date());
        
        // Create wicket detail
        CricketWicketDetail wicketDetail = new CricketWicketDetail();
        wicketDetail.setWicketType(wicketType);
        event.setWicketDetail(wicketDetail);
        
        // --- Process event ---
        cricketMatch.processEvent(event);
        
        // --- Create Ball entity ---
        Ball ball = new Ball();
        ball.setBallId(java.util.UUID.randomUUID().toString());
        ball.setMatchId(cricketMatch.getEntityId());
        ball.setInningsId(currentInnings.getInningsId());
        ball.setOverId(currentOver.getOverId());
        ball.setInningsNumber(currentInnings.getInningsNumber());
        ball.setOverNumber(currentOver.getOverNumber());
        ball.setBallNumber(event.getBallNumber());
        ball.setRunsScored(0);
        ball.setWicket(true);
        ball.setWicketType(wicketType);
        ball.setBoundary(false);
        ball.setExtrasType("NONE");
        
        // --- Persist ---
        offlineBallRepo.add(ball).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Update Over
                offlineOverRepo.update(currentOver).addOnCompleteListener(overTask -> {
                    if (overTask.isSuccessful()) {
                        // Update Innings
                        offlineInningsRepo.update(currentInnings).addOnCompleteListener(inningsTask -> {
                            if (inningsTask.isSuccessful()) {
                                // Update Match
                                offlineMatchRepo.update(cricketMatch).addOnCompleteListener(matchTask -> {
                                    _isLoading.setValue(false);
                                    if (!matchTask.isSuccessful()) {
                                        _errorMessage.setValue("Failed to save match update");
                                    }
                                });
                            } else {
                                _isLoading.setValue(false);
                                _errorMessage.setValue("Failed to save innings update");
                            }
                        });
                    } else {
                        _isLoading.setValue(false);
                        _errorMessage.setValue("Failed to save over update");
                    }
                });
            } else {
                _isLoading.setValue(false);
                _errorMessage.setValue("Failed to save wicket");
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
        Over currentOver = cricketMatch.getCurrentOver();
        
        if (currentInnings == null || currentOver == null) {
            _errorMessage.setValue("Match not started or no active over");
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
        event.setOverNumber(currentOver.getOverNumber());
        event.setBallNumber(currentOver.getBalls() != null ? currentOver.getBalls().size() + 1 : 1);
        event.setTotalRuns(extrasRuns);
        event.setRunsScoredBat(0);
        event.setRunsScoredExtras(extrasRuns);
        event.setLegalDelivery(isLegal);
        event.setWicket(false);
        event.setExtrasType(extrasType);
        event.setBoundary(false);
        event.setBoundaryType(0);
        event.setEventTime(new java.util.Date());
        
        // --- Process event ---
        cricketMatch.processEvent(event);
        
        // --- Create Ball entity ---
        Ball ball = new Ball();
        ball.setBallId(java.util.UUID.randomUUID().toString());
        ball.setMatchId(cricketMatch.getEntityId());
        ball.setInningsId(currentInnings.getInningsId());
        ball.setOverId(currentOver.getOverId());
        ball.setInningsNumber(currentInnings.getInningsNumber());
        ball.setOverNumber(currentOver.getOverNumber());
        ball.setBallNumber(event.getBallNumber());
        ball.setRunsScored(extrasRuns);
        ball.setWicket(false);
        ball.setBoundary(false);
        ball.setExtrasType(extrasType);
        
        // --- Persist ---
        offlineBallRepo.add(ball).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Update Over
                offlineOverRepo.update(currentOver).addOnCompleteListener(overTask -> {
                    if (overTask.isSuccessful()) {
                        // Update Innings
                        offlineInningsRepo.update(currentInnings).addOnCompleteListener(inningsTask -> {
                            if (inningsTask.isSuccessful()) {
                                // Update Match
                                offlineMatchRepo.update(cricketMatch).addOnCompleteListener(matchTask -> {
                                    _isLoading.setValue(false);
                                    if (!matchTask.isSuccessful()) {
                                        _errorMessage.setValue("Failed to save match update");
                                    }
                                });
                            } else {
                                _isLoading.setValue(false);
                                _errorMessage.setValue("Failed to save innings update");
                            }
                        });
                    } else {
                        _isLoading.setValue(false);
                        _errorMessage.setValue("Failed to save over update");
                    }
                });
            } else {
                _isLoading.setValue(false);
                _errorMessage.setValue("Failed to save extra");
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
            if (!task.isSuccessful()) {
                _errorMessage.setValue("Failed to end over");
            }
        });
    }

    /**
     * Starts the match - initializes innings and sets status to LIVE.
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
            
            if (!cricketMatch.canStartMatch()) {
                _errorMessage.setValue("Match cannot be started - check configuration");
                _isLoading.setValue(false);
                return;
            }
            
            cricketMatch.startMatch();
            
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
        } else {
            // Football or other sport
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

    public void addFootballEvent() {
        // Placeholder for football event creation
        // FootballEvent event = new FootballEvent(...);
        // fm.processEvent(event);
        // persistOfflineMatch(fm);
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