package com.tournafy.service.interfaces;

// Note: Imports will be valid once domain models are created.
// import com.tournafy.domain.models.base.Match;
// import com.tournafy.domain.models.team.Player;

/**
 * Defines the contract for calculating and updating player
 * and team statistics based on match events. 
 * This will be implemented as an Observer to listen for MatchObserver notifications.
 */
public interface IStatisticsService {

    /**
     * Updates player statistics based on a completed match.
     * @param match The Match that has been completed.
     */
    void updateStatsFromMatch(Match match);

    /**
     * Retrieves the statistics for a specific player.
     * @param playerId The ID of the player.
     * @return A PlayerStatistics object.
     */
    PlayerStatistics getPlayerStats(String playerId);

    /**
     * Retrieves top performers (e.g., top scorer) for a tournament or series.
     *
     * @param entityId The ID of the Tournament or Series.
     * @return A list of aggregated statistics.
     */
    List<AggregatedStat> getTopPerformers(String entityId);
}