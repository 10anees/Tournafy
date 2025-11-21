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
        }
    }
    
    /**
     * Helper method to populate MatchConfig fields from a data map.
     */
    private void populateConfigFields(com.example.tournafy.domain.models.base.MatchConfig config, Map<String, Object> data) {
        if (data.containsKey("configId")) config.setConfigId((String) data.get("configId"));
        if (data.containsKey("matchId")) config.setMatchId((String) data.get("matchId"));
        // Add other MatchConfig fields as needed
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