package com.example.tournafy.ui.fragments.home;

import com.example.tournafy.ui.adapters.home.HomeAdapter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tournafy.R;
import com.example.tournafy.domain.models.base.HostedEntity;
import com.example.tournafy.domain.models.base.Match;
import com.example.tournafy.domain.models.series.Series;
import com.example.tournafy.domain.models.tournament.Tournament;
import com.example.tournafy.ui.viewmodels.HomeViewModel;
import com.example.tournafy.utils.ShareHelper;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends Fragment implements HomeAdapter.OnEntityClickListener {

    private HomeViewModel homeViewModel;
    private HomeAdapter adapter;
    private RecyclerView recyclerView;
    private TextView tvEmptyState;
    private ChipGroup chipGroupFilter;
    private ExtendedFloatingActionButton fabCreate;

    public HomeFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        recyclerView = view.findViewById(R.id.rvHostedEntities);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        chipGroupFilter = view.findViewById(R.id.chipGroupFilter);
        fabCreate = view.findViewById(R.id.fabCreate);

        setupRecyclerView();
        setupFilters();
        setupFab();
        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new HomeAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupFilters() {
        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chipAll) homeViewModel.setFilter(HomeViewModel.EntityTypeFilter.ALL);
            else if (id == R.id.chipMatches) homeViewModel.setFilter(HomeViewModel.EntityTypeFilter.MATCH);
            else if (id == R.id.chipTournaments) homeViewModel.setFilter(HomeViewModel.EntityTypeFilter.TOURNAMENT);
            // else if (id == R.id.chipSeries) homeViewModel.setFilter(HomeViewModel.EntityTypeFilter.SERIES); // commented out for now
        });
    }

    private void setupFab() {
        fabCreate.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_host_new, null, false);
            AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

            dialogView.findViewById(R.id.btnHostMatch).setOnClickListener(view -> {
                NavController nav = Navigation.findNavController(v);
                nav.navigate(R.id.action_homeFragment_to_hostNewMatchFragment);
                dialog.dismiss();
            });

            dialogView.findViewById(R.id.btnHostTournament).setOnClickListener(view -> {
                NavController nav = Navigation.findNavController(v);
                nav.navigate(R.id.action_home_to_hostNewTournamentFragment);
                dialog.dismiss();
            });

            // dialogView.findViewById(R.id.btnHostSeries).setOnClickListener(view -> {
            //     NavController nav = Navigation.findNavController(v);
            //     nav.navigate(R.id.action_home_to_hostNewSeriesFragment);
            //     dialog.dismiss();
            // });

            dialog.show();
        });
    }

    private void observeViewModel() {
        homeViewModel.hostedEntities.observe(getViewLifecycleOwner(), entities -> {
            if (entities == null || entities.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                tvEmptyState.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                tvEmptyState.setVisibility(View.GONE);
                adapter.submitList(entities);
            }
        });
    }

    @Override
    public void onEntityClick(HostedEntity entity) {
        NavController navController = Navigation.findNavController(requireView());
        Bundle args = new Bundle();
        
        if (entity instanceof Match) {
            Match match = (Match) entity;
            args.putString("match_id", entity.getEntityId());
            
            // Check if this is a tournament match that hasn't been configured yet
            boolean isTournamentMatch = match.getTournamentId() != null && !match.getTournamentId().isEmpty();
            boolean isScheduled = "SCHEDULED".equals(match.getMatchStatus());
            boolean hasConfig = match.getMatchConfig() != null;
            
            if (isTournamentMatch && isScheduled && !hasConfig) {
                // Navigate to AddMatchDetailsFragment to configure the match
                args.putBoolean("is_tournament_match", true);
                args.putString("tournament_id", match.getTournamentId());
                navController.navigate(R.id.action_homeFragment_to_addMatchDetailsFragment, args);
            } else {
                // Navigate to live score for configured or ongoing matches
                boolean isCricket = "CRICKET".equalsIgnoreCase(match.getSportId());
                if (isCricket) {
                    navController.navigate(R.id.action_home_to_cricketLiveScore, args);
                } else {
                    navController.navigate(R.id.action_home_to_footballLiveScore, args);
                }
            }
        } else if (entity instanceof Tournament) {
            // Navigate to TournamentActivity
            args.putString("tournament_id", entity.getEntityId());
            args.putBoolean("is_online", entity.isOnline());
            navController.navigate(R.id.action_home_to_tournamentActivity, args);
        } else if (entity instanceof Series) {
            Toast.makeText(getContext(), "Series Details coming soon", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onEntityLongClick(HostedEntity entity) {
        if (entity.isOnline()) {
            Toast.makeText(getContext(), "Already Online", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Syncing " + entity.getName() + " to Cloud...", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDeleteClick(HostedEntity entity) {
        new AlertDialog.Builder(requireContext())
            .setTitle("Delete Event")
            .setMessage("Are you sure you want to delete '" + entity.getName() + "'? This cannot be undone.")
            .setPositiveButton("Delete", (dialog, which) -> {
                // Call ViewModel to delete
                homeViewModel.deleteEntity(entity);
                Toast.makeText(getContext(), "Deleted", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public void onShareClick(HostedEntity entity) {
        // Check if user is logged in
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Please log in to share matches", Toast.LENGTH_SHORT).show();
            return;
        }

        // Only allow sharing matches (not tournaments or series)
        if (entity instanceof Match) {
            ShareHelper.shareMatch(requireContext(), (Match) entity);
        } else {
            Toast.makeText(getContext(), "Only matches can be shared", Toast.LENGTH_SHORT).show();
        }
    }
}