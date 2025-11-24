package com.example.tournafy.service.impl;

import com.example.tournafy.data.repository.offline.MatchFirestoreRepository;
import com.example.tournafy.data.repository.offline.TeamFirestoreRepository;
import com.example.tournafy.data.repository.offline.TournamentFirestoreRepository;
import com.example.tournafy.data.repository.offline.TournamentTeamFirestoreRepository;
import com.example.tournafy.data.repository.offline.TournamentStageFirestoreRepository;
import com.example.tournafy.data.repository.offline.TournamentMatchFirestoreRepository;
import com.example.tournafy.domain.models.base.Match;
import com.example.tournafy.domain.models.match.cricket.CricketMatch;
import com.example.tournafy.domain.models.match.cricket.CricketMatchConfig;
import com.example.tournafy.domain.models.match.football.FootballMatch;
import com.example.tournafy.domain.models.match.football.FootballMatchConfig;
import com.example.tournafy.domain.models.team.Team;
import com.example.tournafy.domain.models.team.TournamentTeam;
import com.example.tournafy.domain.models.tournament.Tournament;
import com.example.tournafy.domain.models.tournament.TournamentMatch;
import com.example.tournafy.domain.models.tournament.TournamentStage;
import com.example.tournafy.domain.enums.StageType;
import com.example.tournafy.domain.enums.EntityStatus;
import com.example.tournafy.service.interfaces.ITournamentService;
import com.example.tournafy.service.strategies.tournament.IBracketGenerationStrategy;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TournamentService implements ITournamentService {

    private final TournamentFirestoreRepository tournamentRepository;
    private final MatchFirestoreRepository matchRepository;
    private final TournamentTeamFirestoreRepository tournamentTeamRepository;
    private final TournamentStageFirestoreRepository tournamentStageRepository;
    private final TournamentMatchFirestoreRepository tournamentMatchRepository;
    private final TeamFirestoreRepository teamRepository;

    @Inject
    public TournamentService(
            TournamentFirestoreRepository tournamentRepository,
            MatchFirestoreRepository matchRepository,
            TournamentTeamFirestoreRepository tournamentTeamRepository,
            TournamentStageFirestoreRepository tournamentStageRepository,
            TournamentMatchFirestoreRepository tournamentMatchRepository,
            TeamFirestoreRepository teamRepository) {
        this.tournamentRepository = tournamentRepository;
        this.matchRepository = matchRepository;
        this.tournamentTeamRepository = tournamentTeamRepository;
        this.tournamentStageRepository = tournamentStageRepository;
        this.tournamentMatchRepository = tournamentMatchRepository;
        this.teamRepository = teamRepository;
    }

    @Override
    public void generateBrackets(Tournament tournament, IBracketGenerationStrategy strategy, TournamentCallback<Tournament> callback) {
        if (tournament == null) {
            callback.onError(new Exception("Tournament is null"));
            return;
        }
        
        String tournamentId = tournament.getEntityId();
        
        // Step 1: Get all teams for the tournament
        tournamentTeamRepository.getAllForTournament(tournamentId)
            .observeForever(new androidx.lifecycle.Observer<List<TournamentTeam>>() {
                @Override
                public void onChanged(List<TournamentTeam> teams) {
                    if (teams == null || teams.isEmpty()) {
                        callback.onError(new Exception("No teams found for bracket generation"));
                        tournamentTeamRepository.getAllForTournament(tournamentId).removeObserver(this);
                        return;
                    }
                    
                    try {
                        // Step 2: Use strategy to generate match structure
                        List<TournamentMatch> matchStructures = strategy.generate(teams);
                        
                        if (matchStructures.isEmpty()) {
                            callback.onError(new Exception("Strategy generated no matches"));
                            tournamentTeamRepository.getAllForTournament(tournamentId).removeObserver(this);
                            return;
                        }
                        
                        // Step 3: Create or get the knockout stage
                        createKnockoutStage(tournamentId, teams, matchStructures, tournament.getSportType(), callback);
                        
                    } catch (Exception e) {
                        callback.onError(e);
                    }
                    
                    tournamentTeamRepository.getAllForTournament(tournamentId).removeObserver(this);
                }
            });
    }
    
    /**
     * Create knockout stage and generate matches for it
     */
    private void createKnockoutStage(String tournamentId, List<TournamentTeam> teams, 
                                     List<TournamentMatch> matchStructures, String sportType,
                                     TournamentCallback<Tournament> callback) {
        
        // Determine stage type based on number of teams
        String stageName;
        StageType stageType;
        int teamsCount = teams.size();
        
        if (teamsCount <= 2) {
            stageName = "Final";
            stageType = StageType.FINAL;
        } else if (teamsCount <= 4) {
            stageName = "Semi Finals";
            stageType = StageType.SEMI_FINAL;
        } else if (teamsCount <= 8) {
            stageName = "Quarter Finals";
            stageType = StageType.QUARTER_FINAL;
        } else {
            stageName = "Round of 16";
            stageType = StageType.ROUND_OF_16;
        }
        
        // Create stage
        TournamentStage stage = new TournamentStage();
        stage.setTournamentId(tournamentId);
        stage.setStageName(stageName);
        stage.setStageType(stageType.name());
        stage.setStageOrder(1);
        stage.setCompleted(false);
        
        // Save stage first
        tournamentStageRepository.add(tournamentId, stage)
            .addOnSuccessListener(aVoid -> {
                String stageId = stage.getStageId();
                
                // Now create actual matches for this stage
                createMatchesFromStructure(tournamentId, stageId, teams, matchStructures, sportType, callback);
            })
            .addOnFailureListener(callback::onError);
    }
    
    /**
     * Create actual Match entities from TournamentMatch structures
     */
    private void createMatchesFromStructure(String tournamentId, String stageId, 
                                            List<TournamentTeam> teams,
                                            List<TournamentMatch> matchStructures, 
                                            String sportType,
                                            TournamentCallback<Tournament> callback) {
        
        final int totalMatches = matchStructures.size();
        final int[] createdCount = {0};
        final boolean[] hasError = {false};
        
        // Step 1: Load all Team entities with players
        final java.util.Map<String, Team> teamMap = new java.util.HashMap<>();
        final int[] teamsLoaded = {0};
        final int totalTeamsToLoad = teams.size();
        
        for (TournamentTeam tournamentTeam : teams) {
            teamRepository.getById(tournamentTeam.getTeamId())
                .observeForever(new androidx.lifecycle.Observer<Team>() {
                    @Override
                    public void onChanged(Team team) {
                        if (team != null) {
                            teamMap.put(team.getTeamId(), team);
                        }
                        
                        teamsLoaded[0]++;
                        
                        // Once all teams loaded, create matches
                        if (teamsLoaded[0] == totalTeamsToLoad) {
                            createMatchesWithTeamMap(tournamentId, stageId, teams, teamMap, 
                                                     matchStructures, sportType, callback);
                        }
                        
                        // Remove observer after first load
                        teamRepository.getById(tournamentTeam.getTeamId()).removeObserver(this);
                    }
                });
        }
    }
    
    /**
     * Create matches after teams are loaded
     */
    private void createMatchesWithTeamMap(String tournamentId, String stageId,
                                          List<TournamentTeam> teams,
                                          java.util.Map<String, Team> teamMap,
                                          List<TournamentMatch> matchStructures,
                                          String sportType,
                                          TournamentCallback<Tournament> callback) {
        
        final int totalMatches = matchStructures.size();
        final int[] createdCount = {0};
        final boolean[] hasError = {false};
        
        // Calculate byes
        int nextPowerOf2 = getNextPowerOf2(teams.size());
        int byesNeeded = nextPowerOf2 - teams.size();
        int matchIndex = 0;
        
        // Teams that get byes (first N teams)
        List<TournamentTeam> byeTeams = teams.subList(0, Math.min(byesNeeded, teams.size()));
        // Teams that play in first round
        List<TournamentTeam> playingTeams = teams.subList(Math.min(byesNeeded, teams.size()), teams.size());
        
        // Create matches for playing teams
        for (int i = 0; i < playingTeams.size() && i + 1 < playingTeams.size(); i += 2) {
            final int currentMatchOrder = matchIndex + 1;
            TournamentTeam team1 = playingTeams.get(i);
            TournamentTeam team2 = playingTeams.get(i + 1);
            
            // Create actual Match entity with players
            Match match = createMatchForTournament(team1, team2, sportType, tournamentId, teamMap);
            
            // Save match to Firestore
            matchRepository.add(match)
                .addOnSuccessListener(matchVoid -> {
                    String matchId = match.getEntityId();
                    
                    // Create TournamentMatch link
                    TournamentMatch tournamentMatch = new TournamentMatch(
                        tournamentId, stageId, matchId, currentMatchOrder
                    );
                    
                    tournamentMatchRepository.add(tournamentId, tournamentMatch)
                        .addOnSuccessListener(tmVoid -> {
                            synchronized (createdCount) {
                                createdCount[0]++;
                                
                                // Check if all matches created
                                if (createdCount[0] == totalMatches && !hasError[0]) {
                                    // Reload tournament and return
                                    loadAndReturnTournament(tournamentId, callback);
                                }
                            }
                        })
                        .addOnFailureListener(e -> {
                            synchronized (hasError) {
                                if (!hasError[0]) {
                                    hasError[0] = true;
                                    callback.onError(e);
                                }
                            }
                        });
                })
                .addOnFailureListener(e -> {
                    synchronized (hasError) {
                        if (!hasError[0]) {
                            hasError[0] = true;
                            callback.onError(e);
                        }
                    }
                });
            
            matchIndex++;
        }
        
        // Handle case where no matches needed (error)
        if (totalMatches == 0) {
            callback.onError(new Exception("No matches could be created"));
        }
    }
    
    /**
     * Create a Match entity for tournament bracket
     */
    private Match createMatchForTournament(TournamentTeam team1, TournamentTeam team2, 
                                           String sportType, String tournamentId,
                                           java.util.Map<String, Team> teamMap) {
        Match match;
        
        if ("CRICKET".equalsIgnoreCase(sportType)) {
            CricketMatch cricketMatch = new CricketMatch();
            cricketMatch.setName(team1.getTeamName() + " vs " + team2.getTeamName());
            cricketMatch.setMatchFormat("T20");
            
            // NOTE: Do NOT set matchConfig or toss here
            // Host will set these via AddMatchDetailsFragment when starting the match
            // This ensures proper configuration flow for tournament matches
            
            match = cricketMatch;
        } else {
            FootballMatch footballMatch = new FootballMatch();
            footballMatch.setName(team1.getTeamName() + " vs " + team2.getTeamName());
            footballMatch.setMatchFormat("90min");
            
            // NOTE: Do NOT set matchConfig here
            // Host will set these via AddMatchDetailsFragment when starting the match
            
            match = footballMatch;
        }
        
        match.setTournamentId(tournamentId);
        match.setStatus(EntityStatus.SCHEDULED.name());
        match.setMatchDate(new Date());
        match.setHostUserId("TOURNAMENT_HOST"); // Will be set by actual tournament host
        
        // Create MatchTeam objects for both teams with players
        java.util.List<com.example.tournafy.domain.models.team.MatchTeam> matchTeams = new java.util.ArrayList<>();
        
        com.example.tournafy.domain.models.team.MatchTeam matchTeam1 = new com.example.tournafy.domain.models.team.MatchTeam();
        matchTeam1.setTeamId(team1.getTeamId());
        matchTeam1.setTeamName(team1.getTeamName());
        matchTeam1.setHomeTeam(true); // First team is home team
        // Load players from Team entity
        Team fullTeam1 = teamMap.get(team1.getTeamId());
        if (fullTeam1 != null && fullTeam1.getPlayers() != null) {
            matchTeam1.setPlayers(new java.util.ArrayList<>(fullTeam1.getPlayers()));
        }
        matchTeams.add(matchTeam1);
        
        com.example.tournafy.domain.models.team.MatchTeam matchTeam2 = new com.example.tournafy.domain.models.team.MatchTeam();
        matchTeam2.setTeamId(team2.getTeamId());
        matchTeam2.setTeamName(team2.getTeamName());
        matchTeam2.setHomeTeam(false); // Second team is away team
        // Load players from Team entity
        Team fullTeam2 = teamMap.get(team2.getTeamId());
        if (fullTeam2 != null && fullTeam2.getPlayers() != null) {
            matchTeam2.setPlayers(new java.util.ArrayList<>(fullTeam2.getPlayers()));
        }
        matchTeams.add(matchTeam2);
        
        // Set teams for the match
        if (match instanceof CricketMatch) {
            ((CricketMatch) match).setTeams(matchTeams);
        } else if (match instanceof FootballMatch) {
            ((FootballMatch) match).setTeams(matchTeams);
        }
        
        return match;
    }
    
    /**
     * Load tournament and return via callback
     */
    private void loadAndReturnTournament(String tournamentId, TournamentCallback<Tournament> callback) {
        tournamentRepository.getById(tournamentId)
            .observeForever(new androidx.lifecycle.Observer<Tournament>() {
                @Override
                public void onChanged(Tournament tournament) {
                    if (tournament != null) {
                        callback.onSuccess(tournament);
                    } else {
                        callback.onError(new Exception("Failed to reload tournament"));
                    }
                    tournamentRepository.getById(tournamentId).removeObserver(this);
                }
            });
    }
    
    /**
     * Calculate next power of 2 for bracket sizing
     */
    private int getNextPowerOf2(int n) {
        if (n <= 1) return 1;
        int power = 1;
        while (power < n) {
            power *= 2;
        }
        return power;
    }

    @Override
    public void updateStandings(String tournamentId, String completedMatchId, TournamentCallback<Void> callback) {
        // Implementation placeholder
        System.out.println("Updating standings for tournament: " + tournamentId);
        callback.onSuccess(null);
    }

    @Override
    public void getTournamentTeams(String tournamentId, TournamentCallback<List<TournamentTeam>> callback) {
        // Load teams from TournamentTeam subcollection
        androidx.lifecycle.Observer<List<TournamentTeam>> teamsObserver = new androidx.lifecycle.Observer<List<TournamentTeam>>() {
            @Override
            public void onChanged(List<TournamentTeam> teams) {
                if (teams != null) {
                    callback.onSuccess(teams);
                } else {
                    callback.onSuccess(new java.util.ArrayList<>());
                }
                // Remove observer after execution to prevent memory leaks
                tournamentTeamRepository.getAllForTournament(tournamentId).removeObserver(this);
            }
        };
        tournamentTeamRepository.getAllForTournament(tournamentId).observeForever(teamsObserver);
    }

    @Override
    public void advanceKnockoutTeams(String tournamentId, String stageId, TournamentCallback<Void> callback) {
        // Implementation placeholder
        System.out.println("Advancing teams for stage: " + stageId);
        callback.onSuccess(null);
    }

    /**
     * Start a tournament (DRAFT → ACTIVE)
     * Validates that tournament has teams and stages before starting
     */
    public void startTournament(String tournamentId, TournamentCallback<Void> callback) {
        // Get tournament
        androidx.lifecycle.Observer<Tournament> observer = new androidx.lifecycle.Observer<Tournament>() {
            @Override
            public void onChanged(Tournament tournament) {
                if (tournament == null) {
                    callback.onError(new Exception("Tournament not found"));
                    tournamentRepository.getById(tournamentId).removeObserver(this);
                    return;
                }

                // Validate tournament can start
                if (!EntityStatus.DRAFT.name().equals(tournament.getStatus())) {
                    callback.onError(new Exception("Tournament must be in DRAFT status to start"));
                    tournamentRepository.getById(tournamentId).removeObserver(this);
                    return;
                }

                // Check if teams exist
                tournamentTeamRepository.getAllForTournament(tournamentId)
                        .observeForever(new androidx.lifecycle.Observer<List<TournamentTeam>>() {
                            @Override
                            public void onChanged(List<TournamentTeam> teams) {
                                if (teams == null || teams.isEmpty()) {
                                    callback.onError(new Exception("Cannot start tournament without teams"));
                                    tournamentTeamRepository.getAllForTournament(tournamentId).removeObserver(this);
                                    return;
                                }

                                // Update tournament status to IN_PROGRESS
                                tournament.setStatus(EntityStatus.IN_PROGRESS.name());
                                tournamentRepository.update(tournament)
                                        .addOnSuccessListener(aVoid -> {
                                            callback.onSuccess(null);
                                            tournamentTeamRepository.getAllForTournament(tournamentId).removeObserver(this);
                                        })
                                        .addOnFailureListener(e -> {
                                            callback.onError(e);
                                            tournamentTeamRepository.getAllForTournament(tournamentId).removeObserver(this);
                                        });
                            }
                        });

                tournamentRepository.getById(tournamentId).removeObserver(this);
            }
        };
        tournamentRepository.getById(tournamentId).observeForever(observer);
    }

    /**
     * Complete a tournament (ACTIVE → COMPLETED)
     * Sets the winner team ID
     */
    public void completeTournament(String tournamentId, String winnerTeamId, TournamentCallback<Void> callback) {
        androidx.lifecycle.Observer<Tournament> observer = new androidx.lifecycle.Observer<Tournament>() {
            @Override
            public void onChanged(Tournament tournament) {
                if (tournament == null) {
                    callback.onError(new Exception("Tournament not found"));
                    tournamentRepository.getById(tournamentId).removeObserver(this);
                    return;
                }

                tournament.setStatus(EntityStatus.COMPLETED.name());
                tournament.setWinnerTeamId(winnerTeamId);

                tournamentRepository.update(tournament)
                        .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                        .addOnFailureListener(callback::onError);

                tournamentRepository.getById(tournamentId).removeObserver(this);
            }
        };
        tournamentRepository.getById(tournamentId).observeForever(observer);
    }

    /**
     * Create initial stages for a tournament based on tournament type
     */
    public void createInitialStages(String tournamentId, String tournamentType, TournamentCallback<Void> callback) {
        List<TournamentStage> stages = new ArrayList<>();

        switch (tournamentType.toUpperCase()) {
            case "KNOCKOUT":
                // Create knockout stages: ROUND_OF_16, QUARTER, SEMI, FINAL
                stages.add(createStage(tournamentId, "Round of 16", StageType.ROUND_OF_16.name(), 1));
                stages.add(createStage(tournamentId, "Quarter Finals", StageType.QUARTER_FINAL.name(), 2));
                stages.add(createStage(tournamentId, "Semi Finals", StageType.SEMI_FINAL.name(), 3));
                stages.add(createStage(tournamentId, "Final", StageType.FINAL.name(), 4));
                break;

            case "LEAGUE":
            case "ROUND ROBIN":
                // Create single group stage
                stages.add(createStage(tournamentId, "Group Stage", StageType.GROUP.name(), 1));
                break;

            case "MIXED":
                // Create group stage followed by knockout
                stages.add(createStage(tournamentId, "Group Stage", StageType.GROUP.name(), 1));
                stages.add(createStage(tournamentId, "Quarter Finals", StageType.QUARTER_FINAL.name(), 2));
                stages.add(createStage(tournamentId, "Semi Finals", StageType.SEMI_FINAL.name(), 3));
                stages.add(createStage(tournamentId, "Final", StageType.FINAL.name(), 4));
                break;

            default:
                callback.onError(new Exception("Unknown tournament type: " + tournamentType));
                return;
        }

        // Batch add stages
        tournamentStageRepository.addStages(tournamentId, stages)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }

    /**
     * Helper to create a tournament stage
     */
    private TournamentStage createStage(String tournamentId, String name, String type, int order) {
        TournamentStage stage = new TournamentStage();
        stage.setTournamentId(tournamentId);
        stage.setStageName(name);
        stage.setStageType(type);
        stage.setStageOrder(order);
        stage.setCompleted(false);
        return stage;
    }

    /**
     * Check if a stage is completed (all matches finished)
     */
    public void checkStageCompletion(String tournamentId, String stageId, TournamentCallback<Boolean> callback) {
        tournamentMatchRepository.getMatchesByStage(tournamentId, stageId)
                .observeForever(new androidx.lifecycle.Observer<List<TournamentMatch>>() {
                    @Override
                    public void onChanged(List<TournamentMatch> tournamentMatches) {
                        if (tournamentMatches == null || tournamentMatches.isEmpty()) {
                            callback.onSuccess(false);
                            tournamentMatchRepository.getMatchesByStage(tournamentId, stageId).removeObserver(this);
                            return;
                        }

                        // Check each match status
                        boolean allCompleted = true;
                        for (TournamentMatch tm : tournamentMatches) {
                            // Get actual match and check status
                            // For now, we'll assume it's a simple check
                            // In reality, you'd need to fetch each match and check its status
                        }

                        callback.onSuccess(allCompleted);
                        tournamentMatchRepository.getMatchesByStage(tournamentId, stageId).removeObserver(this);
                    }
                });
    }

    /**
     * Add teams to tournament
     */
    public void addTeamsToTournament(String tournamentId, List<TournamentTeam> teams, TournamentCallback<Void> callback) {
        // Initialize stats for each team
        for (TournamentTeam team : teams) {
            team.setTournamentId(tournamentId);
            team.setPoints(0);
            team.setMatchesPlayed(0);
            team.setMatchesWon(0);
            team.setMatchesLost(0);
            team.setMatchesDrawn(0);
            team.setGoalsFor(0);
            team.setGoalsAgainst(0);
            team.setNetRunRate(0.0f);
        }

        tournamentTeamRepository.addTeams(tournamentId, teams)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }

    /**
     * PHASE 9: Create Match entities from TournamentMatch links for a stage
     * Generates actual Match documents and links them to the tournament
     */
    public void createMatchesForStage(String tournamentId, String stageId, StageType stageType, 
                                      List<TournamentTeam> teams, String sportType, 
                                      TournamentCallback<List<String>> callback) {
        
        if (teams == null || teams.isEmpty()) {
            callback.onError(new Exception("No teams provided for match creation"));
            return;
        }

        List<String> createdMatchIds = new ArrayList<>();
        List<TournamentMatch> tournamentMatches = new ArrayList<>();
        
        try {
            // Determine match pairings based on stage type
            List<TeamPair> pairings = generateMatchPairings(teams, stageType);
            
            for (int i = 0; i < pairings.size(); i++) {
                TeamPair pair = pairings.get(i);
                final int currentMatchOrder = i + 1; // Make it final for lambda
                
                // Create the actual Match entity
                Match match = createMatchForTeams(pair.team1, pair.team2, sportType, tournamentId);
                
                // Save match to repository
                String matchId = java.util.UUID.randomUUID().toString();
                match.setEntityId(matchId);
                
                matchRepository.add(match)
                    .addOnSuccessListener(aVoid -> {
                        // Create TournamentMatch link
                        TournamentMatch tournamentMatch = new TournamentMatch(
                            tournamentId, stageId, matchId, currentMatchOrder
                        );
                        
                        tournamentMatchRepository.add(tournamentId, tournamentMatch)
                            .addOnSuccessListener(tmVoid -> {
                                createdMatchIds.add(matchId);
                                
                                // If all matches created, return success
                                if (createdMatchIds.size() == pairings.size()) {
                                    callback.onSuccess(createdMatchIds);
                                }
                            })
                            .addOnFailureListener(callback::onError);
                    })
                    .addOnFailureListener(callback::onError);
            }
            
        } catch (Exception e) {
            callback.onError(e);
        }
    }

    /**
     * Helper: Generate team pairings for matches based on stage type
     */
    private List<TeamPair> generateMatchPairings(List<TournamentTeam> teams, StageType stageType) {
        List<TeamPair> pairings = new ArrayList<>();
        
        // Sort teams by current standings for seeded brackets
        List<TournamentTeam> sortedTeams = new ArrayList<>(teams);
        sortedTeams.sort((t1, t2) -> {
            // Sort by points descending, then by NRR/GD
            int pointsCompare = Integer.compare(t2.getPoints(), t1.getPoints());
            if (pointsCompare != 0) return pointsCompare;
            return Float.compare(t2.getNetRunRate(), t1.getNetRunRate());
        });
        
        // Generate pairings based on stage requirements
        switch (stageType) {
            case QUARTER_FINAL:
                // Need 8 teams for quarters
                if (sortedTeams.size() >= 8) {
                    pairings.add(new TeamPair(sortedTeams.get(0), sortedTeams.get(7))); // 1 vs 8
                    pairings.add(new TeamPair(sortedTeams.get(1), sortedTeams.get(6))); // 2 vs 7
                    pairings.add(new TeamPair(sortedTeams.get(2), sortedTeams.get(5))); // 3 vs 6
                    pairings.add(new TeamPair(sortedTeams.get(3), sortedTeams.get(4))); // 4 vs 5
                }
                break;
                
            case SEMI_FINAL:
                // Need 4 teams for semis
                if (sortedTeams.size() >= 4) {
                    pairings.add(new TeamPair(sortedTeams.get(0), sortedTeams.get(3))); // 1 vs 4
                    pairings.add(new TeamPair(sortedTeams.get(1), sortedTeams.get(2))); // 2 vs 3
                }
                break;
                
            case FINAL:
                // Need 2 teams for final
                if (sortedTeams.size() >= 2) {
                    pairings.add(new TeamPair(sortedTeams.get(0), sortedTeams.get(1)));
                }
                break;
                
            case GROUP:
                // Round robin - all teams play each other
                for (int i = 0; i < sortedTeams.size(); i++) {
                    for (int j = i + 1; j < sortedTeams.size(); j++) {
                        pairings.add(new TeamPair(sortedTeams.get(i), sortedTeams.get(j)));
                    }
                }
                break;
                
            default:
                break;
        }
        
        return pairings;
    }

    /**
     * Helper: Create a Match entity for two teams
     */
    private Match createMatchForTeams(TournamentTeam team1, TournamentTeam team2, 
                                     String sportType, String tournamentId) {
        Match match;
        
        if ("CRICKET".equalsIgnoreCase(sportType)) {
            CricketMatch cricketMatch = new CricketMatch();
            cricketMatch.setSportId("cricket");
            cricketMatch.setMatchFormat("T20"); // Default format
            match = cricketMatch;
        } else {
            FootballMatch footballMatch = new FootballMatch();
            footballMatch.setSportId("football");
            footballMatch.setMatchFormat("90min"); // Default format
            match = footballMatch;
        }
        
        // Set match properties
        match.setName(team1.getTeamName() + " vs " + team2.getTeamName());
        match.setTournamentId(tournamentId);
        match.setStatus(EntityStatus.SCHEDULED.name());
        match.setMatchDate(new Date());
        
        // TODO: Set team IDs once Match model supports it
        // match.setTeam1Id(team1.getTeamId());
        // match.setTeam2Id(team2.getTeamId());
        
        return match;
    }

    /**
     * Helper class for team pairings
     */
    private static class TeamPair {
        TournamentTeam team1;
        TournamentTeam team2;
        
        TeamPair(TournamentTeam team1, TournamentTeam team2) {
            this.team1 = team1;
            this.team2 = team2;
        }
    }

    /**
     * Check if stage is complete and advance to next stage if needed
     */
    public void checkAndAdvanceStage(String tournamentId, String completedStageId, 
                                    TournamentCallback<Void> callback) {
        
        // Check if all matches in stage are completed
        checkStageCompletion(tournamentId, completedStageId, new TournamentCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean isComplete) {
                if (!isComplete) {
                    callback.onSuccess(null);
                    return;
                }
                
                // Mark current stage as completed
                tournamentStageRepository.getById(tournamentId, completedStageId)
                    .observeForever(new androidx.lifecycle.Observer<TournamentStage>() {
                        @Override
                        public void onChanged(TournamentStage stage) {
                            if (stage == null) {
                                callback.onError(new Exception("Stage not found"));
                                return;
                            }
                            
                            stage.setCompleted(true);
                            tournamentStageRepository.update(tournamentId, stage)
                                .addOnSuccessListener(aVoid -> {
                                    // Find next stage
                                    findAndStartNextStage(tournamentId, stage.getStageOrder(), callback);
                                })
                                .addOnFailureListener(callback::onError);
                            
                            tournamentStageRepository.getById(tournamentId, completedStageId)
                                .removeObserver(this);
                        }
                    });
            }
            
            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    /**
     * Find and start the next stage in tournament progression
     */
    private void findAndStartNextStage(String tournamentId, int currentStageOrder, 
                                       TournamentCallback<Void> callback) {
        
        tournamentStageRepository.getAllForTournament(tournamentId)
            .observeForever(new androidx.lifecycle.Observer<List<TournamentStage>>() {
                @Override
                public void onChanged(List<TournamentStage> stages) {
                    if (stages == null || stages.isEmpty()) {
                        callback.onSuccess(null);
                        tournamentStageRepository.getAllForTournament(tournamentId).removeObserver(this);
                        return;
                    }
                    
                    // Find next stage by order
                    TournamentStage nextStage = null;
                    for (TournamentStage stage : stages) {
                        if (stage.getStageOrder() == currentStageOrder + 1) {
                            nextStage = stage;
                            break;
                        }
                    }
                    
                    if (nextStage == null) {
                        // No more stages - tournament should be completed
                        callback.onSuccess(null);
                        tournamentStageRepository.getAllForTournament(tournamentId).removeObserver(this);
                        return;
                    }
                    
                    // Get top teams from current stage to advance
                    advanceTeamsToNextStage(tournamentId, nextStage, callback);
                    tournamentStageRepository.getAllForTournament(tournamentId).removeObserver(this);
                }
            });
    }

    /**
     * Advance qualified teams to next knockout stage
     */
    private void advanceTeamsToNextStage(String tournamentId, TournamentStage nextStage, 
                                        TournamentCallback<Void> callback) {
        
        // Get tournament to determine sport type
        tournamentRepository.getById(tournamentId)
            .observeForever(new androidx.lifecycle.Observer<Tournament>() {
                @Override
                public void onChanged(Tournament tournament) {
                    if (tournament == null) {
                        callback.onError(new Exception("Tournament not found"));
                        tournamentRepository.getById(tournamentId).removeObserver(this);
                        return;
                    }
                    
                    // Get qualified teams (top N based on stage type)
                    tournamentTeamRepository.getAllForTournament(tournamentId)
                        .observeForever(new androidx.lifecycle.Observer<List<TournamentTeam>>() {
                            @Override
                            public void onChanged(List<TournamentTeam> allTeams) {
                                if (allTeams == null || allTeams.isEmpty()) {
                                    callback.onError(new Exception("No teams found"));
                                    tournamentTeamRepository.getAllForTournament(tournamentId)
                                        .removeObserver(this);
                                    return;
                                }
                                
                                // Get qualified teams based on next stage requirements
                                List<TournamentTeam> qualifiedTeams = getQualifiedTeams(
                                    allTeams, 
                                    StageType.valueOf(nextStage.getStageType())
                                );
                                
                                // Create matches for next stage
                                createMatchesForStage(
                                    tournamentId,
                                    nextStage.getStageId(),
                                    StageType.valueOf(nextStage.getStageType()),
                                    qualifiedTeams,
                                    tournament.getSportType(),
                                    new TournamentCallback<List<String>>() {
                                        @Override
                                        public void onSuccess(List<String> matchIds) {
                                            callback.onSuccess(null);
                                        }
                                        
                                        @Override
                                        public void onError(Exception e) {
                                            callback.onError(e);
                                        }
                                    }
                                );
                                
                                tournamentTeamRepository.getAllForTournament(tournamentId)
                                    .removeObserver(this);
                            }
                        });
                    
                    tournamentRepository.getById(tournamentId).removeObserver(this);
                }
            });
    }

    /**
     * Get qualified teams for next stage
     */
    private List<TournamentTeam> getQualifiedTeams(List<TournamentTeam> allTeams, StageType nextStageType) {
        // Sort teams by standings
        List<TournamentTeam> sorted = new ArrayList<>(allTeams);
        sorted.sort((t1, t2) -> {
            int pointsCompare = Integer.compare(t2.getPoints(), t1.getPoints());
            if (pointsCompare != 0) return pointsCompare;
            return Float.compare(t2.getNetRunRate(), t1.getNetRunRate());
        });
        
        // Determine how many teams qualify
        int qualifyCount;
        switch (nextStageType) {
            case FINAL:
                qualifyCount = 2;
                break;
            case SEMI_FINAL:
                qualifyCount = 4;
                break;
            case QUARTER_FINAL:
                qualifyCount = 8;
                break;
            default:
                qualifyCount = sorted.size();
                break;
        }
        
        return sorted.subList(0, Math.min(qualifyCount, sorted.size()));
    }

    /**
     * PHASE 10: Calculate and update standings after a match completes
     * Handles both cricket (NRR) and football (goal difference)
     */
    public void calculateAndUpdateStandings(String tournamentId, String matchId, Match match, TournamentCallback<Void> callback) {
        if (match == null) {
            callback.onError(new Exception("Match not found"));
            return;
        }

        // Determine sport type
        boolean isCricket = match instanceof CricketMatch;
        
        // Get winner and loser team IDs from match result
        String winnerTeamId = match.getWinnerTeamId();
        
        // TODO: Get team IDs from match (requires Match model update)
        // For now, we'll need to extract from match data
        
        // Get both teams involved in the match
        // String team1Id = match.getTeam1Id();
        // String team2Id = match.getTeam2Id();
        
        // Update stats for both teams
        tournamentTeamRepository.getAllForTournament(tournamentId)
            .observeForever(new androidx.lifecycle.Observer<List<TournamentTeam>>() {
                @Override
                public void onChanged(List<TournamentTeam> teams) {
                    if (teams == null || teams.isEmpty()) {
                        callback.onError(new Exception("No teams found for tournament"));
                        tournamentTeamRepository.getAllForTournament(tournamentId).removeObserver(this);
                        return;
                    }
                    
                    // Update each team's statistics
                    for (TournamentTeam team : teams) {
                        // TODO: Check if this team was in the match
                        // For now, just update the winner if we have one
                        if (winnerTeamId != null && winnerTeamId.equals(team.getTeamId())) {
                            team.setMatchesPlayed(team.getMatchesPlayed() + 1);
                            team.setMatchesWon(team.getMatchesWon() + 1);
                            team.setPoints(team.getPoints() + 2); // 2 points for win
                            
                            // Update team in repository
                            tournamentTeamRepository.update(tournamentId, team)
                                .addOnSuccessListener(aVoid -> {
                                    // Check if stage is complete after this match
                                    checkStageCompletionAfterMatch(tournamentId, matchId, callback);
                                })
                                .addOnFailureListener(callback::onError);
                        }
                    }
                    
                    tournamentTeamRepository.getAllForTournament(tournamentId).removeObserver(this);
                }
            });
    }

    /**
     * Check if stage should advance after match completion
     */
    private void checkStageCompletionAfterMatch(String tournamentId, String matchId, 
                                                TournamentCallback<Void> callback) {
        
        // Find which stage this match belongs to
        tournamentMatchRepository.getByMatchId(tournamentId, matchId)
            .observeForever(new androidx.lifecycle.Observer<TournamentMatch>() {
                @Override
                public void onChanged(TournamentMatch tournamentMatch) {
                    if (tournamentMatch == null) {
                        callback.onSuccess(null);
                        tournamentMatchRepository.getByMatchId(tournamentId, matchId).removeObserver(this);
                        return;
                    }
                    
                    String stageId = tournamentMatch.getStageId();
                    
                    // Check if this stage is now complete
                    checkAndAdvanceStage(tournamentId, stageId, callback);
                    
                    tournamentMatchRepository.getByMatchId(tournamentId, matchId).removeObserver(this);
                }
            });
    }
}