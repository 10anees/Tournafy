package com.example.tournafy.data.repository.offline;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.tournafy.domain.models.match.cricket.Over;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class OverFirestoreRepository extends FirestoreRepository<Over> {

    public static final String COLLECTION_PATH = "overs";

    @Inject
    public OverFirestoreRepository(FirebaseFirestore firestoreInstance) {
        super(firestoreInstance, COLLECTION_PATH, Over.class);
    }

    @Override
    protected String getEntityId(Over entity) {
        return entity.getOverId();
    }

    @Override
    public com.google.android.gms.tasks.Task<Void> add(Over entity) {
        if (entity.getOverId() == null || entity.getOverId().isEmpty()) {
            String newId = collectionReference.document().getId();
            entity.setOverId(newId);
        }
        return addOrUpdateWithId(entity.getOverId(), entity);
    }

    /**
     * Custom method to get all overs for a specific innings.
     * @param inningsId The ID of the innings.
     * @return LiveData holding a list of overs.
     */
    public LiveData<List<Over>> getOversForInnings(String inningsId) {
        MutableLiveData<List<Over>> liveData = new MutableLiveData<>();
        
        collectionReference.whereEqualTo("inningsId", inningsId)
            .orderBy("overNumber") // Order by 1, 2, 3...
            .addSnapshotListener((snapshots, e) -> {
                if (e != null) {
                    liveData.setValue(null);
                    return;
                }
                if (snapshots != null) {
                    liveData.setValue(snapshots.toObjects(Over.class));
                }
            });
            
        return liveData;
    }
}