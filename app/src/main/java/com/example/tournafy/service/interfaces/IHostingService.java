package com.example.tournafy.service.interfaces;

import com.example.tournafy.domain.models.match.cricket.CricketMatch;
import com.example.tournafy.domain.models.match.football.FootballMatch;
import com.example.tournafy.domain.models.series.Series;
import com.example.tournafy.domain.models.tournament.Tournament;

/**
 * Service interface for handling the creation and management of
 * matches, tournaments, and series. 
 * This service orchestrates the use of factories and builders
 * to create new hosted entities. 
 */
public interface IHostingService {

    /**
     * Creates a new Cricket Match using the Builder pattern.
     *
     * @param builder A pre-configured CricketMatch.Builder.
     * @param callback Callback to return the created match or an error.
     */
    void createCricketMatch(CricketMatch.Builder builder, HostingCallback<CricketMatch> callback);

    /**
     * Creates a new Football Match using the Builder pattern.
     *
     * @param builder A pre-configured FootballMatch.Builder.
     * @param callback Callback to return the created match or an error.
     */
    void createFootballMatch(FootballMatch.Builder builder, HostingCallback<FootballMatch> callback);

    /**
     * Creates a new Tournament using the Builder pattern. 
     *
     * @param builder A pre-configured Tournament.Builder.
     * @param callback Callback to return the created tournament or an error.
     */
    void createTournament(Tournament.Builder builder, HostingCallback<Tournament> callback);

    /**
     * Creates a new Series using the Builder pattern. 
     *
     * @param builder A pre-configured Series.Builder.
     * @param callback Callback to return the created series or an error.
     */
    void createSeries(Series.Builder builder, HostingCallback<Series> callback);

    /**
     * Updates an existing match.
     *
     * @param match The match object with updated data.
     * @param callback Callback to signal success or error.
     */
    <T> void updateMatch(T match, HostingCallback<Void> callback);

    /**
     * Updates an existing tournament.
     *
     * @param tournament The tournament object with updated data.
     * @param callback Callback to signal success or error.
     */
    void updateTournament(Tournament tournament, HostingCallback<Void> callback);

    /**
     * Updates an existing series.
     *
     * @param series The series object with updated data.
     * @param callback Callback to signal success or error.
     */
    void updateSeries(Series series, HostingCallback<Void> callback);

    /**
     * A generic callback interface for asynchronous hosting operations.
     *
     * @param <T> The type of the successful result.
     */
    interface HostingCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }
}