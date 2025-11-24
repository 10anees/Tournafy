package com.example.tournafy.service.strategies.tournament;

import com.example.tournafy.domain.models.team.TournamentTeam;
import com.example.tournafy.domain.models.tournament.TournamentMatch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Seeded bracket generation strategy.
 * Pairs teams based on their seeding/ranking (top seed vs bottom seed).
 * Supports any number of teams (not just powers of 2) - higher seeds get byes.
 * Assumes teams are already sorted by points/standings or have seed numbers.
 */
public class SeededBracketStrategy implements IBracketGenerationStrategy {

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
        
        // Sort teams by points (descending) and NRR (descending) for seeding
        List<TournamentTeam> sortedTeams = new ArrayList<>(teams);
        sortedTeams.sort(new Comparator<TournamentTeam>() {
            @Override
            public int compare(TournamentTeam t1, TournamentTeam t2) {
                // First compare by points
                int pointsCompare = Integer.compare(t2.getPoints(), t1.getPoints());
                if (pointsCompare != 0) {
                    return pointsCompare;
                }
                // Then by NRR/Goal Difference
                return Float.compare(t2.getNetRunRate(), t1.getNetRunRate());
            }
        });

        // Calculate number of matches needed for first round
        // For n teams: if n is power of 2, n/2 matches
        // Otherwise: higher seeds get byes
        int teamsCount = sortedTeams.size();
        int nextPowerOf2 = getNextPowerOf2(teamsCount);
        int byesNeeded = nextPowerOf2 - teamsCount;
        int firstRoundMatches = (teamsCount - byesNeeded) / 2;
        
        // Top seeds (first byesNeeded teams) get byes
        // Remaining teams play in first round
        // Pair them: first playing team vs last, second vs second-last, etc.
        int matchOrder = 1;
        List<TournamentTeam> playingTeams = sortedTeams.subList(byesNeeded, teamsCount);
        int n = playingTeams.size();
        
        for (int i = 0; i < n / 2; i++) {
            TournamentTeam higherSeed = playingTeams.get(i);
            TournamentTeam lowerSeed = playingTeams.get(n - 1 - i);

            TournamentMatch match = new TournamentMatch();
            match.setMatchOrder(matchOrder++);
            // Note: tournamentId, stageId, and matchId will be set by service layer
            // Store team IDs in match for later match creation
            
            matches.add(match);
        }
        
        // Top seeds (first byesNeeded teams) automatically advance to next round
        // This can be handled at the service layer

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
