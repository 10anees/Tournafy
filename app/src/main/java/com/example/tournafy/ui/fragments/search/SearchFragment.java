package com.example.tournafy.ui.fragments.search;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tournafy.R;
import com.example.tournafy.domain.models.search.SearchResult;
import com.example.tournafy.ui.activities.MatchActivity;
import com.example.tournafy.ui.adapters.SearchResultAdapter;
import com.example.tournafy.ui.viewmodels.SearchViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Fragment for searching matches, tournaments, and series by code.
 */
@AndroidEntryPoint
public class SearchFragment extends Fragment implements SearchResultAdapter.OnResultClickListener {
    
    private SearchViewModel searchViewModel;
    
    // UI Components
    private TextInputEditText etSearch;
    private MaterialButton btnSearch;
    private ProgressBar progressBar;
    private TextView tvError;
    private RecyclerView recyclerResults;
    
    private SearchResultAdapter adapter;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        searchViewModel = new ViewModelProvider(this).get(SearchViewModel.class);
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupRecyclerView();
        setupListeners();
        observeViewModel();
    }
    
    private void initViews(View view) {
        etSearch = view.findViewById(R.id.etSearch);
        btnSearch = view.findViewById(R.id.btnSearch);
        progressBar = view.findViewById(R.id.progressBar);
        tvError = view.findViewById(R.id.tvError);
        recyclerResults = view.findViewById(R.id.recyclerResults);
    }
    
    private void setupRecyclerView() {
        adapter = new SearchResultAdapter(this);
        recyclerResults.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerResults.setAdapter(adapter);
    }
    
    private void setupListeners() {
        // Search button click
        btnSearch.setOnClickListener(v -> performSearch());
        
        // Enter key on keyboard
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });
        
        // Clear error when user starts typing
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvError.setVisibility(View.GONE);
            }
            
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }
    
    private void observeViewModel() {
        // Observe search results
        searchViewModel.searchResults.observe(getViewLifecycleOwner(), results -> {
            if (results != null && !results.isEmpty()) {
                adapter.setResults(results);
                recyclerResults.setVisibility(View.VISIBLE);
                android.util.Log.d("SearchFragment", "Displaying " + results.size() + " result(s)");
            } else {
                recyclerResults.setVisibility(View.GONE);
            }
        });
        
        // Observe loading state
        searchViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null && isLoading) {
                progressBar.setVisibility(View.VISIBLE);
                btnSearch.setEnabled(false);
            } else {
                progressBar.setVisibility(View.GONE);
                btnSearch.setEnabled(true);
            }
        });
        
        // Observe errors
        searchViewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                tvError.setText(error);
                tvError.setVisibility(View.VISIBLE);
                recyclerResults.setVisibility(View.GONE);
            } else {
                tvError.setVisibility(View.GONE);
            }
        });
    }
    
    private void performSearch() {
        String query = etSearch.getText().toString().trim();
        
        if (query.isEmpty()) {
            tvError.setText("Please enter a match code");
            tvError.setVisibility(View.VISIBLE);
            return;
        }
        
        // Hide keyboard
        android.view.inputmethod.InputMethodManager imm = 
            (android.view.inputmethod.InputMethodManager) requireActivity()
                .getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
        }
        
        // Perform search
        android.util.Log.d("SearchFragment", "Searching for: " + query);
        searchViewModel.search(query);
    }
    
    @Override
    public void onResultClick(SearchResult result) {
        android.util.Log.d("SearchFragment", "Result clicked: " + result.getTitle() + 
            " (Type: " + result.getType() + ", ID: " + result.getId() + ")");
        
        switch (result.getType()) {
            case SearchResult.TYPE_MATCH:
                openMatch(result.getId());
                break;
            case SearchResult.TYPE_TOURNAMENT:
                // TODO: Open tournament activity
                Toast.makeText(getContext(), "Opening tournament: " + result.getTitle(), 
                    Toast.LENGTH_SHORT).show();
                break;
            case SearchResult.TYPE_SERIES:
                // TODO: Open series activity
                Toast.makeText(getContext(), "Opening series: " + result.getTitle(), 
                    Toast.LENGTH_SHORT).show();
                break;
        }
    }
    
    private void openMatch(String matchId) {
        Intent intent = new Intent(getContext(), MatchActivity.class);
        intent.putExtra(MatchActivity.EXTRA_MATCH_ID, matchId);
        startActivity(intent);
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        searchViewModel.clearSearch();
    }
}
