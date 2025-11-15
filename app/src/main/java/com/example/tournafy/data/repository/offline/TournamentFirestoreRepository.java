package com.example.tournafy.data.repository.offline;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.tournafy.domain.models.tournament.Tournament; // Uses the Tournament model
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Firestore repository implementation for Tournament entities
 * This class handles all data operations for the Tournament model.
 */
@Singleton
public class TournamentFirestoreRepository extends FirestoreRepository<Tournament> {

    public static final String COLLECTION_PATH = "tournaments";

    @Inject
    public TournamentFirestoreRepository(FirebaseFirestore firestoreInstance) {
        super(firestoreInstance, COLLECTION_PATH, Tournament.class);
    }

    @Override
    protected String getEntityId(Tournament entity) {
        return entity.getEntityId();
    }

    @Override
    public com.google.android.gms.tasks.Task<Void> add(Tournament entity) {
        // If the entity doesn't have an ID, create one.
        if (entity.getEntityId() == null || entity.getEntityId().isEmpty()) {
            String newId = collectionReference.document().getId();
            entity.setEntityId(newId);
        }
        return addOrUpdateWithId(entity.getEntityId(), entity);
    }

    /**
     * Custom repository method to find all tournaments hosted by a specific user.
     * @param hostId The ID of the host user.
     * @return LiveData holding a list of tournaments.
     */
    public LiveData<List<Tournament>> getTournamentsByHostId(String hostId) {
        MutableLiveData<List<Tournament>> liveData = new MutableLiveData<>();
        
        collectionReference.whereEqualTo("hostUserId", hostId)
            .addSnapshotListener((snapshots, e) -> {
                if (e != null) {
                    liveData.setValue(null);
                    return;
                }
                if (snapshots != null) {
                    liveData.setValue(snapshots.toObjects(Tournament.class));
                }
            });
            
        return liveData;
    }
}