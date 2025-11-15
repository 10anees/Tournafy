package com.example.tournafy.data.repository.offline;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.tournafy.domain.models.match.cricket.Innings;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class InningsFirestoreRepository extends FirestoreRepository<Innings> {

    public static final String COLLECTION_PATH = "innings";

    @Inject
    public InningsFirestoreRepository(FirebaseFirestore firestoreInstance) {
        super(firestoreInstance, COLLECTION_PATH, Innings.class);
    }

    @Override
    protected String getEntityId(Innings entity) {
        return entity.getInningsId();
    }

    @Override
    public com.google.android.gms.tasks.Task<Void> add(Innings entity) {
        if (entity.getInningsId() == null || entity.getInningsId().isEmpty()) {
            String newId = collectionReference.document().getId();
            entity.setInningsId(newId);
        }
        return addOrUpdateWithId(entity.getInningsId(), entity);
    }
    
    /**
     * Custom method to get all innings for a specific match.
     * @param matchId The ID of the match.
     * @return LiveData holding a list of innings.
     */
    public LiveData<List<Innings>> getInningsForMatch(String matchId) {
        MutableLiveData<List<Innings>> liveData = new MutableLiveData<>();
        
        collectionReference.whereEqualTo("matchId", matchId)
            .orderBy("inningsNumber") // Order by 1st innings, then 2nd
            .addSnapshotListener((snapshots, e) -> {
                if (e != null) {
                    liveData.setValue(null);
                    return;
                }
                if (snapshots != null) {
                    liveData.setValue(snapshots.toObjects(Innings.class));
                }
            });
            
        return liveData;
    }
}