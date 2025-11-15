package com.example.tournafy.data.repository.offline;

import com.google.firebase.firestore.FirebaseFirestore;
import com.example.tournafy.domain.models.sync.SyncLog;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SyncLogFirestoreRepository extends FirestoreRepository<SyncLog> {

    public static final String COLLECTION_PATH = "sync_logs";

    @Inject
    public SyncLogFirestoreRepository(FirebaseFirestore firestoreInstance) {
        super(firestoreInstance, COLLECTION_PATH, SyncLog.class);
    }

    @Override
    protected String getEntityId(SyncLog entity) {
        return entity.getSyncId();
    }

    @Override
    public com.google.android.gms.tasks.Task<Void> add(SyncLog entity) {
        if (entity.getSyncId() == null || entity.getSyncId().isEmpty()) {
            String newId = collectionReference.document().getId();
            entity.setSyncId(newId);
        }
        return addOrUpdateWithId(entity.getSyncId(), entity);
    }
}