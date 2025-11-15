package com.example.tournafy.service.interfaces;

import com.example.tournafy.domain.models.base.Match;
import com.example.tournafy.domain.models.series.Series;
import com.example.tournafy.domain.models.tournament.Tournament;

/**
 * Defines the contract for creating and managing new
 * matches, series, and tournaments. 
 * This service will coordinate with the Builder pattern. 
 */
public interface IHostingService {

    /**
     * Creates a new match.
     * @param matchBuilder The builder object containing match details.
     * @return The newly created Match.
     */
    Match createMatch(Match.Builder matchBuilder);

    /**
     * Creates a new tournament.
     * @param tournamentBuilder The builder object containing tournament details.
     * @return The newly created Tournament.
     */
    Tournament createTournament(Tournament.Builder tournamentBuilder);

    /**
     * Creates a new series.
     * @param seriesBuilder The builder object containing series details.
     * @return The newly created Series.
     */
    Series createSeries(Series.Builder seriesBuilder);

    /**
     * Uploads an offline HostedEntity (Match, Tournament, Series) to
     * make it online.
     * @param entityId The ID of the entity to upload.
     * @param entityType The type of entity (Match, Tournament, Series).
     */
    void uploadOfflineEntity(String entityId, String entityType);
}