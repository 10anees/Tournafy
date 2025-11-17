package com.example.tournafy.ui.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.tournafy.data.repository.offline.PlayerStatisticsFirestoreRepository;
import com.example.tournafy.data.repository.offline.SeriesFirestoreRepository;
import com.example.tournafy.data.repository.online.PlayerStatisticsFirebaseRepository;
import com.example.tournafy.data.repository.online.SeriesFirebaseRepository;
import com.example.tournafy.di.RepositoryQualifiers.OfflineRepo;
import com.example.tournafy.di.RepositoryQualifiers.OnlineRepo;
import com.example.tournafy.domain.models.series.Series;
import com.example.tournafy.domain.models.statistics.PlayerStatistics;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SeriesViewModel extends ViewModel {

    private final SeriesFirestoreRepository offlineSeriesRepo;
    private final PlayerStatisticsFirestoreRepository offlinePlayerStatsRepo;
    private final SeriesFirebaseRepository onlineSeriesRepo;
    private final PlayerStatisticsFirebaseRepository onlinePlayerStatsRepo;

    private final MutableLiveData<String> _offlineSeriesId = new MutableLiveData<>();
    private final MutableLiveData<String> _onlineSeriesId = new MutableLiveData<>();

    // LiveData declared here but initialized in constructor
    public final LiveData<Series> offlineSeries;
    public final LiveData<List<PlayerStatistics>> offlinePlayerStats;
    public final LiveData<Series> onlineSeries;
    public final LiveData<List<PlayerStatistics>> onlinePlayerStats;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public final LiveData<String> errorMessage = _errorMessage;

    @Inject
    public SeriesViewModel(
            @OfflineRepo SeriesFirestoreRepository offlineSeriesRepo,
            @OfflineRepo PlayerStatisticsFirestoreRepository offlinePlayerStatsRepo,
            @OnlineRepo SeriesFirebaseRepository onlineSeriesRepo,
            @OnlineRepo PlayerStatisticsFirebaseRepository onlinePlayerStatsRepo
    ) {
        this.offlineSeriesRepo = offlineSeriesRepo;
        this.offlinePlayerStatsRepo = offlinePlayerStatsRepo;
        this.onlineSeriesRepo = onlineSeriesRepo;
        this.onlinePlayerStatsRepo = onlinePlayerStatsRepo;

        // Initialization moved here to ensure repos are assigned
        this.offlineSeries = Transformations.switchMap(_offlineSeriesId,
                id -> offlineSeriesRepo.getById(id)
        );
        this.offlinePlayerStats = Transformations.switchMap(_offlineSeriesId,
                id -> offlinePlayerStatsRepo.getStatsForEntity(id)
        );
        this.onlineSeries = Transformations.switchMap(_onlineSeriesId,
                id -> onlineSeriesRepo.getById(id)
        );
        this.onlinePlayerStats = Transformations.switchMap(_onlineSeriesId,
                id -> onlinePlayerStatsRepo.getStatisticsBySeriesId(id)
        );
    }

    public void loadOfflineSeries(String seriesId) {
        _offlineSeriesId.setValue(seriesId);
    }

    public void loadOnlineSeries(String seriesId) {
        _onlineSeriesId.setValue(seriesId);
    }

    public void updateOfflineSeries(Series updatedSeries) {
        _isLoading.setValue(true);
        offlineSeriesRepo.update(updatedSeries)
                .addOnSuccessListener(aVoid -> _isLoading.setValue(false))
                .addOnFailureListener(e -> {
                    _errorMessage.setValue("Failed to update series: " + e.getMessage());
                    _isLoading.setValue(false);
                });
    }

    public void clearErrorMessage() {
        _errorMessage.setValue(null);
    }
}