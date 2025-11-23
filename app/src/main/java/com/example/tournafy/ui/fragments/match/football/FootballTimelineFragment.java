package com.example.tournafy.ui.fragments.match.football;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tournafy.R;
import com.example.tournafy.domain.models.match.football.FootballEvent;
import com.example.tournafy.domain.models.match.football.FootballMatch;
import com.example.tournafy.ui.adapters.FootballEventAdapter;
import com.example.tournafy.ui.viewmodels.MatchViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * FootballTimelineFragment - Displays chronological event timeline for a football match.
 * 
 * Features:
 * - Chronological list of all match events (goals, cards, substitutions, etc.)
 * - Event icons and color coding by type
 * - Minute badges with match period
 * - Team identification for each event
 * - Score updates at time of event
 * - Empty state when no events
 * 
 * Uses FootballEventAdapter which already handles the visual display.
 */
@AndroidEntryPoint
public class FootballTimelineFragment extends Fragment {

    private MatchViewModel matchViewModel;
    private RecyclerView rvTimeline;
    private LinearLayout llEmptyTimeline;
    private FootballEventAdapter timelineAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_football_timeline, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        matchViewModel = new ViewModelProvider(requireActivity()).get(MatchViewModel.class);

        initViews(view);
        observeViewModel();
    }

    private void initViews(View view) {
        rvTimeline = view.findViewById(R.id.rvEventTimeline);
        llEmptyTimeline = view.findViewById(R.id.llEmptyTimeline);
        
        // Setup RecyclerView with adapter
        timelineAdapter = new FootballEventAdapter();
        rvTimeline.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTimeline.setAdapter(timelineAdapter);
    }

    private void observeViewModel() {
        matchViewModel.offlineMatch.observe(getViewLifecycleOwner(), match -> {
            if (match instanceof FootballMatch) {
                updateTimeline((FootballMatch) match);
            }
        });
    }

    /**
     * Updates the event timeline with all match events.
     * Events are displayed in chronological order (earliest to latest).
     */
    private void updateTimeline(FootballMatch match) {
        List<FootballEvent> events = match.getFootballEvents();
        
        if (events == null || events.isEmpty()) {
            // Show empty state
            rvTimeline.setVisibility(View.GONE);
            llEmptyTimeline.setVisibility(View.VISIBLE);
            return;
        }

        // Hide empty state
        rvTimeline.setVisibility(View.VISIBLE);
        llEmptyTimeline.setVisibility(View.GONE);

        // Sort events by match minute (chronological order)
        List<FootballEvent> sortedEvents = new ArrayList<>(events);
        Collections.sort(sortedEvents, (e1, e2) -> 
            Integer.compare(e1.getMatchMinute(), e2.getMatchMinute())
        );

        // Set team names for the adapter
        if (match.getTeams() != null && match.getTeams().size() >= 2) {
            timelineAdapter.setTeamNames(
                match.getTeams().get(0).getTeamName(),
                match.getTeams().get(1).getTeamName(),
                match.getTeams().get(0).getTeamId(),
                match.getTeams().get(1).getTeamId()
            );
        }

        // Update adapter with sorted events
        timelineAdapter.setEvents(sortedEvents);
    }
}
