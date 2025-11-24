package com.example.tournafy.service.strategies.tournament;

import com.example.tournafy.domain.models.team.TournamentTeam;
import com.example.tournafy.domain.models.tournament.TournamentMatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Random bracket generation strategy.
 * Randomly pairs teams for knockout matches.
 * Supports any number of teams (not just powers of 2) by giving byes to teams.
 */
public class RandomBracketStrategy implements IBracketGenerationStrategy {

    @Override
    public List<TournamentMatch> generate(List<TournamentTeam> teams) {
        if (teams == null || teams.isEmpty()) {
            return new ArrayList<>();
        }

        // Minimum 2 teams required
        if (teams.size() < 2) {
            return new ArrayList<>();
        }

        List<TournamentMatch> matches = new ArrayList<>();
        
        // Create a shuffled copy of teams
        List<TournamentTeam> shuffledTeams = new ArrayList<>(teams);
        Collections.shuffle(shuffledTeams);
        
        // Calculate number of matches needed for first round
        // For n teams: if n is power of 2, n/2 matches
        // Otherwise: calculate byes needed
        int teamsCount = shuffledTeams.size();
        int nextPowerOf2 = getNextPowerOf2(teamsCount);
        int byesNeeded = nextPowerOf2 - teamsCount;
        int firstRoundMatches = (teamsCount - byesNeeded) / 2;
        
        // Pair teams randomly for first round matches
        int matchOrder = 1;
        int teamIndex = 0;
        
        // Create matches for teams that play in first round
        for (int i = 0; i < firstRoundMatches; i++) {
            TournamentTeam team1 = shuffledTeams.get(teamIndex++);
            TournamentTeam team2 = shuffledTeams.get(teamIndex++);

            TournamentMatch match = new TournamentMatch();
            match.setMatchOrder(matchOrder++);
            // Note: tournamentId and stageId will be set by the service layer
            // matchId will be set when actual match is created
            // Store team IDs in match for later match creation
            
            matches.add(match);
        }
        
        // Remaining teams get byes (automatically advance to next round)
        // These can be handled at the service layer by creating placeholder matches
        // or by tracking which teams advance automatically

        return matches;
    }
    
    /**
     * Calculate the next power of 2 greater than or equal to n
     */
    private int getNextPowerOf2(int n) {
        if (n <= 1) return 1;
        int power = 1;
        while (power < n) {
            power *= 2;
        }
        return power;
    }
}
