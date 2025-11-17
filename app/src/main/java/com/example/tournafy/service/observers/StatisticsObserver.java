package com.example.tournafy.service.observers;

import com.example.tournafy.domain.interfaces.MatchObserver;
import com.example.tournafy.domain.interfaces.SeriesObserver;
import com.example.tournafy.domain.interfaces.TournamentObserver;
import com.example.tournafy.domain.models.base.Match;
import com.example.tournafy.domain.models.base.MatchEvent;
import com.example.tournafy.domain.models.series.Series;
import com.example.tournafy.domain.models.tournament.Tournament;
import com.example.tournafy.service.interfaces.IStatisticsService;

/**
 * Implements the Observer patterns to listen for match events and
 * trigger updates to player statistics.
 *
 * This observer acts as a bridge, delegating the actual calculation
 * logic to the IStatisticsService.
 */
public class StatisticsObserver implements MatchObserver, TournamentObserver, SeriesObserver {

    private final IStatisticsService statisticsService;
    private final String entityId; // Match, Tournament, or Series ID
    private final String entityType; // "Match", "Tournament", "Series"

    /**
     * Constructs a new StatisticsObserver.
     *
     * @param statisticsService The service responsible for all stat calculations.
     * @param entityId The ID of the entity (Match, Tournament, Series) these stats are for.
     * @param entityType The type of the entity.
     */
    public StatisticsObserver(IStatisticsService statisticsService, String entityId, String entityType) {
        this.statisticsService = statisticsService;
        this.entityId = entityId;
        this.entityType = entityType;
    }

    // --- MatchObserver Implementation ---

    /**
     * This is the primary method for this observer.
     * When a new event is added to a match, it is immediately passed
     * to the StatisticsService for processing.
     *
     * @param event The new MatchEvent (e.g., CricketEvent, FootballEvent).
     */
    @Override
    public void onEventAdded(MatchEvent event) {
        if (statisticsService != null && event != null) {
            // Delegate event processing to the Statistics Service.
            // The service will handle the logic for updating PlayerStatistics.
            // Using the existing interface method that takes matchId and eventId
            statisticsService.updateStatisticsFromEvent(event.getMatchId(), event.getEventId());
        }
    }

    /**
     * Called when a match's status changes.
     * When a match is marked "COMPLETED", this could trigger
     * the StatisticsService to finalize stats for the match.
     *
     * @param newStatus The new status (e.g., from MatchStatus enum).
     */
    @Override
    public void onMatchStatusChanged(String newStatus) {
        if ("COMPLETED".equalsIgnoreCase(newStatus) && statisticsService != null) {
            // If the entity being observed is this match, tell the service to finalize.
            // The existing interface doesn't have finalizeMatchStats, so we skip this
            // or the service can handle finalization internally when processing final events
            if ("Match".equalsIgnoreCase(entityType)) {
                // Statistics finalization happens through the last event processing
                // No separate finalize call needed with current interface
            }
        }
    }

    @Override
    public void onMatchUpdated(Match match) {
        // Not typically used for stat calculations, as onEventAdded is more granular.
        // We could use this to recalculate stats if needed, but it's less efficient.
    }

    // --- TournamentObserver Implementation ---

    @Override
    public void onStandingsUpdated(Tournament tournament) {
        // This observer is focused on player stats, not team standings.
    }

    /**
     * When a match in a tournament is completed, this could trigger
     * an aggregation of stats at the tournament level.
     *
     * @param completedMatchId The ID of the match that just finished.
     */
    @Override
    public void onMatchCompleted(String completedMatchId) {
        if (statisticsService != null && "Tournament".equalsIgnoreCase(entityType)) {
            // Tournament-level aggregation happens through individual event processing
            // The service aggregates stats as events are processed
            // No separate aggregation method needed with current interface
            // Stats are already aggregated per player across all tournament matches
        }
    }

    // --- SeriesObserver Implementation ---

    /**
     * When the series score is updated (e.g., a match finishes),
     * this could trigger an aggregation of stats at the series level.
     *
     * @param series The updated Series object.
     */
    @Override
    public void onSeriesScoreUpdated(Series series) {
        if (statisticsService != null && "Series".equalsIgnoreCase(entityType)) {
            // Series-level aggregation happens through individual event processing
            // The service aggregates stats as events are processed across all series matches
            // No separate aggregation method needed with current interface
            // Stats are already aggregated per player across all series matches
        }
    }
}