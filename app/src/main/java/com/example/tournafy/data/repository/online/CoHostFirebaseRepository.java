package com.example.tournafy.data.repository.online;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.example.tournafy.domain.models.user.CoHost;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Firebase Realtime Database repository for ONLINE CoHost entities.
 * This handles cloud storage of co-host relationships with real-time updates.
 */
@Singleton
public class CoHostFirebaseRepository extends FirebaseRepository<CoHost> {

    public static final String DATABASE_PATH = "co_hosts";

    @Inject
    public CoHostFirebaseRepository(FirebaseDatabase firebaseDatabase) {
        super(firebaseDatabase, DATABASE_PATH, CoHost.class);
    }

    @Override
    protected String getEntityId(CoHost entity) {
        return entity.getEntityId();
    }

    @Override
    public Task<Void> add(CoHost entity) {
        if (entity.getEntityId() == null || entity.getEntityId().isEmpty()) {
            String newId = databaseReference.push().getKey();
            entity.setEntityId(newId);
        }
        return addOrUpdateWithId(entity.getEntityId(), entity);
    }

    /**
     * Gets all co-hosts for a specific hosted entity (match, tournament, or series).
     * @param hostedEntityId The hosted entity ID.
     * @return LiveData holding a list of co-hosts.
     */
    public LiveData<List<CoHost>> getCoHostsByEntityId(String hostedEntityId) {
        MutableLiveData<List<CoHost>> liveData = new MutableLiveData<>();
        
        Query query = databaseReference.orderByChild("hostedEntityId").equalTo(hostedEntityId);
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<CoHost> coHosts = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    CoHost coHost = childSnapshot.getValue(CoHost.class);
                    if (coHost != null) {
                        coHosts.add(coHost);
                    }
                }
                liveData.setValue(coHosts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                liveData.setValue(null);
            }
        });
        
        return liveData;
    }

    /**
     * Gets all entities where a user is a co-host.
     * @param userId The user ID.
     * @return LiveData holding a list of co-host relationships.
     */
    public LiveData<List<CoHost>> getCoHostsByUserId(String userId) {
        MutableLiveData<List<CoHost>> liveData = new MutableLiveData<>();
        
        Query query = databaseReference.orderByChild("coHostUserId").equalTo(userId);
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<CoHost> coHosts = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    CoHost coHost = childSnapshot.getValue(CoHost.class);
                    if (coHost != null) {
                        coHosts.add(coHost);
                    }
                }
                liveData.setValue(coHosts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                liveData.setValue(null);
            }
        });
        
        return liveData;
    }

    /**
     * Checks if a user is a co-host for a specific entity.
     * @param hostedEntityId The hosted entity ID.
     * @param userId The user ID.
     * @return Task holding true if user is a co-host, false otherwise.
     */
    public Task<Boolean> isCoHost(String hostedEntityId, String userId) {
        Query query = databaseReference.orderByChild("hostedEntityId").equalTo(hostedEntityId);
        
        return query.get().continueWith(task -> {
            if (task.isSuccessful()) {
                for (DataSnapshot childSnapshot : task.getResult().getChildren()) {
                    CoHost coHost = childSnapshot.getValue(CoHost.class);
                    if (coHost != null && userId.equals(coHost.getCoHostUserId())) {
                        return true;
                    }
                }
            }
            return false;
        });
    }

    /**
     * Removes a co-host relationship.
     * @param hostedEntityId The hosted entity ID.
     * @param userId The user ID.
     * @return Task that completes when operation finishes.
     */
    public Task<Void> removeCoHost(String hostedEntityId, String userId) {
        Query query = databaseReference.orderByChild("hostedEntityId").equalTo(hostedEntityId);
        
        return query.get().continueWithTask(task -> {
            if (task.isSuccessful()) {
                for (DataSnapshot childSnapshot : task.getResult().getChildren()) {
                    CoHost coHost = childSnapshot.getValue(CoHost.class);
                    if (coHost != null && userId.equals(coHost.getCoHostUserId())) {
                        return delete(childSnapshot.getKey());
                    }
                }
            }
            return Tasks.forResult(null);
        });
    }

    /**
     * Updates the permission level for a co-host.
     * @param coHostId The co-host ID.
     * @param permissionLevel The new permission level.
     * @return Task that completes when operation finishes.
     */
    public Task<Void> updatePermissionLevel(String coHostId, String permissionLevel) {
        // For Firebase Realtime DB, we access the specific field path
        return databaseReference.child(coHostId).child("permissionLevel").setValue(permissionLevel);
    }
}