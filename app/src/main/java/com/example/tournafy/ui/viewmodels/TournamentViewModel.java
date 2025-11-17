package com.example.tournafy.ui.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

// Import Online Repos
import com.example.tournafy.data.repository.online.PlayerStatisticsFirebaseRepository;
import com.example.tournafy.data.repository.online.TournamentFirebaseRepository;
// Import Offline Repos
import com.example.tournafy.data.repository.offline.PlayerStatisticsFirestoreRepository;
import com.example.tournafy.data.repository.offline.TournamentFirestoreRepository;

import com.example.tournafy.di.RepositoryQualifiers.OfflineRepo;
import com.example.tournafy.di.RepositoryQualifiers.OnlineRepo;

import com.example.tournafy.domain.models.statistics.PlayerStatistics;
import com.example.tournafy.domain.models.team.TournamentTeam;
import com.example.tournafy.domain.models.tournament.Tournament;
import com.example.tournafy.service.interfaces.ITournamentService;
import com.example.tournafy.service.strategies.tournament.IBracketGenerationStrategy;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class TournamentViewModel extends ViewModel {

    private final ITournamentService tournamentService;
    // Offline Repos
    private final TournamentFirestoreRepository offlineTournamentRepo;
    private final PlayerStatisticsFirestoreRepository offlinePlayerStatsRepo;
    // Online Repos
    private final TournamentFirebaseRepository onlineTournamentRepo;
    private final PlayerStatisticsFirebaseRepository onlinePlayerStatsRepo;

    // --- Triggers ---
    private final MutableLiveData<String> _offlineTournamentId = new MutableLiveData<>();
    private final MutableLiveData<String> _onlineTournamentId = new MutableLiveData<>();

    // --- OFFLINE LiveData (for Host UI) ---
    // Logic moved to constructor
    public final LiveData<Tournament> offlineTournament;
    public final LiveData<List<PlayerStatistics>> offlinePlayerStats;

    // This doesn't depend on repos, so it can stay here
    private final MutableLiveData<List<TournamentTeam>> _tournamentTeams = new MutableLiveData<>();
    public final LiveData<List<TournamentTeam>> tournamentTeams = _tournamentTeams;


    // --- ONLINE LiveData (for Viewer UI) ---
    // Logic moved to constructor
    public final LiveData<Tournament> onlineTournament;
    public final LiveData<List<PlayerStatistics>> onlinePlayerStats;


    // --- States ---
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public final LiveData<String> errorMessage = _errorMessage;

    @Inject
    public TournamentViewModel(
            ITournamentService tournamentService,
            @OfflineRepo TournamentFirestoreRepository offlineTournamentRepo,
            @OfflineRepo PlayerStatisticsFirestoreRepository offlinePlayerStatsRepo,
            @OnlineRepo TournamentFirebaseRepository onlineTournamentRepo,
            @OnlineRepo PlayerStatisticsFirebaseRepository onlinePlayerStatsRepo
    ) {
        // 1. Assign Dependencies
        this.tournamentService = tournamentService;
        this.offlineTournamentRepo = offlineTournamentRepo;
        this.offlinePlayerStatsRepo = offlinePlayerStatsRepo;
        this.onlineTournamentRepo = onlineTournamentRepo;
        this.onlinePlayerStatsRepo = onlinePlayerStatsRepo;

        // 2. Initialize LiveData Transformations AFTER assignment
        this.offlineTournament = Transformations.switchMap(_offlineTournamentId,
                id -> offlineTournamentRepo.getById(id)
        );
        this.offlinePlayerStats = Transformations.switchMap(_offlineTournamentId,
                id -> offlinePlayerStatsRepo.getStatsForEntity(id)
        );

        this.onlineTournament = Transformations.switchMap(_onlineTournamentId,
                id -> onlineTournamentRepo.getById(id)
        );
        this.onlinePlayerStats = Transformations.switchMap(_onlineTournamentId,
                id -> onlinePlayerStatsRepo.getStatisticsByTournamentId(id)
        );
    }

    public void loadOfflineTournament(String tournamentId) {
        _offlineTournamentId.setValue(tournamentId);
        loadTournamentTeams(tournamentId);
    }

    public void loadOnlineTournament(String tournamentId) {
        _onlineTournamentId.setValue(tournamentId);
    }

    private void loadTournamentTeams(String tournamentId) {
        _isLoading.setValue(true);
        tournamentService.getTournamentTeams(tournamentId, new ITournamentService.TournamentCallback<List<TournamentTeam>>() {
            @Override
            public void onSuccess(List<TournamentTeam> result) {
                _tournamentTeams.setValue(result);
                _isLoading.setValue(false);
            }

            @Override
            public void onError(Exception e) {
                _errorMessage.setValue("Failed to load teams: " + e.getMessage());
                _isLoading.setValue(false);
            }
        });
    }

    public void onMatchCompleted(String completedMatchId) {
        String tournamentId = _offlineTournamentId.getValue();
        if (tournamentId == null) return;

        _isLoading.setValue(true);
        tournamentService.updateStandings(tournamentId, completedMatchId, new ITournamentService.TournamentCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                _isLoading.setValue(false);
            }

            @Override
            public void onError(Exception e) {
                _errorMessage.setValue("Failed to update standings: " + e.getMessage());
                _isLoading.setValue(false);
            }
        });
    }

    public void onGenerateBracketsClicked(IBracketGenerationStrategy strategy) {
        Tournament currentTournament = offlineTournament.getValue();
        if (currentTournament == null) {
            _errorMessage.setValue("Tournament not loaded.");
            return;
        }

        _isLoading.setValue(true);
        tournamentService.generateBrackets(currentTournament, strategy, new ITournamentService.TournamentCallback<Tournament>() {
            @Override
            public void onSuccess(Tournament updatedTournament) {
                _isLoading.setValue(false);
            }

            @Override
            public void onError(Exception e) {
                _errorMessage.setValue("Failed to generate brackets: " + e.getMessage());
                _isLoading.setValue(false);
            }
        });
    }

    public void clearErrorMessage() {
        _errorMessage.setValue(null);
    }
}