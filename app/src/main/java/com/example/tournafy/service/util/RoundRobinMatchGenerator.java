package com.example.tournafy.service.util;

import com.example.tournafy.domain.models.team.TournamentTeam;
import com.example.tournafy.domain.models.tournament.TournamentMatch;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to generate round-robin match fixtures.
 * Generates all possible pairings where each team plays every other team once.
 */
public class RoundRobinMatchGenerator {

    /**
     * Generate all round-robin matches for given teams
     * @param teams List of tournament teams
     * @return List of tournament match placeholders with match order
     */
    public static List<TournamentMatch> generateMatches(List<TournamentTeam> teams) {
        if (teams == null || teams.size() < 2) {
            return new ArrayList<>();
        }

        List<TournamentMatch> matches = new ArrayList<>();
        int matchOrder = 1;

        // Generate all unique pairings
        for (int i = 0; i < teams.size(); i++) {
            for (int j = i + 1; j < teams.size(); j++) {
                TournamentTeam team1 = teams.get(i);
                TournamentTeam team2 = teams.get(j);

                TournamentMatch match = new TournamentMatch();
                match.setMatchOrder(matchOrder++);
                // tournamentId, stageId, matchId will be set by service layer
                
                matches.add(match);
            }
        }

        return matches;
    }

    /**
     * Generate round-robin matches with home/away (double round-robin)
     * Each team plays every other team twice (once home, once away)
     */
    public static List<TournamentMatch> generateDoubleRoundRobinMatches(List<TournamentTeam> teams) {
        if (teams == null || teams.size() < 2) {
            return new ArrayList<>();
        }

        List<TournamentMatch> matches = new ArrayList<>();
        int matchOrder = 1;

        // First round: each pairing once
        for (int i = 0; i < teams.size(); i++) {
            for (int j = i + 1; j < teams.size(); j++) {
                TournamentMatch match = new TournamentMatch();
                match.setMatchOrder(matchOrder++);
                matches.add(match);
            }
        }

        // Second round: reverse pairings
        for (int i = 0; i < teams.size(); i++) {
            for (int j = i + 1; j < teams.size(); j++) {
                TournamentMatch match = new TournamentMatch();
                match.setMatchOrder(matchOrder++);
                matches.add(match);
            }
        }

        return matches;
    }

    /**
     * Generate round-robin matches divided into groups
     * @param teams All tournament teams
     * @param groupCount Number of groups to divide teams into
     * @return List of matches with group assignments
     */
    public static List<TournamentMatch> generateGroupMatches(List<TournamentTeam> teams, int groupCount) {
        if (teams == null || teams.size() < groupCount * 2) {
            return new ArrayList<>();
        }

        List<TournamentMatch> allMatches = new ArrayList<>();
        int teamsPerGroup = teams.size() / groupCount;
        int matchOrder = 1;

        // Divide teams into groups and generate round-robin for each group
        for (int g = 0; g < groupCount; g++) {
            int startIdx = g * teamsPerGroup;
            int endIdx = (g == groupCount - 1) ? teams.size() : startIdx + teamsPerGroup;
            
            List<TournamentTeam> groupTeams = teams.subList(startIdx, endIdx);
            
            // Generate round-robin for this group
            for (int i = 0; i < groupTeams.size(); i++) {
                for (int j = i + 1; j < groupTeams.size(); j++) {
                    TournamentMatch match = new TournamentMatch();
                    match.setMatchOrder(matchOrder++);
                    allMatches.add(match);
                }
            }
        }

        return allMatches;
    }

    /**
     * Calculate total number of matches for round-robin
     */
    public static int calculateTotalMatches(int teamCount) {
        if (teamCount < 2) return 0;
        // Formula: n*(n-1)/2 for single round-robin
        return (teamCount * (teamCount - 1)) / 2;
    }

    /**
     * Calculate total number of matches for double round-robin
     */
    public static int calculateDoubleRoundRobinMatches(int teamCount) {
        if (teamCount < 2) return 0;
        // Formula: n*(n-1) for double round-robin
        return teamCount * (teamCount - 1);
    }
}
