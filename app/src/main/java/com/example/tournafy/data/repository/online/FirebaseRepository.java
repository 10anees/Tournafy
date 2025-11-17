package com.example.tournafy.data.repository.online;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.tournafy.data.repository.interfaces.IRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for Firebase Realtime Database repositories (ONLINE storage).
 * This differs from FirestoreRepository (offline) in that it uses Firebase Realtime Database
 * for online-only data that can be shared via links and viewed in real-time by multiple users.
 *
 * Key differences from FirestoreRepository:
 * - Uses Firebase Realtime Database instead of Cloud Firestore
 * - No offline persistence by default
 * - Optimized for real-time updates and viewing
 * - Used when isOnline=true for hosted entities
 *
 * @param <T> The domain model type.
 */
public abstract class FirebaseRepository<T> implements IRepository<T, String> {

    protected final DatabaseReference databaseReference;
    private final Class<T> modelClass;

    /**
     * @param firebaseDatabase The Firebase Realtime Database instance.
     * @param path The database path (e.g., "matches", "tournaments").
     * @param modelClass The class type for deserialization.
     */
    public FirebaseRepository(FirebaseDatabase firebaseDatabase, String path, Class<T> modelClass) {
        this.databaseReference = firebaseDatabase.getReference(path);
        this.modelClass = modelClass;
    }

    @Override
    public Task<Void> add(T entity) {
        String id = getEntityId(entity);
        if (id == null || id.isEmpty()) {
            // Generate a new ID using Firebase push()
            DatabaseReference newRef = databaseReference.push();
            id = newRef.getKey();
            // Note: Subclasses should set this ID on the entity
        }
        return databaseReference.child(id).setValue(entity);
    }

    /**
     * Helper method to add or update an entity with a specific ID.
     * @param id The entity ID.
     * @param entity The entity to store.
     * @return Task that completes when operation finishes.
     */
    public Task<Void> addOrUpdateWithId(String id, T entity) {
        return databaseReference.child(id).setValue(entity);
    }

    @Override
    public Task<Void> update(T entity) {
        String id = getEntityId(entity);
        if (id == null) {
            return com.google.android.gms.tasks.Tasks.forException(
                new IllegalArgumentException("Entity ID cannot be null for update.")
            );
        }
        return databaseReference.child(id).setValue(entity);
    }

    @Override
    public Task<Void> delete(String id) {
        return databaseReference.child(id).removeValue();
    }

    @Override
    public LiveData<T> getById(String id) {
        MutableLiveData<T> liveData = new MutableLiveData<>();
        
        databaseReference.child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    T entity = snapshot.getValue(modelClass);
                    liveData.setValue(entity);
                } else {
                    liveData.setValue(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle error (could log or set error state)
                liveData.setValue(null);
            }
        });
        
        return liveData;
    }

    @Override
    public LiveData<List<T>> getAll() {
        MutableLiveData<List<T>> liveData = new MutableLiveData<>();
        
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<T> entities = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    T entity = childSnapshot.getValue(modelClass);
                    if (entity != null) {
                        entities.add(entity);
                    }
                }
                liveData.setValue(entities);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle error
                liveData.setValue(null);
            }
        });
        
        return liveData;
    }

    /**
     * Gets a single-value snapshot (non-live) of an entity by ID.
     * Useful for one-time reads without setting up a listener.
     * @param id The entity ID.
     * @return Task containing the entity.
     */
    public Task<T> getByIdOnce(String id) {
        return databaseReference.child(id).get().continueWith(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                return task.getResult().getValue(modelClass);
            }
            return null;
        });
    }

    /**
     * Updates a specific field in the entity without replacing the entire object.
     * This is more efficient than updating the whole entity.
     * @param id The entity ID.
     * @param field The field name to update.
     * @param value The new value for the field.
     * @return Task that completes when operation finishes.
     */
    public Task<Void> updateField(String id, String field, Object value) {
        return databaseReference.child(id).child(field).setValue(value);
    }

    /**
     * Checks if an entity with the given ID exists.
     * @param id The entity ID.
     * @return Task containing true if exists, false otherwise.
     */
    public Task<Boolean> exists(String id) {
        return databaseReference.child(id).get().continueWith(task -> 
            task.isSuccessful() && task.getResult().exists()
        );
    }

    /**
     * Subclasses must implement this to extract the entity's ID.
     * @param entity The entity.
     * @return The entity's ID.
     */
    protected abstract String getEntityId(T entity);
}
