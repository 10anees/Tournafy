package com.example.tournafy.service.interfaces;

import android.content.Context;
import android.content.Intent;
import androidx.lifecycle.LiveData;
import com.example.tournafy.domain.models.user.User;

/**
 * Defines the contract for all authentication operations,
 * supporting Google, Facebook, and Email providers.
 * This will be implemented as a Singleton.
 */
public interface IAuthService {

    /**
     * @return LiveData<User>
     */
    LiveData<User> getCurrentUser();

    /**
     * @param context The activity or fragment context.
     * @return An Intent to be launched with registerForActivityResult.
     */
    Intent getGoogleSignInIntent(Context context);

    /**
     * @param data The Intent data from onActivityResult.
     */
    void handleGoogleSignInResult(Intent data);

    /**
     * @param email    The user's email.
     * @param password The user's password.
     */
    void signInWithEmail(String email, String password);

    /**
     * @param email    The new user's email.
     * @param password The new user's password.
     * @param name     The new user's name.
     */
    void signUpWithEmail(String email, String password, String name);
    void signOut();
}