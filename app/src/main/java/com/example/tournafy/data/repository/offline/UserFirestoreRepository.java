package com.example.tournafy.data.repository.offline;

import com.google.firebase.firestore.FirebaseFirestore;
import com.example.tournafy.domain.models.user.User; // Uses your provided User model
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Firestore repository implementation for User entities.
 * This class handles all data operations for the User model,
 * using the generic FirestoreRepository as a base.
 */
@Singleton
public class UserFirestoreRepository extends FirestoreRepository<User> {

    public static final String COLLECTION_PATH = "users";

    /**
     * Injects the global FirebaseFirestore instance (provided by Hilt).
     * @param firestoreInstance The Firestore database instance.
     */
    @Inject
    public UserFirestoreRepository(FirebaseFirestore firestoreInstance) {
        super(firestoreInstance, COLLECTION_PATH, User.class);
    }

    @Override
    protected String getEntityId(User entity) {
        return entity.getUserId();
    }
    
    @Override
    public com.google.android.gms.tasks.Task<Void> add(User entity) {
        if (entity.getUserId() == null || entity.getUserId().isEmpty()) {
             return com.google.android.gms.tasks.Tasks.forException(
                new IllegalArgumentException("User ID cannot be null when adding a new User.")
            );
        }
        // Use the addOrUpdateWithId method from the parent to set a specific doc ID
        return addOrUpdateWithId(entity.getUserId(), entity);
    }
}