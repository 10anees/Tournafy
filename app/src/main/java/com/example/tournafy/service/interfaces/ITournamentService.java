package com.example.tournafy.service.interfaces;

import com.example.tournafy.domain.models.tournament.Tournament;
import com.example.tournafy.domain.models.team.TournamentTeam;
import com.example.tournafy.service.strategies.tournament.IBracketGenerationStrategy;
import java.util.List;

/**
 * Service interface for tournament-specific operations. 
 * This includes logic for bracket generation (using Strategy Pattern)
 * and updating tournament tables/standings.
 */
public interface ITournamentService {

    /**
     * Generates the initial match brackets for a tournament using a
     * selected strategy. 
     *
     * @param tournament The tournament to generate brackets for.
     * @param strategy   The IBracketGenerationStrategy to use (e.g., Random, Seeded).
     * @param callback   Callback to return the updated tournament or an error.
     */
    void generateBrackets(Tournament tournament, IBracketGenerationStrategy strategy, TournamentCallback<Tournament> callback);

    /**
     * Updates the tournament standings (e.g., points table) after a match is completed.
     *
     * @param tournamentId The ID of the tournament to update.
     * @param completedMatchId The ID of the match that was just completed.
     * @param callback   Callback to signal success or error.
     */
    void updateStandings(String tournamentId, String completedMatchId, TournamentCallback<Void> callback);

    /**
     * Gets the list of teams participating in the tournament, often with their stats.
     *
     * @param tournamentId The ID of the tournament.
     * @param callback   Callback to return the list of TournamentTeams or an error.
     */
    void getTournamentTeams(String tournamentId, TournamentCallback<List<TournamentTeam>> callback);

    /**
     * Advances winning teams to the next stage in a knockout tournament.
     *
     * @param tournamentId The ID of the tournament.
     * @param stageId      The ID of the stage that was just completed.
     * @param callback   Callback to signal success or error.
     */
    void advanceKnockoutTeams(String tournamentId, String stageId, TournamentCallback<Void> callback);


    /**
     * A generic callback interface for tournament operations.
     *
     * @param <T> The type of the successful result.
     */
    interface TournamentCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }
}