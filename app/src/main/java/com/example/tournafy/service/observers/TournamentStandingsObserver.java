package com.example.tournafy.service.observers;

import com.example.tournafy.domain.interfaces.TournamentObserver;
import com.example.tournafy.domain.models.tournament.Tournament;
import com.example.tournafy.service.interfaces.ITournamentService;

/**
 * Implements the TournamentObserver pattern to react to changes
 * within a tournament, specifically for updating standings, tables, and brackets.
 *
 * This observer delegates the complex calculation and update logic
 * to the ITournamentService.
 */
public class TournamentStandingsObserver implements TournamentObserver {

    private final ITournamentService tournamentService;
    private final String tournamentId;

    /**
     * Constructs a new TournamentStandingsObserver.
     *
     * @param tournamentService The service responsible for tournament logic.
     * @param tournamentId The ID of the tournament this observer is watching.
     */
    public TournamentStandingsObserver(ITournamentService tournamentService, String tournamentId) {
        this.tournamentService = tournamentService;
        this.tournamentId = tournamentId;
    }

    /**
     * Called when the overall tournament object is updated.
     * This might be used to trigger a general refresh, but onMatchCompleted
     * is typically more specific and useful.
     *
     * @param tournament The updated Tournament object.
     */
    @Override
    public void onStandingsUpdated(Tournament tournament) {
        if (tournamentService != null) {
            // A general update was pushed. We could refresh standings.
            // This is less common than reacting to a specific match completion.
            // Since we don't have a specific match ID here, we skip the service call
            // or could implement a different method in the service.
            // For now, this is a no-op as the proper flow is through onMatchCompleted.
        }
    }

    /**
     * This is the primary method for this observer.
     * When a match within the tournament is completed, this method is called.
     * It then tells the TournamentService to process the result of that match.
     *
     * @param completedMatchId The ID of the match that just finished.
     */
    @Override
    public void onMatchCompleted(String completedMatchId) {
        if (tournamentService != null && this.tournamentId != null) {
            // A match has finished.
            // Tell the TournamentService to update the standings for
            // this tournament based on the result of the completed match.
            // The service will handle the logic for tables or brackets.
            tournamentService.updateStandings(this.tournamentId, completedMatchId, 
                new ITournamentService.TournamentCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        // Standings updated successfully
                    }

                    @Override
                    public void onError(Exception e) {
                        // Handle error (could log or notify)
                        System.err.println("Failed to update standings: " + e.getMessage());
                    }
                });
        }
    }
}