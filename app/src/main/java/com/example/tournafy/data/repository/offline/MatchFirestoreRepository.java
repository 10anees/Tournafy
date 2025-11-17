package com.example.tournafy.data.repository.offline;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
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
        // Add other Match fields as needed
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
                    liveData.setValue(null);
                    return;
                }
                Match match = deserializeMatch(snapshot);
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