package com.example.tournafy.service.strategies.tournament;

import com.example.tournafy.domain.models.tournament.Tournament;
import com.example.tournafy.domain.models.team.Team;
import java.util.List;

/**
 * Defines the contract for different algorithms used to
 * set up tournament matches (e.g., Random, Manual).
 */
public interface IBracketGenerationStrategy {

    /**
     * Generates the match schedule for a given tournament stage.
     *
     * @param tournament The tournament to generate matches for.
     * @param teams      The list of teams in that stage.
     */
    void generateMatches(Tournament tournament, List<com.example.tournafy.domain.models.team.Team> teams);
}