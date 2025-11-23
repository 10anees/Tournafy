package com.example.tournafy.ui.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tournafy.data.repository.offline.MatchFirestoreRepository;
import com.example.tournafy.domain.models.base.Match;
import com.example.tournafy.domain.models.search.SearchResult;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * ViewModel for search functionality.
 * Searches for matches, tournaments, and series by code/ID.
 */
@HiltViewModel
public class SearchViewModel extends ViewModel {

    private final MatchFirestoreRepository matchRepository;
    
    private final MutableLiveData<List<SearchResult>> _searchResults = new MutableLiveData<>();
    public final LiveData<List<SearchResult>> searchResults = _searchResults;
    
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoading = _isLoading;
    
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public final LiveData<String> errorMessage = _errorMessage;
    
    private androidx.lifecycle.Observer<List<Match>> searchObserver;

    @Inject
    public SearchViewModel(MatchFirestoreRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    /**
     * Search for matches, tournaments, or series by code.
     * Searches match codes (visibilityLink) case-insensitively.
     * 
     * @param query The search query (match code)
     */
    public void search(String query) {
        if (query == null || query.trim().isEmpty()) {
            _errorMessage.setValue("Please enter a match code");
            return;
        }
        
        _isLoading.setValue(true);
        _errorMessage.setValue(null);
        _searchResults.setValue(new ArrayList<>());
        
        // Normalize query: uppercase and remove hyphens
        String normalizedQuery = query.trim().toUpperCase().replaceAll("-", "");
        
        android.util.Log.d("SearchViewModel", "Searching for: " + query + " (normalized: " + normalizedQuery + ")");
        
        // Remove previous observer if exists
        if (searchObserver != null) {
            matchRepository.getAll().removeObserver(searchObserver);
        }
        
        // Search matches
        searchObserver = new androidx.lifecycle.Observer<List<Match>>() {
            @Override
            public void onChanged(List<Match> matches) {
                List<SearchResult> results = new ArrayList<>();
                
                if (matches != null) {
                    for (Match match : matches) {
                        String visibilityLink = match.getVisibilityLink();
                        if (visibilityLink != null) {
                            String normalizedCode = visibilityLink.toUpperCase().replaceAll("-", "");
                            
                            // Check if code matches or name contains query
                            boolean codeMatches = normalizedCode.contains(normalizedQuery);
                            boolean nameMatches = match.getName().toUpperCase().contains(query.toUpperCase());
                            
                            if (codeMatches || nameMatches) {
                                SearchResult result = new SearchResult();
                                result.setId(match.getEntityId());
                                result.setType(SearchResult.TYPE_MATCH);
                                result.setTitle(match.getName());
                                result.setSubtitle(match.getVenue() != null ? match.getVenue() : "Match");
                                result.setStatus(match.getMatchStatus());
                                result.setCode(match.getVisibilityLink());
                                result.setSportId(match.getSportId());
                                results.add(result);
                                
                                android.util.Log.d("SearchViewModel", "Found match: " + match.getName() + 
                                    " (Code: " + visibilityLink + ")");
                            }
                        }
                    }
                }
                
                _searchResults.setValue(results);
                _isLoading.setValue(false);
                
                if (results.isEmpty()) {
                    _errorMessage.setValue("No matches found for \"" + query + "\"");
                    android.util.Log.d("SearchViewModel", "No results found");
                } else {
                    android.util.Log.d("SearchViewModel", "Found " + results.size() + " result(s)");
                }
                
                // Remove observer after search completes
                matchRepository.getAll().removeObserver(this);
                searchObserver = null;
            }
        };
        
        matchRepository.getAll().observeForever(searchObserver);
    }

    /**
     * Clear search results and error messages.
     */
    public void clearSearch() {
        _searchResults.setValue(new ArrayList<>());
        _errorMessage.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Clean up observer when ViewModel is destroyed
        if (searchObserver != null) {
            matchRepository.getAll().removeObserver(searchObserver);
        }
    }
}
