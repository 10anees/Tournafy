// Path: app/src/main/java/com/tournafy/data/repository/offline/PlayerStatisticsFirestoreRepository.java
package com.example.tournafy.data.repository.offline;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.tasks.Task; // Import Task
import com.google.firebase.firestore.FieldValue; // Import FieldValue
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.tournafy.domain.models.statistics.PlayerStatistics;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PlayerStatisticsFirestoreRepository extends FirestoreRepository<PlayerStatistics> {

    public static final String COLLECTION_PATH = "player_statistics";

    @Inject
    public PlayerStatisticsFirestoreRepository(FirebaseFirestore firestoreInstance) {
        super(firestoreInstance, COLLECTION_PATH, PlayerStatistics.class);
    }

    @Override
    protected String getEntityId(PlayerStatistics entity) {
        return entity.getStatId(); // Assuming EERD 'stat_id' maps to 'statId'
    }

    @Override
    public com.google.android.gms.tasks.Task<Void> add(PlayerStatistics entity) {
        if (entity.getStatId() == null || entity.getStatId().isEmpty()) {
            String newId = collectionReference.document().getId();
            entity.setStatId(newId);
        }
        return addOrUpdateWithId(entity.getStatId(), entity);
    }
    
    /**
     * Custom method to get all player stats for a specific entity (like a tournament).
     * @param entityId The ID of the tournament/series.
     * @return LiveData holding a list of stats.
     */
    public LiveData<List<PlayerStatistics>> getStatsForEntity(String entityId) {
        MutableLiveData<List<PlayerStatistics>> liveData = new MutableLiveData<>();
        
        collectionReference.whereEqualTo("entityId", entityId)
            .addSnapshotListener((snapshots, e) -> {
                if (e != null) {
                    liveData.setValue(null);
                    return;
                }
                if (snapshots != null) {
                    liveData.setValue(snapshots.toObjects(PlayerStatistics.class));
                }
            });
            
        return liveData;
    }
    
    /**
     * * @param statsId The ID of the PlayerStatistics document.
     * @param fieldName The field to increment (e.g., "runs", "goals").
     * @param incrementBy The amount to add.
     * @return Task that completes when operation finishes.
     */
    public Task<Void> incrementStat(String statsId, String fieldName, long incrementBy) {
        // This assumes your stats are in a map called "stats"
        // e.g., { "runs": 10, "wickets": 2 }
        String fieldPath = "stats." + fieldName;
        
        return collectionReference.document(statsId).update(
            fieldPath, FieldValue.increment(incrementBy)
        );
    }
}