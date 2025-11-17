package com.example.tournafy.service.strategies.tournament;

import com.example.tournafy.domain.models.team.TournamentTeam;
import com.example.tournafy.domain.models.tournament.TournamentMatch;
import java.util.List;

/**
 * Strategy Interface for generating tournament brackets.
 * Implementations: RandomBracketStrategy, SeededBracketStrategy, ManualBracketStrategy.
 */
public interface IBracketGenerationStrategy {
    /**
     * Generates a list of matches based on the provided teams.
     * @param teams The list of teams participating in the tournament.
     * @return A list of generated TournamentMatch objects.
     */
    List<TournamentMatch> generate(List<TournamentTeam> teams);
}