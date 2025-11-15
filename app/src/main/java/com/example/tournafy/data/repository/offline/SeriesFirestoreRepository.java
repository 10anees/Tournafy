// Path: app/src/main/java/com/tournafy/data/repository/offline/SeriesFirestoreRepository.java
package com.example.tournafy.data.repository.offline;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.tournafy.domain.models.series.Series; // Uses the Series model
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Firestore repository implementation for Series entities
 * This class handles all data operations for the Series model.
 */
@Singleton
public class SeriesFirestoreRepository extends FirestoreRepository<Series> {

    public static final String COLLECTION_PATH = "series";

    @Inject
    public SeriesFirestoreRepository(FirebaseFirestore firestoreInstance) {
        super(firestoreInstance, COLLECTION_PATH, Series.class);
    }

    @Override
    protected String getEntityId(Series entity) {
        return entity.getEntityId();
    }

    @Override
    public com.google.android.gms.tasks.Task<Void> add(Series entity) {
        // If the entity doesn't have an ID, create one.
        if (entity.getEntityId() == null || entity.getEntityId().isEmpty()) {
            String newId = collectionReference.document().getId();
            entity.setEntityId(newId);
        }
        return addOrUpdateWithId(entity.getEntityId(), entity);
    }

    /**
     * Custom repository method to find all series hosted by a specific user.
     * @param hostId The ID of the host user.
     * @return LiveData holding a list of series.
     */
    public LiveData<List<Series>> getSeriesByHostId(String hostId) {
        MutableLiveData<List<Series>> liveData = new MutableLiveData<>();
        
        collectionReference.whereEqualTo("hostUserId", hostId)
            .addSnapshotListener((snapshots, e) -> {
                if (e != null) {
                    liveData.setValue(null);
                    return;
                }
                if (snapshots != null) {
                    liveData.setValue(snapshots.toObjects(Series.class));
                }
            });
            
        return liveData;
    }
}