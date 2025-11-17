package com.example.tournafy.service.impl;

import com.example.tournafy.data.repository.offline.MatchFirestoreRepository;
import com.example.tournafy.data.repository.offline.SeriesFirestoreRepository;
import com.example.tournafy.data.repository.offline.TournamentFirestoreRepository;
import com.example.tournafy.domain.models.match.cricket.CricketMatch;
import com.example.tournafy.domain.models.match.football.FootballMatch;
import com.example.tournafy.domain.models.series.Series;
import com.example.tournafy.domain.models.tournament.Tournament;
import com.example.tournafy.service.interfaces.IHostingService;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Concrete implementation of the IHostingService.
 * Orchestrates persistence using the Task-based Firestore Repositories.
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
            // FIX: Use addOnSuccessListener on the returned Task
            matchRepository.add(match)
                    .addOnSuccessListener(aVoid -> callback.onSuccess(match))
                    .addOnFailureListener(callback::onError);
        } catch (Exception e) {
            callback.onError(e);
        }
    }

    @Override
    public void createFootballMatch(FootballMatch.Builder builder, HostingCallback<FootballMatch> callback) {
        try {
            FootballMatch match = builder.build();
            // FIX: Use addOnSuccessListener on the returned Task
            matchRepository.add(match)
                    .addOnSuccessListener(aVoid -> callback.onSuccess(match))
                    .addOnFailureListener(callback::onError);
        } catch (Exception e) {
            callback.onError(e);
        }
    }

    @Override
    public void createTournament(Tournament.Builder builder, HostingCallback<Tournament> callback) {
        try {
            Tournament tournament = builder.build();
            tournamentRepository.add(tournament)
                    .addOnSuccessListener(aVoid -> callback.onSuccess(tournament))
                    .addOnFailureListener(callback::onError);
        } catch (Exception e) {
            callback.onError(e);
        }
    }

    @Override
    public void createSeries(Series.Builder builder, HostingCallback<Series> callback) {
        try {
            Series series = builder.build();
            seriesRepository.add(series)
                    .addOnSuccessListener(aVoid -> callback.onSuccess(series))
                    .addOnFailureListener(callback::onError);
        } catch (Exception e) {
            callback.onError(e);
        }
    }

    @Override
    public <T> void updateMatch(T match, HostingCallback<Void> callback) {
        // We need to cast T to a known Match type or use reflection/overloading.
        // Since MatchFirestoreRepository expects a Match object:
        if (match instanceof com.example.tournafy.domain.models.base.Match) {
            matchRepository.update((com.example.tournafy.domain.models.base.Match) match)
                    .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                    .addOnFailureListener(callback::onError);
        } else {
            callback.onError(new IllegalArgumentException("Object provided is not a valid Match"));
        }
    }

    @Override
    public void updateTournament(Tournament tournament, HostingCallback<Void> callback) {
        tournamentRepository.update(tournament)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }

    @Override
    public void updateSeries(Series series, HostingCallback<Void> callback) {
        seriesRepository.update(series)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }
}