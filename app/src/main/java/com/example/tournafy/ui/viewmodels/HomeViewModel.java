package com.example.tournafy.ui.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tournafy.data.repository.offline.MatchFirestoreRepository;
import com.example.tournafy.data.repository.offline.SeriesFirestoreRepository;
import com.example.tournafy.data.repository.offline.TournamentFirestoreRepository;
import com.example.tournafy.di.RepositoryQualifiers.OfflineRepo;
import com.example.tournafy.domain.models.base.HostedEntity;
import com.example.tournafy.domain.models.base.Match;
import com.example.tournafy.domain.models.series.Series;
import com.example.tournafy.domain.models.tournament.Tournament;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class HomeViewModel extends ViewModel {

    private final MatchFirestoreRepository matchRepo;
    private final TournamentFirestoreRepository tournamentRepo;
    private final SeriesFirestoreRepository seriesRepo;

    private List<HostedEntity> allEntitiesCache = new ArrayList<>();
    private final MediatorLiveData<List<HostedEntity>> _hostedEntities = new MediatorLiveData<>();
    public final LiveData<List<HostedEntity>> hostedEntities = _hostedEntities;

    private final MutableLiveData<EntityTypeFilter> _currentFilter = new MutableLiveData<>(EntityTypeFilter.ALL);
    
    // Track current data sources to remove when user changes
    private LiveData<List<Match>> currentMatchSource;
    private LiveData<List<Tournament>> currentTournamentSource;
    private LiveData<List<Series>> currentSeriesSource;

    @Inject
    public HomeViewModel(
            @OfflineRepo MatchFirestoreRepository matchRepo,
            @OfflineRepo TournamentFirestoreRepository tournamentRepo,
            @OfflineRepo SeriesFirestoreRepository seriesRepo) {
        this.matchRepo = matchRepo;
        this.tournamentRepo = tournamentRepo;
        this.seriesRepo = seriesRepo;

        // Moved setup logic inside constructor
        setupDataAggregation();
    }

    private void setupDataAggregation() {
        // Get current user ID - only show entities hosted by this user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = currentUser != null ? currentUser.getUid() : null;
        
        if (currentUserId != null) {
            // User is logged in - filter by hostUserId
            currentMatchSource = matchRepo.getMatchesByHostId(currentUserId);
            currentTournamentSource = tournamentRepo.getTournamentsByHostId(currentUserId);
            currentSeriesSource = seriesRepo.getSeriesByHostId(currentUserId);
        } else {
            // User is not logged in - show all (for offline/local matches)
            currentMatchSource = matchRepo.getAll();
            currentTournamentSource = tournamentRepo.getAll();
            currentSeriesSource = seriesRepo.getAll();
        }
        
        _hostedEntities.addSource(currentMatchSource, matches -> {
            updateCache(matches, Match.class);
            applyFilter();
        });

        _hostedEntities.addSource(currentTournamentSource, tournaments -> {
            updateCache(tournaments, Tournament.class);
            applyFilter();
        });

        _hostedEntities.addSource(currentSeriesSource, seriesList -> {
            updateCache(seriesList, Series.class);
            applyFilter();
        });
    }

    private <T extends HostedEntity> void updateCache(List<T> newItems, Class<T> type) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            allEntitiesCache.removeIf(item -> type.isInstance(item));
        } else {
            List<HostedEntity> toRemove = new ArrayList<>();
            for (HostedEntity item : allEntitiesCache) {
                if (type.isInstance(item))
                    toRemove.add(item);
            }
            allEntitiesCache.removeAll(toRemove);
        }

        if (newItems != null) {
            allEntitiesCache.addAll(newItems);
        }
    }

    public void setFilter(EntityTypeFilter filter) {
        _currentFilter.setValue(filter);
        applyFilter();
    }

    private void applyFilter() {
        EntityTypeFilter filter = _currentFilter.getValue();
        if (filter == null)
            filter = EntityTypeFilter.ALL;

        if (filter == EntityTypeFilter.ALL) {
            _hostedEntities.setValue(new ArrayList<>(allEntitiesCache));
            return;
        }

        List<HostedEntity> filteredList = new ArrayList<>();
        for (HostedEntity entity : allEntitiesCache) {
            if (filter == EntityTypeFilter.MATCH && entity instanceof Match) {
                filteredList.add(entity);
            } else if (filter == EntityTypeFilter.TOURNAMENT && entity instanceof Tournament) {
                filteredList.add(entity);
            } else if (filter == EntityTypeFilter.SERIES && entity instanceof Series) {
                filteredList.add(entity);
            }
        }
        _hostedEntities.setValue(filteredList);
    }

    // In HomeViewModel.java
    public void deleteEntity(HostedEntity entity) {
        if (entity instanceof Match) {
            matchRepo.delete(entity.getEntityId());
        } else if (entity instanceof Tournament) {
            tournamentRepo.delete(entity.getEntityId());
        } else if (entity instanceof Series) {
            seriesRepo.delete(entity.getEntityId());
        }
        // LiveData will update automatically
    }

    public enum EntityTypeFilter {
        ALL, MATCH, TOURNAMENT, SERIES
    }
}