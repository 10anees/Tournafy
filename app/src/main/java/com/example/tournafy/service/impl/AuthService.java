package com.example.tournafy.service.impl;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tournafy.domain.models.user.User;
import com.example.tournafy.service.interfaces.IAuthService;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

/**
 * Singleton implementation of the IAuthService.
 */
public class AuthService implements IAuthService {

    private static final String TAG = "AuthService";
    private static final String USERS_COLLECTION = "users";
    private static volatile AuthService instance;

    private final FirebaseAuth mAuth;
    private final FirebaseFirestore mStore;
    private final MutableLiveData<User> currentUserLiveData = new MutableLiveData<>();
    private User currentUserCache;

    private AuthService() {
        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();

        mAuth.addAuthStateListener(firebaseAuth -> {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser != null) {
                fetchUserDetails(firebaseUser.getUid());
            } else {
                currentUserCache = null;
                currentUserLiveData.postValue(null);
            }
        });
    }

    public static AuthService getInstance() {
        if (instance == null) {
            synchronized (AuthService.class) {
                if (instance == null) {
                    instance = new AuthService();
                }
            }
        }
        return instance;
    }

    @Override
    public LiveData<User> getCurrentUser() {
        return currentUserLiveData;
    }

    @Override
    public <T> void signInWithEmail(String email, String password, AuthCallback<T> callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Fetch User details immediately to return in callback
                        FirebaseUser fUser = mAuth.getCurrentUser();
                        if (fUser != null) {
                            User user = new User();
                            user.setUserId(fUser.getUid());
                            user.setEmail(fUser.getEmail());
                            user.setName(fUser.getDisplayName());
                            // In a real scenario, we'd fetch full details from Firestore here too
                            callback.onSuccess((T) user);
                        } else {
                            callback.onSuccess(null);
                        }
                    } else {
                        Log.e(TAG, "signInWithEmail:failure", task.getException());
                        callback.onError(task.getException());
                    }
                });
    }

    @Override
    public <T> void signUpWithEmail(String email, String password, String username, AuthCallback<T> callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser fUser = task.getResult().getUser();
                        if (fUser != null) {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .build();
                            fUser.updateProfile(profileUpdates);

                            User newUser = new User();
                            newUser.setUserId(fUser.getUid());
                            newUser.setEmail(fUser.getEmail());
                            newUser.setName(username);

                            // Save to Firestore and return USER object in callback
                            saveUserToFirestore(newUser, callback);
                        } else {
                            callback.onError(new Exception("FirebaseUser is null after creation."));
                        }
                    } else {
                        Log.e(TAG, "signUpWithEmail:failure", task.getException());
                        callback.onError(task.getException());
                    }
                });
    }

    @Override
    public <T> void signInWithGoogle(AuthCredential credential, AuthCallback<T> callback) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean isNewUser = task.getResult().getAdditionalUserInfo().isNewUser();
                        FirebaseUser fUser = task.getResult().getUser();

                        if (fUser == null) {
                            callback.onError(new Exception("FirebaseUser is null after Google sign-in."));
                            return;
                        }

                        if (isNewUser) {
                            User newUser = new User();
                            newUser.setUserId(fUser.getUid());
                            newUser.setEmail(fUser.getEmail());
                            newUser.setName(fUser.getDisplayName());
                            if (fUser.getPhotoUrl() != null) {
                                newUser.setProfilePicture(fUser.getPhotoUrl().toString());
                            }
                            // Save and return User object
                            saveUserToFirestore(newUser, callback);
                        } else {
                            // Existing user. Create User object from Auth data to return immediately.
                            User existingUser = new User();
                            existingUser.setUserId(fUser.getUid());
                            existingUser.setEmail(fUser.getEmail());
                            existingUser.setName(fUser.getDisplayName());
                            if (fUser.getPhotoUrl() != null) {
                                existingUser.setProfilePicture(fUser.getPhotoUrl().toString());
                            }

                            callback.onSuccess((T) existingUser);
                        }
                    } else {
                        Log.e(TAG, "signInWithGoogle:failure", task.getException());
                        callback.onError(task.getException());
                    }
                });
    }

    @Override
    public void signOut() {
        mAuth.signOut();
    }

    @Override
    public boolean isUserAuthenticated() {
        return mAuth.getCurrentUser() != null;
    }

    @Override
    public String getCurrentUserId() {
        FirebaseUser user = mAuth.getCurrentUser();
        return (user != null) ? user.getUid() : null;
    }

    // FIX: Changed signature to accept AuthCallback<T> and call onSuccess with the User object (T)
    private <T> void saveUserToFirestore(User user, AuthCallback<T> callback) {
        mStore.collection(USERS_COLLECTION).document(user.getUserId())
                .set(user, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    // Pass the User object back, cast to T
                    callback.onSuccess((T) user);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "saveUserToFirestore:failure", e);
                    callback.onError(e);
                });
    }

    private void fetchUserDetails(String userId) {
        if (currentUserCache != null && currentUserCache.getUserId().equals(userId)) {
            currentUserLiveData.postValue(currentUserCache);
            return;
        }

        DocumentReference userRef = mStore.collection(USERS_COLLECTION).document(userId);
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    User user = document.toObject(User.class);
                    currentUserCache = user;
                    currentUserLiveData.postValue(user);
                } else {
                    // Create basic user from Auth if Firestore is missing
                    FirebaseUser fUser = mAuth.getCurrentUser();
                    if (fUser != null) {
                        User basicUser = new User();
                        basicUser.setUserId(fUser.getUid());
                        basicUser.setEmail(fUser.getEmail());
                        currentUserCache = basicUser;
                        currentUserLiveData.postValue(basicUser);
                    } else {
                        currentUserCache = null;
                        currentUserLiveData.postValue(null);
                    }
                }
            } else {
                currentUserCache = null;
                currentUserLiveData.postValue(null);
            }
        });
    }
}