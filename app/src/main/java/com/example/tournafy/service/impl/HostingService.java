package com.example.tournafy.service.impl;

import com.example.tournafy.data.repository.offline.MatchFirestoreRepository;
import com.example.tournafy.data.repository.offline.SeriesFirestoreRepository;
import com.example.tournafy.data.repository.offline.TournamentFirestoreRepository;
import com.example.tournafy.domain.models.match.cricket.CricketMatch;
import com.example.tournafy.domain.models.match.football.FootballMatch;
import com.example.tournafy.domain.models.series.Series;
import com.example.tournafy.domain.models.tournament.Tournament;
import com.example.tournafy.service.interfaces.IHostingService;
import com.example.tournafy.utils.LinkGenerator;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Concrete implementation of the IHostingService.
 * * FIX APPLIED: Offline-First Optimistic Updates.
 * We no longer wait for .addOnSuccessListener() to fire UI callbacks, 
 * because that listener waits for Server Synchronization (which hangs when offline).
 * Instead, we trigger success immediately after handing the data to the Firestore SDK.
 */
@Singleton
public class HostingService implements IHostingService {

    private final MatchFirestoreRepository matchRepository;
    private final TournamentFirestoreRepository tournamentRepository;
    private final SeriesFirestoreRepository seriesRepository;

    @Inject
    public HostingService(MatchFirestoreRepository matchRepository,
                          TournamentFirestoreRepository tournamentRepository,
                          SeriesFirestoreRepository seriesRepository) {
        this.matchRepository = matchRepository;
        this.tournamentRepository = tournamentRepository;
        this.seriesRepository = seriesRepository;
    }

    @Override
    public void createCricketMatch(CricketMatch.Builder builder, HostingCallback<CricketMatch> callback) {
        try {
            CricketMatch match = builder.build();
            
            // Auto-generate visibility link if not already set
            if (match.getVisibilityLink() == null || match.getVisibilityLink().isEmpty()) {
                String visibilityLink = LinkGenerator.generateLink(match.getName(), match.getEntityId());
                match.setVisibilityLink(visibilityLink);
                android.util.Log.d("HostingService", "Generated visibility link: " + visibilityLink);
            }
            
            android.util.Log.d("HostingService", "Creating match - ID: " + match.getEntityId() + 
                ", Name: " + match.getName() + ", MatchStatus: " + match.getMatchStatus() + 
                ", Teams: " + (match.getTeams() != null ? match.getTeams().size() : 0));
            
            // Fire and forget for UI purposes (Offline First)
            matchRepository.add(match)
                .addOnFailureListener(callback::onError); // Only report if local write completely fails
            
            // Optimistic Success: Don't wait for server sync
            callback.onSuccess(match);
        } catch (Exception e) {
            callback.onError(e);
        }
    }

    @Override
    public void createFootballMatch(FootballMatch.Builder builder, HostingCallback<FootballMatch> callback) {
        try {
            FootballMatch match = builder.build();
            
            // Auto-generate visibility link if not already set
            if (match.getVisibilityLink() == null || match.getVisibilityLink().isEmpty()) {
                String visibilityLink = LinkGenerator.generateLink(match.getName(), match.getEntityId());
                match.setVisibilityLink(visibilityLink);
                android.util.Log.d("HostingService", "Generated visibility link: " + visibilityLink);
            }
            
            matchRepository.add(match)
                .addOnFailureListener(callback::onError);
            
            // Optimistic Success
            callback.onSuccess(match);
        } catch (Exception e) {
            callback.onError(e);
        }
    }

    @Override
    public void createTournament(Tournament.Builder builder, HostingCallback<Tournament> callback) {
        try {
            Tournament tournament = builder.build();
            
            // Auto-generate visibility link if not already set
            if (tournament.getVisibilityLink() == null || tournament.getVisibilityLink().isEmpty()) {
                String visibilityLink = LinkGenerator.generateLink(tournament.getName(), tournament.getEntityId());
                tournament.setVisibilityLink(visibilityLink);
                android.util.Log.d("HostingService", "Generated visibility link: " + visibilityLink);
            }
            
            tournamentRepository.add(tournament)
                .addOnFailureListener(callback::onError);
            
            // Optimistic Success
            callback.onSuccess(tournament);
        } catch (Exception e) {
            callback.onError(e);
        }
    }

    @Override
    public void createSeries(Series.Builder builder, HostingCallback<Series> callback) {
        try {
            Series series = builder.build();
            
            // Auto-generate visibility link if not already set
            if (series.getVisibilityLink() == null || series.getVisibilityLink().isEmpty()) {
                String visibilityLink = LinkGenerator.generateLink(series.getName(), series.getEntityId());
                series.setVisibilityLink(visibilityLink);
                android.util.Log.d("HostingService", "Generated visibility link: " + visibilityLink);
            }
            
            seriesRepository.add(series)
                .addOnFailureListener(callback::onError);
            
            // Optimistic Success
            callback.onSuccess(series);
        } catch (Exception e) {
            callback.onError(e);
        }
    }

    @Override
    public <T> void updateMatch(T match, HostingCallback<Void> callback) {
        if (match instanceof com.example.tournafy.domain.models.base.Match) {
            matchRepository.update((com.example.tournafy.domain.models.base.Match) match)
                    .addOnFailureListener(callback::onError);
            
            // Optimistic Success
            callback.onSuccess(null);
        } else {
            callback.onError(new IllegalArgumentException("Object provided is not a valid Match"));
        }
    }

    @Override
    public void updateTournament(Tournament tournament, HostingCallback<Void> callback) {
        tournamentRepository.update(tournament)
                .addOnFailureListener(callback::onError);
        
        // Optimistic Success
        callback.onSuccess(null);
    }

    @Override
    public void updateSeries(Series series, HostingCallback<Void> callback) {
        seriesRepository.update(series)
                .addOnFailureListener(callback::onError);
        
        // Optimistic Success
        callback.onSuccess(null);
    }
}