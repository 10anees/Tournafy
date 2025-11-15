package com.tournafy.domain.interfaces;
//import com.tournafy.domain.models.tournament.Tournament;

/**
 * Notifies listeners of changes to tournament standings, brackets, or match results.
 */
public interface TournamentObserver {
    /**
     * @param tournament The updated Tournament object.
     */
    void onStandingsUpdated(Tournament tournament);

    /**
     * @param completedMatchId The ID of the match that just finished.
     */
    void onMatchCompleted(String completedMatchId);
}