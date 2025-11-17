package com.example.tournafy.service.interfaces;

import androidx.lifecycle.LiveData;
import com.example.tournafy.domain.models.statistics.PlayerStatistics;
import java.util.List;

/**
 * Service interface for calculating and retrieving statistics. 
 * This service is responsible for aggregating data to show
 * "Top Players" lists and individual player stats.
 */
public interface IStatisticsService {

    /**
     * Gets the statistics for a single player within a specific entity (match, tournament, series).
     *
     * @param playerId  The ID of the player.
     * @param entityId  The ID of the entity (match, tournament, etc.).
     * @return LiveData wrapping the PlayerStatistics.
     */
    LiveData<PlayerStatistics> getPlayerStatistics(String playerId, String entityId);

    /**
     * Gets the aggregated "Top Players" list for a tournament.
     *
     * @param tournamentId The ID of the tournament.
     * @return LiveData wrapping a list of PlayerStatistics, sorted by a key metric.
     */
    LiveData<List<PlayerStatistics>> getTournamentTopPlayers(String tournamentId);

    /**
     * Gets the aggregated "Top Players" list for a series.
     *
     * @param seriesId The ID of the series.
     * @return LiveData wrapping a list of PlayerStatistics, sorted by a key metric.
     */
    LiveData<List<PlayerStatistics>> getSeriesTopPlayers(String seriesId);

    /**
     * This method would be triggered by an observer [cite: 127] to update
     * statistics whenever a relevant match event (like a goal or wicket) occurs.
     *
     * @param matchId The ID of the match where the event occurred.
     * @param eventId   The ID of the event that just happened.
     */
    void updateStatisticsFromEvent(String matchId, String eventId);
}