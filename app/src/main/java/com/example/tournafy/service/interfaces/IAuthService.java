package com.example.tournafy.service.interfaces;

import androidx.lifecycle.LiveData;
import com.example.tournafy.domain.models.user.User;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.AuthCredential;

/**
 * Service interface for handling user authentication (Singleton Pattern).
 * Defines the contract for all auth operations like login, signup, and
 * session management.
 */
public interface IAuthService {

    /**
     * Retrieves the currently authenticated user's data.
     *
     * @return LiveData wrapping the User object, or null if not logged in.
     */
    LiveData<User> getCurrentUser();

    /**
     * Signs a user in using their email and password.
     *
     * @param email    The user's email.
     * @param password The user's password.
     * @param <T>      A type parameter for the task result (e.g., AuthResult)
     */
    <T> void signInWithEmail(String email, String password, AuthCallback<T> callback);

    /**
     * Creates a new user account with email and password.
     *
     * @param email    The new user's email.
     * @param password The new user's password.
     * @param username The new user's display name.
     * @param <T>      A type parameter for the task result
     */
    <T> void signUpWithEmail(String email, String password, String username, AuthCallback<T> callback);

    /**
     * Handles sign-in using a Google account credential.
     *
     * @param credential The Google Auth credential.
     * @param <T>        A type parameter for the task result
     */
    <T> void signInWithGoogle(AuthCredential credential, AuthCallback<T> callback);

    /**
     * Signs the current user out.
     */
    void signOut();

    /**
     * Checks if a user is currently authenticated.
     *
     * @return true if a user is logged in, false otherwise.
     */
    boolean isUserAuthenticated();

    /**
     * Gets the ID of the currently logged-in user.
     *
     * @return String user ID, or null if not authenticated.
     */
    String getCurrentUserId();

    /**
     * A generic callback interface for asynchronous authentication operations.
     *
     * @param <T> The type of the successful result.
     */
    interface AuthCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }
}