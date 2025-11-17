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
import com.example.tournafy.domain.models.match.cricket.CricketMatch;
import com.example.tournafy.domain.models.match.cricket.Innings;
import com.example.tournafy.domain.models.match.cricket.Over;
import com.example.tournafy.domain.models.match.football.FootballEvent;
import com.example.tournafy.service.interfaces.IEventService;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MatchViewModel extends ViewModel {

    // --- OFFLINE Repositories (for Host) ---
    private final MatchFirestoreRepository offlineMatchRepo;
    private final InningsFirestoreRepository offlineInningsRepo;
    private final BallFirestoreRepository offlineBallRepo;
    private final FootballEventFirestoreRepository offlineFootballEventRepo;

    // --- ONLINE Repositories (for Viewer) ---
    private final MatchFirebaseRepository onlineMatchRepo;
    private final InningsFirebaseRepository onlineInningsRepo;
    private final BallFirebaseRepository onlineBallRepo;
    private final FootballEventFirebaseRepository onlineFootballEventRepo;

    // --- Services (for Host) ---
    private final IEventService eventService;
    private final MatchCommandManager commandManager;

    // --- Triggers ---
    private final MutableLiveData<String> _offlineMatchId = new MutableLiveData<>();
    private final MutableLiveData<String> _onlineMatchId = new MutableLiveData<>();
    private final MutableLiveData<String> _onlineOverId = new MutableLiveData<>();

    // --- OFFLINE LiveData (for Host UI) ---
    public final LiveData<Match> offlineMatch;
    public final LiveData<List<Innings>> offlineInningsList;

    // --- ONLINE LiveData (for Viewer UI) ---
    public final LiveData<Match> onlineMatch;
    public final LiveData<List<FootballEvent>> onlineFootballEvents;
    public final LiveData<List<Ball>> onlineBallStream;
    public final LiveData<List<Innings>> onlineInningsList;

    // Error and loading states
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public final LiveData<String> errorMessage = _errorMessage;
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoading = _isLoading;

    @Inject
    public MatchViewModel(
            @OfflineRepo MatchFirestoreRepository offlineMatchRepo,
            @OfflineRepo InningsFirestoreRepository offlineInningsRepo,
            @OfflineRepo BallFirestoreRepository offlineBallRepo,
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
        this.offlineFootballEventRepo = offlineFootballEventRepo;

        this.onlineMatchRepo = onlineMatchRepo;
        this.onlineInningsRepo = onlineInningsRepo;
        this.onlineBallRepo = onlineBallRepo;
        this.onlineFootballEventRepo = onlineFootballEventRepo;

        this.eventService = eventService;
        this.commandManager = commandManager;

        // Initialize LiveData Transformations
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

    // --- Helper Methods for Fragments ---

    public Match getOfflineMatch() {
        return offlineMatch.getValue();
    }

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
        // Placeholder calculation
        return 0.0f;
    }

    public String getStrikerId() { return "striker_placeholder"; }
    public String getNonStrikerId() { return "non_striker_placeholder"; }

    public Over getCurrentOver() {
        Match match = offlineMatch.getValue();
        if (match instanceof CricketMatch) {
            return ((CricketMatch) match).getCurrentOver();
        }
        return null;
    }

    // --- Event Handling Methods (for Host) ---

    public void addCricketBall(int runs) {
        // Use Command Pattern here
        // AddBallCommand command = new AddBallCommand(runs, ...);
        // eventService.executeCommand(command, commandManager, ...);
        addCricketEvent(); // Placeholder call
    }

    public void addCricketWicket(String wicketType) {
        // AddWicketCommand logic
        addCricketEvent(); // Placeholder call
    }

    public void addCricketExtra(String extraType) {
        // AddExtrasCommand logic
        addCricketEvent(); // Placeholder call
    }

    public void addCricketEvent() {
        // Generic trigger to update UI or Repo
    }

    public void addFootballEvent() {
        // Implementation placeholder
    }

    public void undoLastEvent() {
        if (commandManager.canUndo()) {
            commandManager.undo();
        }
    }

    public void redoLastEvent() {
        if (commandManager.canRedo()) {
            commandManager.redo();
        }
    }

    public void clearErrorMessage() {
        _errorMessage.setValue(null);
    }
}