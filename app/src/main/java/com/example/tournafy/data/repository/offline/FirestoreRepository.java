// Path: app/src/main/java/com/tournafy/data/repository/offline/FirestoreRepository.java
package com.example.tournafy.data.repository.offline;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.tournafy.data.repository.interfaces.IRepository;
import java.util.List;

/**
 * @param <T> The domain model type.
 */
public abstract class FirestoreRepository<T> implements com.example.tournafy.data.repository.interfaces.IRepository<T, String> {

    protected final CollectionReference collectionReference;
    private final Class<T> modelClass;

    public FirestoreRepository(FirebaseFirestore firestoreInstance, String collectionPath, Class<T> modelClass) {
        this.collectionReference = firestoreInstance.collection(collectionPath);
        this.modelClass = modelClass;
    }

    @Override
    public Task<Void> add(T entity) {
        String id = getEntityId(entity);
        if (id == null || id.isEmpty()) {
            // If ID is missing, let Firestore generate one.
            // Note: This is usually handled by the specific repo override.
            return collectionReference.add(entity).continueWith(task -> null);
        }
        // Use the specific ID
        return collectionReference.document(id).set(entity);
    }
    
    public Task<Void> addOrUpdateWithId(String id, T entity) {
        return collectionReference.document(id).set(entity);
    }

    @Override
    public Task<Void> update(T entity) {
        String id = getEntityId(entity);
        if (id == null) {
            return com.google.android.gms.tasks.Tasks.forException(
                new IllegalArgumentException("Entity ID cannot be null for update.")
            );
        }
        return collectionReference.document(id).set(entity);
    }

    @Override
    public Task<Void> delete(String id) {
        return collectionReference.document(id).delete();
    }

    @Override
    public LiveData<T> getById(String id) {
        MutableLiveData<T> liveData = new MutableLiveData<>();
        collectionReference.document(id).addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                liveData.setValue(null);
                return;
            }
            if (snapshot != null && snapshot.exists()) {
                liveData.setValue(snapshot.toObject(modelClass));
            } else {
                liveData.setValue(null);
            }
        });
        return liveData;
    }

    @Override
    public LiveData<List<T>> getAll() {
        MutableLiveData<List<T>> liveData = new MutableLiveData<>();
        collectionReference.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                liveData.setValue(null);
                return;
            }
            if (snapshots != null) {
                liveData.setValue(snapshots.toObjects(modelClass));
            }
        });
        return liveData;
    }

    /**
     * @param entity The entity.
     * @return The entity's ID.
     */
    protected abstract String getEntityId(T entity);
}