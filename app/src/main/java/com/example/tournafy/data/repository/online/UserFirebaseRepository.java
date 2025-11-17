package com.example.tournafy.data.repository.online;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.example.tournafy.domain.models.user.User;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Firebase Realtime Database repository for ONLINE User entities.
 * This is used for online user profiles and data synchronization.
 *
 * Note: This is different from UserFirestoreRepository (offline).
 * - Uses Firebase Realtime Database for cloud storage
 * - No offline persistence
 * - Optimized for real-time updates
 * - Can be used for public profile viewing
 */
@Singleton
public class UserFirebaseRepository extends FirebaseRepository<User> {

    public static final String DATABASE_PATH = "users";

    @Inject
    public UserFirebaseRepository(FirebaseDatabase firebaseDatabase) {
        super(firebaseDatabase, DATABASE_PATH, User.class);
    }

    @Override
    protected String getEntityId(User entity) {
        return entity.getUserId();
    }

    /**
     * Overrides the generic 'add' method to ensure a User ID is set.
     */
    @Override
    public Task<Void> add(User entity) {
        // If the entity doesn't have an ID, create one.
        if (entity.getUserId() == null || entity.getUserId().isEmpty()) {
            String newId = databaseReference.push().getKey();
            entity.setUserId(newId);
        }
        return addOrUpdateWithId(entity.getUserId(), entity);
    }

    /**
     * Gets a user by their username.
     * @param username The username to search for.
     * @return LiveData holding the user.
     */
    public LiveData<User> getUserByUsername(String username) {
        MutableLiveData<User> liveData = new MutableLiveData<>();
        
        Query query = databaseReference.orderByChild("username").equalTo(username);
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    User user = childSnapshot.getValue(User.class);
                    liveData.setValue(user);
                    return; // Username should be unique
                }
                liveData.setValue(null); // No user found
            }

            @Override
            public void onCancelled(DatabaseError error) {
                liveData.setValue(null);
            }
        });
        
        return liveData;
    }

    /**
     * Gets a user by their email.
     * @param email The email to search for.
     * @return LiveData holding the user.
     */
    public LiveData<User> getUserByEmail(String email) {
        MutableLiveData<User> liveData = new MutableLiveData<>();
        
        Query query = databaseReference.orderByChild("email").equalTo(email);
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    User user = childSnapshot.getValue(User.class);
                    liveData.setValue(user);
                    return; // Email should be unique
                }
                liveData.setValue(null); // No user found
            }

            @Override
            public void onCancelled(DatabaseError error) {
                liveData.setValue(null);
            }
        });
        
        return liveData;
    }

    /**
     * Updates only the user's profile picture URL.
     * @param userId The user ID.
     * @param profilePicUrl The new profile picture URL.
     * @return Task that completes when operation finishes.
     */
    public Task<Void> updateProfilePicture(String userId, String profilePicUrl) {
        return updateField(userId, "profilePicUrl", profilePicUrl);
    }

    /**
     * Updates only the user's display name.
     * @param userId The user ID.
     * @param displayName The new display name.
     * @return Task that completes when operation finishes.
     */
    public Task<Void> updateDisplayName(String userId, String displayName) {
        return updateField(userId, "displayName", displayName);
    }

    /**
     * Updates the user's online status.
     * @param userId The user ID.
     * @param isOnline Whether the user is online.
     * @return Task that completes when operation finishes.
     */
    public Task<Void> updateOnlineStatus(String userId, boolean isOnline) {
        return updateField(userId, "isOnline", isOnline);
    }

    /**
     * Updates the user's last seen timestamp.
     * @param userId The user ID.
     * @param timestamp The timestamp (in milliseconds).
     * @return Task that completes when operation finishes.
     */
    public Task<Void> updateLastSeen(String userId, long timestamp) {
        return updateField(userId, "lastSeen", timestamp);
    }

    /**
     * Searches for users by a partial name match.
     * This can be used for user search functionality.
     * @param namePrefix The prefix to search for.
     * @return LiveData holding a list of matching users.
     */
    public LiveData<List<User>> searchUsersByName(String namePrefix) {
        MutableLiveData<List<User>> liveData = new MutableLiveData<>();
        
        // Firebase doesn't support "contains" queries directly, so we use startAt/endAt
        // This will match users whose displayName starts with the prefix
        Query query = databaseReference.orderByChild("displayName")
                .startAt(namePrefix)
                .endAt(namePrefix + "\uf8ff"); // \uf8ff is a high Unicode character
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<User> users = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    User user = childSnapshot.getValue(User.class);
                    if (user != null) {
                        users.add(user);
                    }
                }
                liveData.setValue(users);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                liveData.setValue(null);
            }
        });
        
        return liveData;
    }
}
