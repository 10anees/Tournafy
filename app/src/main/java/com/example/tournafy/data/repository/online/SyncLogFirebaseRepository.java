package com.example.tournafy.data.repository.online;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.example.tournafy.domain.models.sync.SyncLog;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Firebase Realtime Database repository for ONLINE SyncLog entities.
 * This is used for tracking synchronization operations between offline and online storage.
 *
 * Note: This is different from SyncLogFirestoreRepository (offline).
 * - Uses Firebase Realtime Database for cloud storage
 * - Tracks sync operations for debugging and monitoring
 * - Enables sync status monitoring across devices
 */
@Singleton
public class SyncLogFirebaseRepository extends FirebaseRepository<SyncLog> {

    public static final String DATABASE_PATH = "sync_logs";

    @Inject
    public SyncLogFirebaseRepository(FirebaseDatabase firebaseDatabase) {
        super(firebaseDatabase, DATABASE_PATH, SyncLog.class);
    }

    @Override
    protected String getEntityId(SyncLog entity) {
        return entity.getEntityId();
    }

    /**
     * Overrides the generic 'add' method to ensure a SyncLog ID is set.
     */
    @Override
    public Task<Void> add(SyncLog entity) {
        // If the entity doesn't have an ID, create one.
        if (entity.getEntityId() == null || entity.getEntityId().isEmpty()) {
            String newId = databaseReference.push().getKey();
            entity.setEntityId(newId);
        }
        return addOrUpdateWithId(entity.getEntityId(), entity);
    }

    /**
     * Gets all sync logs for a specific entity.
     * @param entityId The entity ID that was synced.
     * @return LiveData holding a list of sync logs.
     */
    public LiveData<List<SyncLog>> getSyncLogsByEntityId(String entityId) {
        MutableLiveData<List<SyncLog>> liveData = new MutableLiveData<>();
        
        Query query = databaseReference.orderByChild("entityId").equalTo(entityId);
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<SyncLog> logs = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    SyncLog log = childSnapshot.getValue(SyncLog.class);
                    if (log != null) {
                        logs.add(log);
                    }
                }
                liveData.setValue(logs);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                liveData.setValue(null);
            }
        });
        
        return liveData;
    }

    /**
     * Gets all sync logs for a specific entity type.
     * @param entityType The type of entity (e.g., "MATCH", "TOURNAMENT", "SERIES").
     * @return LiveData holding a list of sync logs.
     */
    public LiveData<List<SyncLog>> getSyncLogsByEntityType(String entityType) {
        MutableLiveData<List<SyncLog>> liveData = new MutableLiveData<>();
        
        Query query = databaseReference.orderByChild("entityType").equalTo(entityType);
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<SyncLog> logs = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    SyncLog log = childSnapshot.getValue(SyncLog.class);
                    if (log != null) {
                        logs.add(log);
                    }
                }
                liveData.setValue(logs);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                liveData.setValue(null);
            }
        });
        
        return liveData;
    }

    /**
     * Gets all sync logs by status.
     * @param status The sync status (e.g., "SYNCED", "PENDING", "FAILED").
     * @return LiveData holding a list of sync logs.
     */
    public LiveData<List<SyncLog>> getSyncLogsByStatus(String status) {
        MutableLiveData<List<SyncLog>> liveData = new MutableLiveData<>();
        
        Query query = databaseReference.orderByChild("syncStatus").equalTo(status);
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<SyncLog> logs = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    SyncLog log = childSnapshot.getValue(SyncLog.class);
                    if (log != null) {
                        logs.add(log);
                    }
                }
                liveData.setValue(logs);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                liveData.setValue(null);
            }
        });
        
        return liveData;
    }

    /**
     * Gets all pending sync logs (status = "PENDING").
     * Useful for retry mechanisms.
     * @return LiveData holding a list of pending sync logs.
     */
    public LiveData<List<SyncLog>> getPendingSyncLogs() {
        return getSyncLogsByStatus("PENDING");
    }

    /**
     * Gets all failed sync logs (status = "FAILED").
     * Useful for debugging and error monitoring.
     * @return LiveData holding a list of failed sync logs.
     */
    public LiveData<List<SyncLog>> getFailedSyncLogs() {
        return getSyncLogsByStatus("FAILED");
    }

    /**
     * Gets sync logs for a specific user.
     * @param userId The user ID who initiated the sync.
     * @return LiveData holding a list of sync logs.
     */
    public LiveData<List<SyncLog>> getSyncLogsByUserId(String userId) {
        MutableLiveData<List<SyncLog>> liveData = new MutableLiveData<>();
        
        Query query = databaseReference.orderByChild("userId").equalTo(userId);
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<SyncLog> logs = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    SyncLog log = childSnapshot.getValue(SyncLog.class);
                    if (log != null) {
                        logs.add(log);
                    }
                }
                liveData.setValue(logs);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                liveData.setValue(null);
            }
        });
        
        return liveData;
    }

    /**
     * Gets recent sync logs (ordered by timestamp).
     * @param limit The maximum number of logs to retrieve.
     * @return LiveData holding a list of recent sync logs.
     */
    public LiveData<List<SyncLog>> getRecentSyncLogs(int limit) {
        MutableLiveData<List<SyncLog>> liveData = new MutableLiveData<>();
        
        Query query = databaseReference.orderByChild("syncTimestamp").limitToLast(limit);
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<SyncLog> logs = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    SyncLog log = childSnapshot.getValue(SyncLog.class);
                    if (log != null) {
                        logs.add(log);
                    }
                }
                liveData.setValue(logs);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                liveData.setValue(null);
            }
        });
        
        return liveData;
    }

    /**
     * Updates the sync status of a log.
     * @param logId The sync log ID.
     * @param newStatus The new status (e.g., "SYNCED", "FAILED").
     * @return Task that completes when operation finishes.
     */
    public Task<Void> updateSyncStatus(String logId, String newStatus) {
        return updateField(logId, "syncStatus", newStatus);
    }

    /**
     * Updates the error message of a failed sync log.
     * @param logId The sync log ID.
     * @param errorMessage The error message.
     * @return Task that completes when operation finishes.
     */
    public Task<Void> updateErrorMessage(String logId, String errorMessage) {
        return databaseReference.child(logId).updateChildren(
            new java.util.HashMap<String, Object>() {{
                put("syncStatus", "FAILED");
                put("errorMessage", errorMessage);
                put("lastAttemptTimestamp", System.currentTimeMillis());
            }}
        );
    }

    /**
     * Increments the retry count for a sync log using Firebase Transaction.
     * CRITICAL FIX: Uses runTransaction() to prevent race conditions when multiple
     * retry attempts happen simultaneously. This is atomic and guarantees consistency.
     * 
     * @param logId The sync log ID.
     * @return Task that completes when transaction finishes.
     */
    public Task<Void> incrementRetryCount(String logId) {
        // Use Firebase Transaction for atomic increment
        com.google.android.gms.tasks.TaskCompletionSource<Void> taskSource = 
            new com.google.android.gms.tasks.TaskCompletionSource<>();
        
        databaseReference.child(logId).child("retryCount")
            .runTransaction(new com.google.firebase.database.Transaction.Handler() {
                @Override
                public com.google.firebase.database.Transaction.Result doTransaction(
                        com.google.firebase.database.MutableData mutableData) {
                    Integer currentCount = mutableData.getValue(Integer.class);
                    if (currentCount == null) {
                        mutableData.setValue(1);
                    } else {
                        mutableData.setValue(currentCount + 1);
                    }
                    return com.google.firebase.database.Transaction.success(mutableData);
                }

                @Override
                public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {
                    if (error != null) {
                        taskSource.setException(error.toException());
                    } else if (!committed) {
                        taskSource.setException(new Exception("Transaction not committed"));
                    } else {
                        taskSource.setResult(null);
                    }
                }
            });
        
        return taskSource.getTask();
    }
}
