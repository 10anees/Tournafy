package com.example.tournafy.data.repository.offline;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.tournafy.domain.models.base.Match; // Uses the Match model
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Firestore repository implementation for Match entities.
 */
@Singleton
public class MatchFirestoreRepository extends FirestoreRepository<Match> {

    public static final String COLLECTION_PATH = "matches";

    @Inject
    public MatchFirestoreRepository(FirebaseFirestore firestoreInstance) {
        // Pass the class type for the abstract Match.
        // Firestore can deserialize into this as long as the concrete
        // classes (CricketMatch, FootballMatch) don't have extra
        // fields that need to be saved at this top level.
        super(firestoreInstance, COLLECTION_PATH, Match.class);
    }

    @Override
    protected String getEntityId(Match entity) {
        return entity.getEntityId();
    }
    
    /**
     * Custom repository method to find all matches hosted by a specific user.
     * This demonstrates how to add custom queries to a specific repository.
     * @param hostId The ID of the host user.
     * @return LiveData holding a list of matches.
     */
    public LiveData<List<Match>> getMatchesByHostId(String hostId) {
        MutableLiveData<List<Match>> liveData = new MutableLiveData<>();
        
        collectionReference.whereEqualTo("hostUserId", hostId)
            .addSnapshotListener((snapshots, e) -> {
                if (e != null) {
                    // Handle error (e.g., log it)
                    liveData.setValue(null);
                    return;
                }
                if (snapshots != null) {
                    liveData.setValue(snapshots.toObjects(Match.class));
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