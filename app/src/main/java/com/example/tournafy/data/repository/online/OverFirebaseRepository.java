package com.example.tournafy.data.repository.online;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.example.tournafy.domain.models.match.cricket.Over;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Firebase Realtime Database repository for ONLINE Over entities.
 * This is used for storing cricket over data in online matches.
 *
 * Note: This is different from OverFirestoreRepository (offline).
 * - Uses Firebase Realtime Database for cloud storage
 * - No offline persistence
 * - Optimized for real-time updates
 * - Enables live ball-by-ball updates
 */
@Singleton
public class OverFirebaseRepository extends FirebaseRepository<Over> {

    public static final String DATABASE_PATH = "overs";

    @Inject
    public OverFirebaseRepository(FirebaseDatabase firebaseDatabase) {
        super(firebaseDatabase, DATABASE_PATH, Over.class);
    }

    @Override
    protected String getEntityId(Over entity) {
        return entity.getOverId();
    }

    /**
     * Overrides the generic 'add' method to ensure an Over ID is set.
     */
    @Override
    public Task<Void> add(Over entity) {
        // If the entity doesn't have an ID, create one.
        if (entity.getOverId() == null || entity.getOverId().isEmpty()) {
            String newId = databaseReference.push().getKey();
            entity.setOverId(newId);
        }
        return addOrUpdateWithId(entity.getOverId(), entity);
    }

    /**
     * Gets all overs for a specific innings.
     * @param inningsId The innings ID.
     * @return LiveData holding a list of overs.
     */
    public LiveData<List<Over>> getOversByInningsId(String inningsId) {
        MutableLiveData<List<Over>> liveData = new MutableLiveData<>();
        
        Query query = databaseReference.orderByChild("inningsId").equalTo(inningsId);
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Over> overs = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Over over = childSnapshot.getValue(Over.class);
                    if (over != null) {
                        overs.add(over);
                    }
                }
                liveData.setValue(overs);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                liveData.setValue(null);
            }
        });
        
        return liveData;
    }

    /**
     * Gets a specific over by innings ID and over number.
     * @param inningsId The innings ID.
     * @param overNumber The over number.
     * @return LiveData holding the over.
     */
    public LiveData<Over> getOverByNumber(String inningsId, int overNumber) {
        MutableLiveData<Over> liveData = new MutableLiveData<>();
        
        Query query = databaseReference.orderByChild("inningsId").equalTo(inningsId);
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Over over = childSnapshot.getValue(Over.class);
                    if (over != null && over.getOverNumber() == overNumber) {
                        liveData.setValue(over);
                        return;
                    }
                }
                liveData.setValue(null); // No over found
            }

            @Override
            public void onCancelled(DatabaseError error) {
                liveData.setValue(null);
            }
        });
        
        return liveData;
    }

    /**
     * Gets the current (active) over for an innings.
     * @param inningsId The innings ID.
     * @return LiveData holding the current over.
     */
    public LiveData<Over> getCurrentOver(String inningsId) {
        MutableLiveData<Over> liveData = new MutableLiveData<>();
        
        Query query = databaseReference.orderByChild("inningsId").equalTo(inningsId);
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Over latestOver = null;
                int maxOverNumber = -1;
                
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Over over = childSnapshot.getValue(Over.class);
                    if (over != null && !over.isCompleted() && over.getOverNumber() > maxOverNumber) {
                        latestOver = over;
                        maxOverNumber = over.getOverNumber();
                    }
                }
                liveData.setValue(latestOver);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                liveData.setValue(null);
            }
        });
        
        return liveData;
    }

    /**
     * Updates the over's run total.
     * @param overId The over ID.
     * @param runs The total runs scored in the over.
     * @return Task that completes when operation finishes.
     */
    public Task<Void> updateOverRuns(String overId, int runs) {
        return updateField(overId, "totalRuns", runs);
    }

    /**
     * Updates the number of legal deliveries in the over.
     * @param overId The over ID.
     * @param legalDeliveries The count of legal deliveries.
     * @return Task that completes when operation finishes.
     */
    public Task<Void> updateLegalDeliveries(String overId, int legalDeliveries) {
        return updateField(overId, "legalDeliveries", legalDeliveries);
    }

    /**
     * Marks an over as completed.
     * @param overId The over ID.
     * @return Task that completes when operation finishes.
     */
    public Task<Void> completeOver(String overId) {
        return updateField(overId, "isCompleted", true);
    }

    /**
     * Updates both runs and legal deliveries for an over.
     * More efficient than two separate calls.
     * @param overId The over ID.
     * @param runs The total runs scored.
     * @param legalDeliveries The count of legal deliveries.
     * @return Task that completes when operation finishes.
     */
    public Task<Void> updateOverProgress(String overId, int runs, int legalDeliveries) {
        return databaseReference.child(overId).updateChildren(
            new java.util.HashMap<String, Object>() {{
                put("totalRuns", runs);
                put("legalDeliveries", legalDeliveries);
            }}
        );
    }
}
