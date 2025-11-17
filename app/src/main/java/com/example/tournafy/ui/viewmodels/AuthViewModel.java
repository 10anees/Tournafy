package com.example.tournafy.ui.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tournafy.domain.models.user.User;
import com.example.tournafy.service.interfaces.IAuthService;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AuthViewModel extends ViewModel {

    private final IAuthService authService;

    // Using the Service's LiveData directly as the source of truth
    public final LiveData<User> user;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public final LiveData<String> errorMessage = _errorMessage;

    @Inject
    public AuthViewModel(IAuthService authService) {
        this.authService = authService;
        // Connect directly to the service's user stream
        this.user = authService.getCurrentUser();
    }

    public void signInWithGoogle(GoogleSignInAccount account) {
        _isLoading.setValue(true);
        // Convert Google Account to AuthCredential
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        authService.signInWithGoogle(credential, new IAuthService.AuthCallback<User>() {
            @Override
            public void onSuccess(User result) {
                // User LiveData will update automatically via the service
                _isLoading.setValue(false);
            }

            @Override
            public void onError(Exception e) {
                _errorMessage.setValue(e.getMessage());
                _isLoading.setValue(false);
            }
        });
    }

    public void signUpWithEmail(String email, String password, String username) {
        _isLoading.setValue(true);
        authService.signUpWithEmail(email, password, username, new IAuthService.AuthCallback<User>() {
            @Override
            public void onSuccess(User result) {
                _isLoading.setValue(false);
            }

            @Override
            public void onError(Exception e) {
                _errorMessage.setValue(e.getMessage());
                _isLoading.setValue(false);
            }
        });
    }

    public void loginWithEmail(String email, String password) {
        _isLoading.setValue(true);
        // Corrected method name: signInWithEmail
        authService.signInWithEmail(email, password, new IAuthService.AuthCallback<User>() {
            @Override
            public void onSuccess(User result) {
                _isLoading.setValue(false);
            }

            @Override
            public void onError(Exception e) {
                _errorMessage.setValue(e.getMessage());
                _isLoading.setValue(false);
            }
        });
    }

    public void logout() {
        authService.signOut();
    }

    public void clearErrorMessage() {
        _errorMessage.setValue(null);
    }
}