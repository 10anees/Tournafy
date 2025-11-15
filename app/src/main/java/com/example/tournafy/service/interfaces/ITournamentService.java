package com.tournafy.service.interfaces;

// Note: Imports will be valid once domain models are created.
// import com.tournafy.domain.models.tournament.Tournament;
// import com.tournafy.domain.models.team.Team;
import java.util.List;

/**
 * Defines the contract for tournament-specific operations,
 * such as bracket generation and advancing teams. 
 */
public interface ITournamentService {

    /**
     * Generates the initial matches/brackets for a tournament.
     * This will use a Strategy Pattern (Random, Seeded, Manual).
     *
     * @param tournament The tournament to generate matches for.
     * @param teams      The list of teams participating.
     */
    void generateBrackets(Tournament tournament, List<Team> teams);

    /**
     * Advances a team to the next stage after a win.
     * @param tournament The tournament.
     * @param winningTeam The team that won the match.
     * @param matchId    The ID of the match that was completed.
     */
    void advanceTeam(Tournament tournament, Team winningTeam, String matchId);

    /**
     * Updates the tournament table (for Round Robin) after a match.
     * @param tournament The tournament.
     * @param matchId    The ID of the match that was completed.
     */
    void updateTable(Tournament tournament, String matchId);
}