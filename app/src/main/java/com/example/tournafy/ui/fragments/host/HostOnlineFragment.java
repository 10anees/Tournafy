package com.example.tournafy.ui.fragments.host;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.tournafy.R;
import com.example.tournafy.ui.viewmodels.AuthViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HostOnlineFragment extends Fragment {

    private static final int RC_SIGN_IN = 9001;

    private AuthViewModel authViewModel;
    private GoogleSignInClient googleSignInClient;

    // UI Views
    private Group grpUnauthenticated, grpAuthenticated;
    private ProgressBar progressBar;
    private MaterialButton btnSignInGoogle;
    private TextView tvWelcome;
    private MaterialCardView cardNewMatch, cardNewTournament; // cardNewSeries commented out

    public HostOnlineFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Ensure this ID is in strings.xml or google-services.json
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_host_online, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        // Bind Views
        grpUnauthenticated = view.findViewById(R.id.grpUnauthenticated);
        grpAuthenticated = view.findViewById(R.id.grpAuthenticated);
        progressBar = view.findViewById(R.id.progressBar);
        btnSignInGoogle = view.findViewById(R.id.btnSignInGoogle);
        tvWelcome = view.findViewById(R.id.tvWelcome);
        cardNewMatch = view.findViewById(R.id.cardNewMatch);
        cardNewTournament = view.findViewById(R.id.cardNewTournament);
        // cardNewSeries = view.findViewById(R.id.cardNewSeries); // commented out for future use

        // Setup Listeners
        btnSignInGoogle.setOnClickListener(v -> signIn());
        setupNavigationListeners();

        // Observe Auth State
        observeViewModel();
    }

    private void setupNavigationListeners() {
        cardNewMatch.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_hostOnline_to_hostNewMatchFragment);
        });

        cardNewTournament.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            // Ensure this action exists in nav_graph
            navController.navigate(R.id.action_hostOnline_to_hostNewTournamentFragment);
        });

        // cardNewSeries.setOnClickListener(v -> {
        //     NavController navController = Navigation.findNavController(v);
        //     // Ensure this action exists in nav_graph
        //     navController.navigate(R.id.action_hostOnline_to_hostNewSeriesFragment);
        // });
    }

    private void observeViewModel() {
        // Observe User State
        authViewModel.user.observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                // User is Logged In
                grpUnauthenticated.setVisibility(View.GONE);
                grpAuthenticated.setVisibility(View.VISIBLE);
                tvWelcome.setText("Welcome, " + (user.getName() != null ? user.getName() : "Host"));
            } else {
                // User is NOT Logged In
                grpAuthenticated.setVisibility(View.GONE);
                grpUnauthenticated.setVisibility(View.VISIBLE);
            }
        });

        // Observe Loading State
        authViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnSignInGoogle.setEnabled(!isLoading);
            // Disable cards if loading
            cardNewMatch.setEnabled(!isLoading);
            cardNewTournament.setEnabled(!isLoading);
            // cardNewSeries.setEnabled(!isLoading); // commented out for future use
        });

        // Observe Error Messages
        authViewModel.errorMessage.observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                authViewModel.clearErrorMessage();
            }
        });
    }

    private void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                // Firebase Auth via ViewModel
                authViewModel.signInWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(getContext(), "Google Sign In Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}