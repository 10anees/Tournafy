package com.example.tournafy.data.repository.offline;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.tournafy.domain.models.base.Match;
import com.example.tournafy.domain.models.match.cricket.CricketMatch;
import com.example.tournafy.domain.models.match.football.FootballMatch;
import com.example.tournafy.domain.models.sport.SportTypeEnum;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Firestore repository implementation for Match entities.
 * 
 * CRITICAL FIX: Match is abstract, so we cannot use snapshot.toObject(Match.class).
 * We must check the "sportType" field and deserialize to the correct concrete class
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
     * This prevents crashes from trying to instantiate abstract Match class.
     * @param snapshot The DocumentSnapshot containing match data.
     * @return The correct Match subclass instance (CricketMatch or FootballMatch).
     */
    private Match deserializeMatch(DocumentSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) {
            return null;
        }
        
        // Read the sportType field to determine which class to use
        String sportType = snapshot.getString("sportType");
        
        if (sportType == null) {
            return null;
        }
        
        // Deserialize to the correct concrete class
        if (SportTypeEnum.CRICKET.name().equals(sportType)) {
            return snapshot.toObject(CricketMatch.class);
        } else if (SportTypeEnum.FOOTBALL.name().equals(sportType)) {
            return snapshot.toObject(FootballMatch.class);
        }
        
        return null;
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