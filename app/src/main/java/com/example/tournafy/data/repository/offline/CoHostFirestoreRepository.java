package com.example.tournafy.data.repository.offline;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.tournafy.domain.models.user.CoHost;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Firestore repository for OFFLINE CoHost entities.
 */
@Singleton
public class CoHostFirestoreRepository extends FirestoreRepository<CoHost> {

    public static final String COLLECTION_NAME = "co_hosts";

    @Inject
    public CoHostFirestoreRepository(FirebaseFirestore firestore) {
        super(firestore, COLLECTION_NAME, CoHost.class);
    }

    @Override
    protected String getEntityId(CoHost entity) {
        return entity.getEntityId();
    }

    public LiveData<List<CoHost>> getCoHostsByEntityId(String hostedEntityId) {
        MutableLiveData<List<CoHost>> liveData = new MutableLiveData<>();
        collectionReference.whereEqualTo("hostedEntityId", hostedEntityId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        liveData.setValue(null);
                        return;
                    }
                    if (snapshot != null) {
                        liveData.setValue(snapshot.toObjects(CoHost.class));
                    }
                });
        return liveData;
    }

    public LiveData<List<CoHost>> getCoHostsByUserId(String userId) {
        MutableLiveData<List<CoHost>> liveData = new MutableLiveData<>();
        collectionReference.whereEqualTo("coHostUserId", userId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        liveData.setValue(null);
                        return;
                    }
                    if (snapshot != null) {
                        liveData.setValue(snapshot.toObjects(CoHost.class));
                    }
                });
        return liveData;
    }

    public Task<Boolean> isCoHost(String hostedEntityId, String userId) {
        return collectionReference
                .whereEqualTo("hostedEntityId", hostedEntityId)
                .whereEqualTo("coHostUserId", userId)
                .limit(1)
                .get()
                .continueWith(task -> task.isSuccessful() && !task.getResult().isEmpty());
    }

    public Task<Void> removeCoHost(String hostedEntityId, String userId) {
        return collectionReference
                .whereEqualTo("hostedEntityId", hostedEntityId)
                .whereEqualTo("coHostUserId", userId)
                .limit(1)
                .get()
                .continueWithTask(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        String docId = task.getResult().getDocuments().get(0).getId();
                        return delete(docId);
                    }
                    return com.google.android.gms.tasks.Tasks.forResult(null);
                });
    }

    /**
     * Updates the permission level for a co-host.
     */
    public Task<Void> updatePermissionLevel(String coHostId, String permissionLevel) {
        // FIX: Use direct Firestore update call instead of missing updateField helper
        return collectionReference.document(coHostId).update("permissionLevel", permissionLevel);
    }
}