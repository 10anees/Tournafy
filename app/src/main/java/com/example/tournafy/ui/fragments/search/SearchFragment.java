package com.example.tournafy.ui.fragments.search;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.tournafy.R;
import com.example.tournafy.domain.models.base.Match;
import com.example.tournafy.domain.models.match.cricket.CricketMatch;
import com.example.tournafy.domain.models.match.football.FootballMatch;
import com.example.tournafy.ui.viewmodels.MatchViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SearchFragment extends Fragment {

    private MatchViewModel matchViewModel;
    
    // UI Components
    private TextInputEditText etSearch;
    private TextInputLayout tilSearch;
    private MaterialButton btnSearch;
    private ProgressBar progressBar;
    private TextView tvError;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Views
        etSearch = view.findViewById(R.id.etSearch);
        tilSearch = view.findViewById(R.id.tilSearch);
        btnSearch = view.findViewById(R.id.btnSearch);
        progressBar = view.findViewById(R.id.progressBar);
        tvError = view.findViewById(R.id.tvError);

        // Initialize ViewModel
        matchViewModel = new ViewModelProvider(requireActivity()).get(MatchViewModel.class);

        // Setup Observers
        setupObservers();

        // Setup Listeners
        btnSearch.setOnClickListener(v -> performSearch());
    }

    private void setupObservers() {
        // 1. Observe Loading State
        matchViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnSearch.setEnabled(!isLoading);
            tilSearch.setEnabled(!isLoading);
        });

        // 2. Observe Online Match Result
        matchViewModel.onlineMatch.observe(getViewLifecycleOwner(), match -> {
            if (match != null) {
                // Match Found!
                tvError.setVisibility(View.GONE);
                navigateToMatchDetails(match);
            } else {
                // If loading is finished but match is null, it might not exist
                // (Note: Real-time db returns null if path doesn't exist)
                if (Boolean.FALSE.equals(matchViewModel.isLoading.getValue())) {
                    showError("Match not found.");
                }
            }
        });
    }

    private void performSearch() {
        String query = etSearch.getText() != null ? etSearch.getText().toString().trim() : "";

        if (TextUtils.isEmpty(query)) {
            tilSearch.setError("Please enter an ID or Link");
            return;
        }

        tilSearch.setError(null);
        tvError.setVisibility(View.GONE);
        
        // In a real scenario, you'd parse the link to extract the ID. 
        // For now, we assume the user enters the ID directly.
        String entityId = extractIdFromInput(query);
        
        // Trigger the load in the ViewModel
        // This updates the _onlineMatchId MutableLiveData, triggering the repository call
        matchViewModel.loadOnlineMatch(entityId);
    }

    private String extractIdFromInput(String input) {
        // Logic to parse deep links or standard URLs would go here.
        // e.g. tournafy.com/match/12345 -> returns 12345
        return input; 
    }

    private void navigateToMatchDetails(Match match) {
        NavController navController = Navigation.findNavController(requireView());
        
        if (match instanceof CricketMatch) {
            // Navigate to Cricket Details
            // Note: You will need to create this navigation action in navigation graph later
            // NavDirections action = SearchFragmentDirections.actionSearchToCricketDetails(match.getEntityId());
            // navController.navigate(action);
            
            Toast.makeText(getContext(), "Found Cricket Match: " + match.getName(), Toast.LENGTH_SHORT).show();
        } else if (match instanceof FootballMatch) {
            // Navigate to Football Details
            Toast.makeText(getContext(), "Found Football Match: " + match.getName(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}