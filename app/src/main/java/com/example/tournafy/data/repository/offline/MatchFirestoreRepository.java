package com.example.tournafy.data.repository.offline;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tournafy.domain.models.match.cricket.CricketEvent;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.tournafy.domain.models.base.Match;
import com.example.tournafy.domain.models.match.cricket.CricketMatch;
import com.example.tournafy.domain.models.match.cricket.CricketMatchConfig;
import com.example.tournafy.domain.models.match.football.FootballMatch;
import com.example.tournafy.domain.models.match.football.FootballMatchConfig;
import com.example.tournafy.domain.models.sport.SportTypeEnum;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Firestore repository implementation for Match entities.
 * 
 * CRITICAL FIX: Match is abstract, so we cannot use snapshot.toObject(Match.class).
 * We must check the "sportId" field and deserialize to the correct concrete class
 * (CricketMatch or FootballMatch) to avoid runtime crashes.
 */
@Singleton
public class MatchFirestoreRepository extends FirestoreRepository<Match> {

    public static final String COLLECTION_PATH = "matches";

    @Inject
    public MatchFirestoreRepository(FirebaseFirestore firestoreInstance) {
        super(firestoreInstance, COLLECTION_PATH, Match.class);
    }

    @Override
    protected String getEntityId(Match entity) {
        return entity.getEntityId();
    }
    
    /**
     * CRITICAL: Deserializes a DocumentSnapshot to the correct concrete Match subclass.
     * This prevents crashes from trying to instantiate abstract Match class and MatchConfig.
     * 
     * WORKAROUND: Firestore's toObject() will fail when trying to deserialize abstract
     * MatchConfig. We catch the exception and manually handle the deserialization by
     * reading the matchConfig data as a Map and creating the correct concrete config object.
     * 
     * @param snapshot The DocumentSnapshot containing match data.
     * @return The correct Match subclass instance (CricketMatch or FootballMatch).
     */
    private Match deserializeMatch(DocumentSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) {
            return null;
        }
        
        // Read the sportId field to determine which class to use
        String sportId = snapshot.getString("sportId");
        
        if (sportId == null) {
            return null;
        }
        
        try {
            Match match = null;
            
            // Try direct deserialization first (will fail due to abstract MatchConfig)
            // But we catch the exception and handle it manually
            try {
                if (SportTypeEnum.CRICKET.name().equals(sportId)) {
                    match = snapshot.toObject(CricketMatch.class);
                } else if (SportTypeEnum.FOOTBALL.name().equals(sportId)) {
                    match = snapshot.toObject(FootballMatch.class);
                }
            } catch (RuntimeException e) {
                // Deserialization failed due to abstract MatchConfig
                // Manually deserialize without the matchConfig field
                Map<String, Object> data = snapshot.getData();
                if (data == null) {
                    return null;
                }
                
                // Get and remove matchConfig data to deserialize the rest
                @SuppressWarnings("unchecked")
                Map<String, Object> matchConfigData = (Map<String, Object>) data.get("matchConfig");
                
                // Create a new snapshot view without matchConfig
                // This is a workaround - we manually construct the match object
                if (SportTypeEnum.CRICKET.name().equals(sportId)) {
                    match = new CricketMatch();
                    // Manually set fields from data map
                    populateMatchFields(match, data);
                    
                    // Create and populate CricketMatchConfig
                    if (matchConfigData != null) {
                        CricketMatchConfig config = new CricketMatchConfig();
                        populateConfigFields(config, matchConfigData);
                        match.setMatchConfig(config);
                    }
                } else if (SportTypeEnum.FOOTBALL.name().equals(sportId)) {
                    match = new FootballMatch();
                    // Manually set fields from data map
                    populateMatchFields(match, data);
                    
                    // Create and populate FootballMatchConfig
                    if (matchConfigData != null) {
                        FootballMatchConfig config = new FootballMatchConfig();
                        populateConfigFields(config, matchConfigData);
                        match.setMatchConfig(config);
                    }
                }
            }
            
            return match;
            
        } catch (Exception e) {
            // Final catch-all: return null to prevent app crash
            return null;
        }
    }
    
    /**
     * Helper method to populate Match fields from a data map.
     * This is used when automatic deserialization fails.
     * CRITICAL FIX: Now properly handles cricket-specific fields.
     */
    private void populateMatchFields(Match match, Map<String, Object> data) {
        // Populate common Match fields
        if (data.containsKey("entityId")) match.setEntityId((String) data.get("entityId"));
        if (data.containsKey("name")) match.setName((String) data.get("name"));
        if (data.containsKey("sportId")) match.setSportId((String) data.get("sportId"));
        if (data.containsKey("matchStatus")) match.setMatchStatus((String) data.get("matchStatus"));
        if (data.containsKey("venue")) match.setVenue((String) data.get("venue"));
        if (data.containsKey("hostUserId")) match.setHostUserId((String) data.get("hostUserId"));
        if (data.containsKey("status")) match.setStatus((String) data.get("status"));
        
        // CRITICAL FIX: Populate cricket-specific fields if this is a CricketMatch
        if (match instanceof CricketMatch) {
            CricketMatch cricketMatch = (CricketMatch) match;
            
            // Populate toss fields
            if (data.containsKey("tossWinner")) {
                cricketMatch.setTossWinner((String) data.get("tossWinner"));
                android.util.Log.d("MatchFirestoreRepository", "Deserialized tossWinner: " + data.get("tossWinner"));
            }
            if (data.containsKey("tossDecision")) {
                cricketMatch.setTossDecision((String) data.get("tossDecision"));
                android.util.Log.d("MatchFirestoreRepository", "Deserialized tossDecision: " + data.get("tossDecision"));
            }
            
            // Populate innings
            if (data.containsKey("innings")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> inningsData = (List<Map<String, Object>>) data.get("innings");
                if (inningsData != null) {
                    List<com.example.tournafy.domain.models.match.cricket.Innings> innings = new ArrayList<>();
                    for (Map<String, Object> inningsMap : inningsData) {
                        com.example.tournafy.domain.models.match.cricket.Innings inning = 
                            new com.example.tournafy.domain.models.match.cricket.Innings();
                        if (inningsMap.containsKey("inningsId")) 
                            inning.setInningsId((String) inningsMap.get("inningsId"));
                        if (inningsMap.containsKey("matchId")) 
                            inning.setMatchId((String) inningsMap.get("matchId"));
                        if (inningsMap.containsKey("inningsNumber")) 
                            inning.setInningsNumber(((Long) inningsMap.get("inningsNumber")).intValue());
                        if (inningsMap.containsKey("battingTeamId")) 
                            inning.setBattingTeamId((String) inningsMap.get("battingTeamId"));
                        if (inningsMap.containsKey("bowlingTeamId")) 
                            inning.setBowlingTeamId((String) inningsMap.get("bowlingTeamId"));
                        if (inningsMap.containsKey("totalRuns")) 
                            inning.setTotalRuns(((Long) inningsMap.get("totalRuns")).intValue());
                        if (inningsMap.containsKey("wicketsFallen")) 
                            inning.setWicketsFallen(((Long) inningsMap.get("wicketsFallen")).intValue());
                        if (inningsMap.containsKey("oversCompleted")) 
                            inning.setOversCompleted(((Long) inningsMap.get("oversCompleted")).intValue());
                        if (inningsMap.containsKey("completed")) 
                            inning.setCompleted((Boolean) inningsMap.get("completed"));
                        innings.add(inning);
                    }
                    cricketMatch.setInnings(innings);
                    android.util.Log.d("MatchFirestoreRepository", "Deserialized " + innings.size() + 
                        " innings for match " + match.getEntityId());
                }
            }
            
            // Populate cricketEvents
            if (data.containsKey("cricketEvents")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> eventsData = (List<Map<String, Object>>) data.get("cricketEvents");
                if (eventsData != null) {
                    List<CricketEvent> events = new ArrayList<>();
                    // TODO: Populate individual CricketEvent fields from map
                    // For now, we'll skip detailed event deserialization
                    cricketMatch.setCricketEvents(events);
                }
            }
            
            // Populate teams with players
            if (data.containsKey("teams")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> teamsData = (List<Map<String, Object>>) data.get("teams");
                if (teamsData != null) {
                    List<com.example.tournafy.domain.models.team.MatchTeam> teams = new ArrayList<>();
                    for (Map<String, Object> teamMap : teamsData) {
                        com.example.tournafy.domain.models.team.MatchTeam team = 
                            new com.example.tournafy.domain.models.team.MatchTeam();
                        if (teamMap.containsKey("teamId")) 
                            team.setTeamId((String) teamMap.get("teamId"));
                        if (teamMap.containsKey("teamName")) 
                            team.setTeamName((String) teamMap.get("teamName"));
                        
                        // Deserialize players list
                        if (teamMap.containsKey("players")) {
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> playersData = (List<Map<String, Object>>) teamMap.get("players");
                            if (playersData != null) {
                                List<com.example.tournafy.domain.models.team.Player> players = new ArrayList<>();
                                for (Map<String, Object> playerMap : playersData) {
                                    com.example.tournafy.domain.models.team.Player player = 
                                        new com.example.tournafy.domain.models.team.Player();
                                    if (playerMap.containsKey("playerId")) 
                                        player.setPlayerId((String) playerMap.get("playerId"));
                                    if (playerMap.containsKey("playerName")) 
                                        player.setPlayerName((String) playerMap.get("playerName"));
                                    if (playerMap.containsKey("jerseyNumber")) 
                                        player.setJerseyNumber(((Long) playerMap.get("jerseyNumber")).intValue());
                                    players.add(player);
                                }
                                team.setPlayers(players);
                                android.util.Log.d("MatchFirestoreRepository", "Deserialized " + players.size() + 
                                    " players for team " + team.getTeamName());
                            }
                        }
                        
                        teams.add(team);
                    }
                    cricketMatch.setTeams(teams);
                }
            }
            
            // Populate currentInningsNumber and targetScore
            if (data.containsKey("currentInningsNumber")) {
                Object inningsNum = data.get("currentInningsNumber");
                if (inningsNum instanceof Long) {
                    // Use reflection to set private field
                    try {
                        java.lang.reflect.Field field = CricketMatch.class.getDeclaredField("currentInningsNumber");
                        field.setAccessible(true);
                        field.set(cricketMatch, ((Long) inningsNum).intValue());
                        android.util.Log.d("MatchFirestoreRepository", "Set currentInningsNumber to " + inningsNum);
                    } catch (Exception e) {
                        android.util.Log.e("MatchFirestoreRepository", "Failed to set currentInningsNumber", e);
                    }
                }
            }
            if (data.containsKey("targetScore")) {
                Object targetScoreObj = data.get("targetScore");
                if (targetScoreObj instanceof Long) {
                    // Use reflection to set private field
                    try {
                        java.lang.reflect.Field field = CricketMatch.class.getDeclaredField("targetScore");
                        field.setAccessible(true);
                        field.set(cricketMatch, ((Long) targetScoreObj).intValue());
                    } catch (Exception e) {
                        android.util.Log.e("MatchFirestoreRepository", "Failed to set targetScore", e);
                    }
                }
            }
            
            // Populate player tracking fields (striker, non-striker, bowler)
            if (data.containsKey("currentStrikerId")) {
                Object strikerId = data.get("currentStrikerId");
                if (strikerId instanceof String) {
                    try {
                        java.lang.reflect.Field field = CricketMatch.class.getDeclaredField("currentStrikerId");
                        field.setAccessible(true);
                        field.set(cricketMatch, strikerId);
                        android.util.Log.d("MatchFirestoreRepository", "Set currentStrikerId to " + strikerId);
                    } catch (Exception e) {
                        android.util.Log.e("MatchFirestoreRepository", "Failed to set currentStrikerId", e);
                    }
                }
            }
            if (data.containsKey("currentNonStrikerId")) {
                Object nonStrikerId = data.get("currentNonStrikerId");
                if (nonStrikerId instanceof String) {
                    try {
                        java.lang.reflect.Field field = CricketMatch.class.getDeclaredField("currentNonStrikerId");
                        field.setAccessible(true);
                        field.set(cricketMatch, nonStrikerId);
                        android.util.Log.d("MatchFirestoreRepository", "Set currentNonStrikerId to " + nonStrikerId);
                    } catch (Exception e) {
                        android.util.Log.e("MatchFirestoreRepository", "Failed to set currentNonStrikerId", e);
                    }
                }
            }
            if (data.containsKey("currentBowlerId")) {
                Object bowlerId = data.get("currentBowlerId");
                if (bowlerId instanceof String) {
                    try {
                        java.lang.reflect.Field field = CricketMatch.class.getDeclaredField("currentBowlerId");
                        field.setAccessible(true);
                        field.set(cricketMatch, bowlerId);
                        android.util.Log.d("MatchFirestoreRepository", "Set currentBowlerId to " + bowlerId);
                    } catch (Exception e) {
                        android.util.Log.e("MatchFirestoreRepository", "Failed to set currentBowlerId", e);
                    }
                }
            }
            
            // Populate currentOvers with nested balls
            if (data.containsKey("currentOvers")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> oversData = (List<Map<String, Object>>) data.get("currentOvers");
                if (oversData != null) {
                    List<com.example.tournafy.domain.models.match.cricket.Over> overs = new ArrayList<>();
                    for (Map<String, Object> overMap : oversData) {
                        com.example.tournafy.domain.models.match.cricket.Over over = 
                            new com.example.tournafy.domain.models.match.cricket.Over();
                        if (overMap.containsKey("overId")) 
                            over.setOverId((String) overMap.get("overId"));
                        if (overMap.containsKey("inningsId")) 
                            over.setInningsId((String) overMap.get("inningsId"));
                        if (overMap.containsKey("overNumber")) 
                            over.setOverNumber(((Long) overMap.get("overNumber")).intValue());
                        if (overMap.containsKey("bowlerId")) 
                            over.setBowlerId((String) overMap.get("bowlerId"));
                        if (overMap.containsKey("runsInOver")) 
                            over.setRunsInOver(((Long) overMap.get("runsInOver")).intValue());
                        if (overMap.containsKey("wicketsInOver")) 
                            over.setWicketsInOver(((Long) overMap.get("wicketsInOver")).intValue());
                        if (overMap.containsKey("completed")) 
                            over.setCompleted((Boolean) overMap.get("completed"));
                        
                        // Deserialize balls list
                        if (overMap.containsKey("balls")) {
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> ballsData = (List<Map<String, Object>>) overMap.get("balls");
                            if (ballsData != null) {
                                List<com.example.tournafy.domain.models.match.cricket.Ball> balls = new ArrayList<>();
                                for (Map<String, Object> ballMap : ballsData) {
                                    com.example.tournafy.domain.models.match.cricket.Ball ball = 
                                        new com.example.tournafy.domain.models.match.cricket.Ball();
                                    if (ballMap.containsKey("ballId")) 
                                        ball.setBallId((String) ballMap.get("ballId"));
                                    if (ballMap.containsKey("matchId")) 
                                        ball.setMatchId((String) ballMap.get("matchId"));
                                    if (ballMap.containsKey("inningsId")) 
                                        ball.setInningsId((String) ballMap.get("inningsId"));
                                    if (ballMap.containsKey("overId")) 
                                        ball.setOverId((String) ballMap.get("overId"));
                                    if (ballMap.containsKey("inningsNumber")) 
                                        ball.setInningsNumber(((Long) ballMap.get("inningsNumber")).intValue());
                                    if (ballMap.containsKey("overNumber")) 
                                        ball.setOverNumber(((Long) ballMap.get("overNumber")).intValue());
                                    if (ballMap.containsKey("ballNumber")) 
                                        ball.setBallNumber(((Long) ballMap.get("ballNumber")).intValue());
                                    if (ballMap.containsKey("batsmanId")) 
                                        ball.setBatsmanId((String) ballMap.get("batsmanId"));
                                    if (ballMap.containsKey("bowlerId")) 
                                        ball.setBowlerId((String) ballMap.get("bowlerId"));
                                    if (ballMap.containsKey("runsScored")) 
                                        ball.setRunsScored(((Long) ballMap.get("runsScored")).intValue());
                                    if (ballMap.containsKey("isWicket")) 
                                        ball.setWicket((Boolean) ballMap.get("isWicket"));
                                    if (ballMap.containsKey("isBoundary")) 
                                        ball.setBoundary((Boolean) ballMap.get("isBoundary"));
                                    if (ballMap.containsKey("extrasType")) 
                                        ball.setExtrasType((String) ballMap.get("extrasType"));
                                    if (ballMap.containsKey("wicketType")) 
                                        ball.setWicketType((String) ballMap.get("wicketType"));
                                    balls.add(ball);
                                }
                                over.setBalls(balls);
                                android.util.Log.d("MatchFirestoreRepository", "Deserialized " + balls.size() + 
                                    " balls for over " + over.getOverNumber());
                            }
                        }
                        
                        overs.add(over);
                    }
                    // Use reflection to set private currentOvers field
                    try {
                        java.lang.reflect.Field field = CricketMatch.class.getDeclaredField("currentOvers");
                        field.setAccessible(true);
                        field.set(cricketMatch, overs);
                        android.util.Log.d("MatchFirestoreRepository", "Deserialized " + overs.size() + 
                            " overs for match " + match.getEntityId());
                    } catch (Exception e) {
                        android.util.Log.e("MatchFirestoreRepository", "Failed to set currentOvers", e);
                    }
                }
            }
            
            // CRITICAL FIX: Populate batsmanStatsMap
            if (data.containsKey("batsmanStatsMap")) {
                android.util.Log.d("MatchFirestoreRepository", "Deserializing batsmanStatsMap");
                @SuppressWarnings("unchecked")
                Map<String, Object> batsmanStatsData = (Map<String, Object>) data.get("batsmanStatsMap");
                if (batsmanStatsData != null) {
                    Map<String, com.example.tournafy.domain.models.match.cricket.BatsmanStats> batsmanStats = new java.util.HashMap<>();
                    for (Map.Entry<String, Object> entry : batsmanStatsData.entrySet()) {
                        String playerId = entry.getKey();
                        @SuppressWarnings("unchecked")
                        Map<String, Object> statsMap = (Map<String, Object>) entry.getValue();
                        if (statsMap != null) {
                            // Get playerName from stats map, or use empty string as fallback
                            String playerName = statsMap.containsKey("playerName") ? (String) statsMap.get("playerName") : "";
                            
                            com.example.tournafy.domain.models.match.cricket.BatsmanStats stats = 
                                new com.example.tournafy.domain.models.match.cricket.BatsmanStats(playerId, playerName);
                            
                            if (statsMap.containsKey("runsScored")) 
                                stats.setRunsScored(((Long) statsMap.get("runsScored")).intValue());
                            if (statsMap.containsKey("ballsFaced")) 
                                stats.setBallsFaced(((Long) statsMap.get("ballsFaced")).intValue());
                            if (statsMap.containsKey("fours")) 
                                stats.setFours(((Long) statsMap.get("fours")).intValue());
                            if (statsMap.containsKey("sixes")) 
                                stats.setSixes(((Long) statsMap.get("sixes")).intValue());
                            if (statsMap.containsKey("out")) 
                                stats.setOut((Boolean) statsMap.get("out"));
                            if (statsMap.containsKey("dismissalType")) 
                                stats.setDismissalType((String) statsMap.get("dismissalType"));
                            // Note: strikeRate is calculated automatically in getStrikeRate()
                            
                            batsmanStats.put(playerId, stats);
                            android.util.Log.d("MatchFirestoreRepository", "Deserialized batsman stats for player " + playerId + 
                                ": " + stats.getRunsScored() + "(" + stats.getBallsFaced() + ")");
                        }
                    }
                    cricketMatch.setBatsmanStatsMap(batsmanStats);
                    android.util.Log.d("MatchFirestoreRepository", "Set batsmanStatsMap with " + batsmanStats.size() + " entries");
                }
            }
            
            // CRITICAL FIX: Populate bowlerStatsMap
            if (data.containsKey("bowlerStatsMap")) {
                android.util.Log.d("MatchFirestoreRepository", "Deserializing bowlerStatsMap");
                @SuppressWarnings("unchecked")
                Map<String, Object> bowlerStatsData = (Map<String, Object>) data.get("bowlerStatsMap");
                if (bowlerStatsData != null) {
                    Map<String, com.example.tournafy.domain.models.match.cricket.BowlerStats> bowlerStats = new java.util.HashMap<>();
                    for (Map.Entry<String, Object> entry : bowlerStatsData.entrySet()) {
                        String playerId = entry.getKey();
                        @SuppressWarnings("unchecked")
                        Map<String, Object> statsMap = (Map<String, Object>) entry.getValue();
                        if (statsMap != null) {
                            // Get playerName from stats map, or use empty string as fallback
                            String playerName = statsMap.containsKey("playerName") ? (String) statsMap.get("playerName") : "";
                            
                            com.example.tournafy.domain.models.match.cricket.BowlerStats stats = 
                                new com.example.tournafy.domain.models.match.cricket.BowlerStats(playerId, playerName);
                            
                            if (statsMap.containsKey("ballsBowled")) 
                                stats.setBallsBowled(((Long) statsMap.get("ballsBowled")).intValue());
                            if (statsMap.containsKey("runsConceded")) 
                                stats.setRunsConceded(((Long) statsMap.get("runsConceded")).intValue());
                            if (statsMap.containsKey("wicketsTaken")) 
                                stats.setWicketsTaken(((Long) statsMap.get("wicketsTaken")).intValue());
                            if (statsMap.containsKey("maidenOvers")) 
                                stats.setMaidenOvers(((Long) statsMap.get("maidenOvers")).intValue());
                            if (statsMap.containsKey("wides")) 
                                stats.setWides(((Long) statsMap.get("wides")).intValue());
                            if (statsMap.containsKey("noBalls")) 
                                stats.setNoBalls(((Long) statsMap.get("noBalls")).intValue());
                            // Note: oversBowled and economyRate are calculated automatically
                            
                            bowlerStats.put(playerId, stats);
                            android.util.Log.d("MatchFirestoreRepository", "Deserialized bowler stats for player " + playerId + 
                                ": " + stats.getOversBowled() + "-" + stats.getWicketsTaken() + "-" + stats.getRunsConceded());
                        }
                    }
                    cricketMatch.setBowlerStatsMap(bowlerStats);
                    android.util.Log.d("MatchFirestoreRepository", "Set bowlerStatsMap with " + bowlerStats.size() + " entries");
                }
            }
        }
        
        // CRITICAL FIX: Populate football-specific fields if this is a FootballMatch
        if (match instanceof FootballMatch) {
            FootballMatch footballMatch = (FootballMatch) match;
            
            android.util.Log.d("MatchFirestoreRepository", "=== DESERIALIZING FOOTBALL MATCH ===");
            android.util.Log.d("MatchFirestoreRepository", "Match ID: " + match.getEntityId());
            
            // Populate teams with players
            if (data.containsKey("teams")) {
                android.util.Log.d("MatchFirestoreRepository", "Found 'teams' key in data");
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> teamsData = (List<Map<String, Object>>) data.get("teams");
                if (teamsData != null) {
                    android.util.Log.d("MatchFirestoreRepository", "teamsData is NOT null, size: " + teamsData.size());
                    List<com.example.tournafy.domain.models.team.MatchTeam> teams = new ArrayList<>();
                    for (Map<String, Object> teamMap : teamsData) {
                        com.example.tournafy.domain.models.team.MatchTeam team = 
                            new com.example.tournafy.domain.models.team.MatchTeam();
                        if (teamMap.containsKey("teamId")) 
                            team.setTeamId((String) teamMap.get("teamId"));
                        if (teamMap.containsKey("teamName")) 
                            team.setTeamName((String) teamMap.get("teamName"));
                        if (teamMap.containsKey("homeTeam"))
                            team.setHomeTeam((Boolean) teamMap.get("homeTeam"));
                        
                        android.util.Log.d("MatchFirestoreRepository", "Processing team: " + team.getTeamName());
                        
                        // Deserialize players list
                        if (teamMap.containsKey("players")) {
                            android.util.Log.d("MatchFirestoreRepository", "Found 'players' key in team data");
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> playersData = (List<Map<String, Object>>) teamMap.get("players");
                            if (playersData != null) {
                                android.util.Log.d("MatchFirestoreRepository", "playersData is NOT null, size: " + playersData.size());
                                List<com.example.tournafy.domain.models.team.Player> players = new ArrayList<>();
                                for (Map<String, Object> playerMap : playersData) {
                                    com.example.tournafy.domain.models.team.Player player = 
                                        new com.example.tournafy.domain.models.team.Player();
                                    if (playerMap.containsKey("playerId")) 
                                        player.setPlayerId((String) playerMap.get("playerId"));
                                    if (playerMap.containsKey("playerName")) 
                                        player.setPlayerName((String) playerMap.get("playerName"));
                                    if (playerMap.containsKey("jerseyNumber")) 
                                        player.setJerseyNumber(((Long) playerMap.get("jerseyNumber")).intValue());
                                    
                                    // CRITICAL FIX: Read isStartingXI field from Firestore
                                    if (playerMap.containsKey("startingXI")) {
                                        player.setStartingXI((Boolean) playerMap.get("startingXI"));
                                    }
                                    
                                    android.util.Log.d("MatchFirestoreRepository", "  Deserialized player: " + 
                                        player.getPlayerName() + " (ID: " + player.getPlayerId() + "), Starting XI: " + player.isStartingXI());
                                    players.add(player);
                                }
                                team.setPlayers(players);
                                android.util.Log.d("MatchFirestoreRepository", "Set " + players.size() + 
                                    " players for football team " + team.getTeamName());
                            } else {
                                android.util.Log.d("MatchFirestoreRepository", "playersData is NULL for team " + team.getTeamName());
                            }
                        } else {
                            android.util.Log.d("MatchFirestoreRepository", "No 'players' key in team data for " + team.getTeamName());
                        }
                        
                        teams.add(team);
                    }
                    footballMatch.setTeams(teams);
                    android.util.Log.d("MatchFirestoreRepository", "Set " + teams.size() + 
                        " teams for football match " + match.getEntityId());
                    
                    // Verify teams were actually set
                    List<com.example.tournafy.domain.models.team.MatchTeam> verifyTeams = footballMatch.getTeams();
                    if (verifyTeams != null) {
                        android.util.Log.d("MatchFirestoreRepository", "VERIFICATION: getTeams() returns " + verifyTeams.size() + " teams");
                        for (int i = 0; i < verifyTeams.size(); i++) {
                            com.example.tournafy.domain.models.team.MatchTeam t = verifyTeams.get(i);
                            android.util.Log.d("MatchFirestoreRepository", "  Team " + i + ": " + t.getTeamName() + 
                                " with " + (t.getPlayers() != null ? t.getPlayers().size() : 0) + " players");
                        }
                    } else {
                        android.util.Log.d("MatchFirestoreRepository", "VERIFICATION FAILED: getTeams() returns NULL!");
                    }
                } else {
                    android.util.Log.d("MatchFirestoreRepository", "teamsData is NULL");
                }
            } else {
                android.util.Log.d("MatchFirestoreRepository", "No 'teams' key in data");
            }
            
            // Populate football events
            if (data.containsKey("footballEvents")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> eventsData = (List<Map<String, Object>>) data.get("footballEvents");
                if (eventsData != null) {
                    List<com.example.tournafy.domain.models.match.football.FootballEvent> events = new ArrayList<>();
                    
                    // Deserialize each event
                    for (Map<String, Object> eventMap : eventsData) {
                        try {
                            com.example.tournafy.domain.models.match.football.FootballEvent event = 
                                new com.example.tournafy.domain.models.match.football.FootballEvent();
                            
                            // Basic fields
                            if (eventMap.containsKey("eventId")) 
                                event.setEventId((String) eventMap.get("eventId"));
                            if (eventMap.containsKey("matchId")) 
                                event.setMatchId((String) eventMap.get("matchId"));
                            if (eventMap.containsKey("teamId")) 
                                event.setTeamId((String) eventMap.get("teamId"));
                            if (eventMap.containsKey("playerId")) 
                                event.setPlayerId((String) eventMap.get("playerId"));
                            if (eventMap.containsKey("eventType")) 
                                event.setEventType((String) eventMap.get("eventType"));
                            if (eventMap.containsKey("eventCategory")) 
                                event.setEventCategory((String) eventMap.get("eventCategory"));
                            if (eventMap.containsKey("description")) 
                                event.setDescription((String) eventMap.get("description"));
                            if (eventMap.containsKey("matchMinute")) 
                                event.setMatchMinute(((Long) eventMap.get("matchMinute")).intValue());
                            if (eventMap.containsKey("addedTimeMinute")) 
                                event.setAddedTimeMinute(((Long) eventMap.get("addedTimeMinute")).intValue());
                            if (eventMap.containsKey("matchPeriod")) 
                                event.setMatchPeriod((String) eventMap.get("matchPeriod"));
                            if (eventMap.containsKey("homeScoreAtEvent")) 
                                event.setHomeScoreAtEvent(((Long) eventMap.get("homeScoreAtEvent")).intValue());
                            if (eventMap.containsKey("awayScoreAtEvent")) 
                                event.setAwayScoreAtEvent(((Long) eventMap.get("awayScoreAtEvent")).intValue());
                            if (eventMap.containsKey("locationOnPitch")) 
                                event.setLocationOnPitch((String) eventMap.get("locationOnPitch"));
                            
                            // Deserialize GoalDetail if present
                            if (eventMap.containsKey("goalDetail")) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> goalMap = (Map<String, Object>) eventMap.get("goalDetail");
                                if (goalMap != null) {
                                    com.example.tournafy.domain.models.match.football.FootballGoalDetail goalDetail = 
                                        new com.example.tournafy.domain.models.match.football.FootballGoalDetail();
                                    
                                    if (goalMap.containsKey("eventId")) 
                                        goalDetail.setEventId((String) goalMap.get("eventId"));
                                    if (goalMap.containsKey("scorerId")) 
                                        goalDetail.setScorerId((String) goalMap.get("scorerId"));
                                    if (goalMap.containsKey("assistPlayerId")) 
                                        goalDetail.setAssistPlayerId((String) goalMap.get("assistPlayerId"));
                                    if (goalMap.containsKey("goalType")) 
                                        goalDetail.setGoalType((String) goalMap.get("goalType"));
                                    if (goalMap.containsKey("minuteScored")) 
                                        goalDetail.setMinuteScored(((Long) goalMap.get("minuteScored")).intValue());
                                    if (goalMap.containsKey("penalty") && goalMap.get("penalty") instanceof Boolean) 
                                        goalDetail.setPenalty((Boolean) goalMap.get("penalty"));
                                    if (goalMap.containsKey("ownGoal") && goalMap.get("ownGoal") instanceof Boolean) 
                                        goalDetail.setOwnGoal((Boolean) goalMap.get("ownGoal"));
                                    
                                    event.setGoalDetail(goalDetail);
                                }
                            }
                            
                            // Deserialize CardDetail if present
                            if (eventMap.containsKey("cardDetail")) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> cardMap = (Map<String, Object>) eventMap.get("cardDetail");
                                if (cardMap != null) {
                                    com.example.tournafy.domain.models.match.football.FootballCardDetail cardDetail = 
                                        new com.example.tournafy.domain.models.match.football.FootballCardDetail();
                                    
                                    if (cardMap.containsKey("eventId")) 
                                        cardDetail.setEventId((String) cardMap.get("eventId"));
                                    if (cardMap.containsKey("playerId")) 
                                        cardDetail.setPlayerId((String) cardMap.get("playerId"));
                                    if (cardMap.containsKey("cardType")) 
                                        cardDetail.setCardType((String) cardMap.get("cardType"));
                                    if (cardMap.containsKey("cardReason")) 
                                        cardDetail.setCardReason((String) cardMap.get("cardReason"));
                                    if (cardMap.containsKey("minuteIssued")) 
                                        cardDetail.setMinuteIssued(((Long) cardMap.get("minuteIssued")).intValue());
                                    if (cardMap.containsKey("secondYellow") && cardMap.get("secondYellow") instanceof Boolean) 
                                        cardDetail.setSecondYellow((Boolean) cardMap.get("secondYellow"));
                                    
                                    event.setCardDetail(cardDetail);
                                }
                            }
                            
                            // Deserialize SubstitutionDetail if present
                            if (eventMap.containsKey("substitutionDetail")) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> subMap = (Map<String, Object>) eventMap.get("substitutionDetail");
                                if (subMap != null) {
                                    com.example.tournafy.domain.models.match.football.FootballSubstitutionDetail subDetail = 
                                        new com.example.tournafy.domain.models.match.football.FootballSubstitutionDetail();
                                    
                                    if (subMap.containsKey("eventId")) 
                                        subDetail.setEventId((String) subMap.get("eventId"));
                                    if (subMap.containsKey("playerOutId")) 
                                        subDetail.setPlayerOutId((String) subMap.get("playerOutId"));
                                    if (subMap.containsKey("playerInId")) 
                                        subDetail.setPlayerInId((String) subMap.get("playerInId"));
                                    if (subMap.containsKey("minuteSubstituted")) 
                                        subDetail.setMinuteSubstituted(((Long) subMap.get("minuteSubstituted")).intValue());
                                    if (subMap.containsKey("substitutionReason")) 
                                        subDetail.setSubstitutionReason((String) subMap.get("substitutionReason"));
                                    
                                    event.setSubstitutionDetail(subDetail);
                                }
                            }
                            
                            events.add(event);
                        } catch (Exception e) {
                            android.util.Log.e("MatchFirestoreRepository", "Failed to deserialize FootballEvent", e);
                        }
                    }
                    
                    footballMatch.setFootballEvents(events);
                    android.util.Log.d("MatchFirestoreRepository", "Deserialized " + events.size() + " football events");
                }
            }
            
            // Populate scores
            if (data.containsKey("homeScore")) {
                Object homeScoreObj = data.get("homeScore");
                if (homeScoreObj instanceof Long) {
                    footballMatch.setHomeScore(((Long) homeScoreObj).intValue());
                }
            }
            if (data.containsKey("awayScore")) {
                Object awayScoreObj = data.get("awayScore");
                if (awayScoreObj instanceof Long) {
                    footballMatch.setAwayScore(((Long) awayScoreObj).intValue());
                }
            }
            
            // Populate match period and minute
            if (data.containsKey("matchPeriod")) {
                // Set via reflection since matchPeriod is private
                try {
                    java.lang.reflect.Field field = FootballMatch.class.getDeclaredField("matchPeriod");
                    field.setAccessible(true);
                    field.set(footballMatch, (String) data.get("matchPeriod"));
                } catch (Exception e) {
                    android.util.Log.e("MatchFirestoreRepository", "Failed to set matchPeriod", e);
                }
            }
            if (data.containsKey("currentMatchMinute")) {
                Object minuteObj = data.get("currentMatchMinute");
                if (minuteObj instanceof Long) {
                    // Set via reflection if field is private
                    try {
                        java.lang.reflect.Field field = FootballMatch.class.getDeclaredField("currentMatchMinute");
                        field.setAccessible(true);
                        field.set(footballMatch, ((Long) minuteObj).intValue());
                    } catch (Exception e) {
                        android.util.Log.e("MatchFirestoreRepository", "Failed to set currentMatchMinute", e);
                    }
                }
            }
            
            // CRITICAL: Deserialize timer state for persistence across navigation
            if (data.containsKey("elapsedTimeMillis")) {
                Object elapsedObj = data.get("elapsedTimeMillis");
                if (elapsedObj instanceof Long) {
                    footballMatch.setElapsedTimeMillis((Long) elapsedObj);
                    android.util.Log.d("MatchFirestoreRepository", "Deserialized elapsedTimeMillis: " + elapsedObj + "ms");
                }
            }
            
            if (data.containsKey("timerRunning")) {
                Object timerRunningObj = data.get("timerRunning");
                if (timerRunningObj instanceof Boolean) {
                    footballMatch.setTimerRunning((Boolean) timerRunningObj);
                    android.util.Log.d("MatchFirestoreRepository", "Deserialized timerRunning: " + timerRunningObj);
                }
            }
        }
    }
    
    /**
     * Helper method to populate MatchConfig fields from a data map.
     */
    private void populateConfigFields(com.example.tournafy.domain.models.base.MatchConfig config, Map<String, Object> data) {
        // Populate base MatchConfig fields
        if (data.containsKey("configId")) config.setConfigId((String) data.get("configId"));
        if (data.containsKey("matchId")) config.setMatchId((String) data.get("matchId"));
        
        // Populate FootballMatchConfig specific fields
        if (config instanceof com.example.tournafy.domain.models.match.football.FootballMatchConfig) {
            com.example.tournafy.domain.models.match.football.FootballMatchConfig footballConfig = 
                (com.example.tournafy.domain.models.match.football.FootballMatchConfig) config;
            
            if (data.containsKey("matchDuration")) {
                Object durationObj = data.get("matchDuration");
                if (durationObj instanceof Long) {
                    footballConfig.setMatchDuration(((Long) durationObj).intValue());
                    android.util.Log.d("MatchFirestoreRepository", "Deserialized matchDuration: " + ((Long) durationObj).intValue());
                }
            }
            
            if (data.containsKey("playersPerSide")) {
                Object playersObj = data.get("playersPerSide");
                if (playersObj instanceof Long) {
                    footballConfig.setPlayersPerSide(((Long) playersObj).intValue());
                    android.util.Log.d("MatchFirestoreRepository", "Deserialized playersPerSide: " + ((Long) playersObj).intValue());
                }
            }
            
            if (data.containsKey("offsideOn")) {
                Object offsideObj = data.get("offsideOn");
                if (offsideObj instanceof Boolean) {
                    footballConfig.setOffsideOn((Boolean) offsideObj);
                    android.util.Log.d("MatchFirestoreRepository", "Deserialized offsideOn: " + offsideObj);
                }
            }
        }
        
        // Populate CricketMatchConfig specific fields
        if (config instanceof com.example.tournafy.domain.models.match.cricket.CricketMatchConfig) {
            com.example.tournafy.domain.models.match.cricket.CricketMatchConfig cricketConfig = 
                (com.example.tournafy.domain.models.match.cricket.CricketMatchConfig) config;
            
            if (data.containsKey("numberOfOvers")) {
                Object oversObj = data.get("numberOfOvers");
                if (oversObj instanceof Long) {
                    cricketConfig.setNumberOfOvers(((Long) oversObj).intValue());
                    android.util.Log.d("MatchFirestoreRepository", "Deserialized numberOfOvers: " + oversObj);
                }
            }
            
            if (data.containsKey("playersPerSide")) {
                Object playersObj = data.get("playersPerSide");
                if (playersObj instanceof Long) {
                    cricketConfig.setPlayersPerSide(((Long) playersObj).intValue());
                    android.util.Log.d("MatchFirestoreRepository", "Deserialized cricket playersPerSide: " + playersObj);
                }
            }
            
            if (data.containsKey("wideOn")) {
                Object wideObj = data.get("wideOn");
                if (wideObj instanceof Boolean) {
                    cricketConfig.setWideOn((Boolean) wideObj);
                }
            }
            
            if (data.containsKey("lastManStanding")) {
                Object lastManObj = data.get("lastManStanding");
                if (lastManObj instanceof Boolean) {
                    cricketConfig.setLastManStanding((Boolean) lastManObj);
                    android.util.Log.d("MatchFirestoreRepository", "Deserialized lastManStanding: " + lastManObj);
                }
            }
        }
    }
    
    /**
     * OVERRIDE: Custom implementation that handles polymorphism.
     */
    @Override
    public LiveData<Match> getById(String id) {
        MutableLiveData<Match> liveData = new MutableLiveData<>();
        
        collectionReference.document(id)
            .addSnapshotListener((snapshot, e) -> {
                if (e != null) {
                    android.util.Log.e("MatchFirestoreRepository", "Error loading match " + id, e);
                    liveData.setValue(null);
                    return;
                }
                Match match = deserializeMatch(snapshot);
                if (match != null) {
                    android.util.Log.d("MatchFirestoreRepository", "Loaded match from Firestore - ID: " + 
                        match.getEntityId() + ", Name: " + match.getName() + ", Status: " + match.getMatchStatus());
                }
                liveData.setValue(match);
            });
            
        return liveData;
    }
    
    /**
     * OVERRIDE: Custom implementation that handles polymorphism.
     */
    @Override
    public LiveData<List<Match>> getAll() {
        MutableLiveData<List<Match>> liveData = new MutableLiveData<>();
        
        collectionReference.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                liveData.setValue(null);
                return;
            }
            if (snapshots != null) {
                List<Match> matches = new ArrayList<>();
                for (DocumentSnapshot doc : snapshots.getDocuments()) {
                    Match match = deserializeMatch(doc);
                    if (match != null) {
                        matches.add(match);
                    }
                }
                liveData.setValue(matches);
            }
        });
        
        return liveData;
    }
    
    /**
     * Custom repository method to find all matches hosted by a specific user.
     * FIXED: Uses deserializeMatch() to handle polymorphism.
     * @param hostId The ID of the host user.
     * @return LiveData holding a list of matches.
     */
    public LiveData<List<Match>> getMatchesByHostId(String hostId) {
        MutableLiveData<List<Match>> liveData = new MutableLiveData<>();
        
        collectionReference.whereEqualTo("hostUserId", hostId)
            .addSnapshotListener((snapshots, e) -> {
                if (e != null) {
                    liveData.setValue(null);
                    return;
                }
                if (snapshots != null) {
                    List<Match> matches = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Match match = deserializeMatch(doc);
                        if (match != null) {
                            matches.add(match);
                        }
                    }
                    liveData.setValue(matches);
                }
            });
            
        return liveData;
    }
    
    /**
     * Overrides the generic 'add' method to ensure a HostedEntity ID is set.
     */
    @Override
    public com.google.android.gms.tasks.Task<Void> add(Match entity) {
        // If the entity doesn't have an ID, create one.
        if (entity.getEntityId() == null || entity.getEntityId().isEmpty()) {
            String newId = collectionReference.document().getId();
            entity.setEntityId(newId);
        }
        return addOrUpdateWithId(entity.getEntityId(), entity);
    }
}