package com.example.tournafy.ui.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tournafy.domain.models.base.HostedEntity;
import com.example.tournafy.domain.models.match.cricket.CricketMatch;
import com.example.tournafy.domain.models.match.football.FootballMatch;
import com.example.tournafy.domain.models.series.Series;
import com.example.tournafy.domain.models.tournament.Tournament;
import com.example.tournafy.service.interfaces.IHostingService;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * ViewModel for handling the creation of new matches, tournaments, and series (MVVM).
 *
 * This class is used by the "HostNew..." fragments. It takes configured
 * builders from the UI, passes them to the IHostingService, and reports the
 * outcome (success, error, loading) back to the UI via LiveData.
 */
@HiltViewModel
public class HostViewModel extends ViewModel {

    private final IHostingService hostingService;

    // --- Shared State for Match Wizard ---
    // FIX: Added this field so AddMatchDetailsFragment can pass the name to HostNewMatchFragment
    public final MutableLiveData<String> matchNameInput = new MutableLiveData<>("");
    public final MutableLiveData<Integer> playersPerSide = new MutableLiveData<>(11); // Default 11 players

    // --- Creation State ---
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<HostedEntity> _creationSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();

    // Public LiveData for the UI to observe
    public final LiveData<Boolean> isLoading = _isLoading;
    public final LiveData<HostedEntity> creationSuccess = _creationSuccess;
    public final LiveData<String> errorMessage = _errorMessage;

    @Inject
    public HostViewModel(IHostingService hostingService) {
        this.hostingService = hostingService;
    }

    /**
     * Attempts to create a new Cricket Match.
     *
     * @param builder The configured builder from the UI.
     */
    public void createCricketMatch(CricketMatch.Builder builder) {
        _isLoading.setValue(true);
        hostingService.createCricketMatch(builder, new IHostingService.HostingCallback<CricketMatch>() {
            @Override
            public void onSuccess(CricketMatch result) {
                _isLoading.setValue(false);
                _creationSuccess.setValue(result);
            }

            @Override
            public void onError(Exception e) {
                _isLoading.setValue(false);
                _errorMessage.setValue(e.getMessage());
            }
        });
    }

    /**
     * Attempts to create a new Football Match.
     *
     * @param builder The configured builder from the UI.
     */
    public void createFootballMatch(FootballMatch.Builder builder) {
        _isLoading.setValue(true);
        hostingService.createFootballMatch(builder, new IHostingService.HostingCallback<FootballMatch>() {
            @Override
            public void onSuccess(FootballMatch result) {
                _isLoading.setValue(false);
                _creationSuccess.setValue(result);
            }

            @Override
            public void onError(Exception e) {
                _isLoading.setValue(false);
                _errorMessage.setValue(e.getMessage());
            }
        });
    }

    /**
     * Attempts to create a new Tournament.
     *
     * @param builder The configured builder from the UI.
     */
    public void createTournament(Tournament.Builder builder) {
        _isLoading.setValue(true);
        hostingService.createTournament(builder, new IHostingService.HostingCallback<Tournament>() {
            @Override
            public void onSuccess(Tournament result) {
                _isLoading.setValue(false);
                _creationSuccess.setValue(result);
            }

            @Override
            public void onError(Exception e) {
                _isLoading.setValue(false);
                _errorMessage.setValue(e.getMessage());
            }
        });
    }

    /**
     * Attempts to create a new Series.
     *
     * @param builder The configured builder from the UI.
     */
    public void createSeries(Series.Builder builder) {
        _isLoading.setValue(true);
        hostingService.createSeries(builder, new IHostingService.HostingCallback<Series>() {
            @Override
            public void onSuccess(Series result) {
                _isLoading.setValue(false);
                _creationSuccess.setValue(result);
            }

            @Override
            public void onError(Exception e) {
                _isLoading.setValue(false);
                _errorMessage.setValue(e.getMessage());
            }
        });
    }

    /**
     * Clears the success event after it has been handled (e.g., after navigation).
     */
    public void clearSuccessEvent() {
        _creationSuccess.setValue(null);
    }

    /**
     * Clears the error message after it has been shown to the user.
     */
    public void clearErrorMessage() {
        _errorMessage.setValue(null);
    }
}