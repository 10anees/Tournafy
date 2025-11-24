package com.example.tournafy.ui.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.tournafy.R;
import com.example.tournafy.domain.models.tournament.Tournament;
import com.example.tournafy.ui.adapters.TournamentTabsAdapter;
import com.example.tournafy.ui.viewmodels.TournamentViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Activity for displaying tournament details with tabs:
 * - Overview (name, format, dates, status)
 * - Table/Standings (points table)
 * - Knockout (bracket visualization)
 * - Matches (list of all tournament matches)
 * - Top Players (statistics leaderboard)
 */
@AndroidEntryPoint
public class TournamentActivity extends AppCompatActivity {

    private static final String TAG = "TournamentActivity";
    public static final String EXTRA_TOURNAMENT_ID = "tournament_id";
    public static final String EXTRA_IS_ONLINE = "is_online";

    private TournamentViewModel tournamentViewModel;

    // UI Components
    private MaterialToolbar toolbar;
    private Chip chipStatus;
    private TextView tvTournamentName;
    private TextView tvSportType;
    private TextView tvTournamentType;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    private TournamentTabsAdapter tabsAdapter;
    private String tournamentId;
    private boolean isOnline = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tournament);

        // Initialize ViewModel
        tournamentViewModel = new ViewModelProvider(this).get(TournamentViewModel.class);

        initViews();
        setupToolbar();
        
        // Check if opened from deep link
        android.content.Intent intent = getIntent();
        android.net.Uri data = intent.getData();
        
        if (data != null) {
            // Deep link: https://tournafy.app/tournament/{visibilityLink}
            String visibilityLink = data.getLastPathSegment();
            Log.d(TAG, "Opened from deep link - Visibility Link: " + visibilityLink);
            
            if (visibilityLink != null && !visibilityLink.isEmpty()) {
                // Load online tournament by visibility link
                isOnline = true;
                // TODO: Load by visibility link
                // tournamentViewModel.loadOnlineTournamentByLink(visibilityLink);
                Toast.makeText(this, "Loading tournament...", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Opened from app with tournament ID
            tournamentId = intent.getStringExtra(EXTRA_TOURNAMENT_ID);
            isOnline = intent.getBooleanExtra(EXTRA_IS_ONLINE, false);
            
            if (tournamentId == null || tournamentId.isEmpty()) {
                Toast.makeText(this, "Invalid tournament", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            
            Log.d(TAG, "Loading tournament: " + tournamentId + " (online: " + isOnline + ")");
            
            if (isOnline) {
                tournamentViewModel.loadOnlineTournament(tournamentId);
            } else {
                tournamentViewModel.loadOfflineTournament(tournamentId);
            }
        }

        observeTournament();
        setupViewPager();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        chipStatus = findViewById(R.id.chipStatus);
        tvTournamentName = findViewById(R.id.tvTournamentName);
        tvSportType = findViewById(R.id.tvSportType);
        tvTournamentType = findViewById(R.id.tvTournamentType);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Tournament");
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupViewPager() {
        // Create adapter with tournament ID
        tabsAdapter = new TournamentTabsAdapter(this, tournamentId, isOnline);
        viewPager.setAdapter(tabsAdapter);

        // Link TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position) {
                    case 0:
                        tab.setText("Overview");
                        tab.setIcon(R.drawable.ic_info);
                        break;
                    case 1:
                        tab.setText("Table");
                        tab.setIcon(R.drawable.ic_table);
                        break;
                    case 2:
                        tab.setText("Knockout");
                        tab.setIcon(R.drawable.ic_bracket);
                        break;
                    case 3:
                        tab.setText("Matches");
                        tab.setIcon(R.drawable.ic_matches);
                        break;
                    case 4:
                        tab.setText("Top Players");
                        tab.setIcon(R.drawable.ic_star);
                        break;
                }
            }
        }).attach();
    }

    private void observeTournament() {
        // Observe the appropriate LiveData based on online/offline mode
        if (isOnline) {
            tournamentViewModel.onlineTournament.observe(this, this::updateUI);
        } else {
            tournamentViewModel.offlineTournament.observe(this, this::updateUI);
        }

        // Observe errors
        tournamentViewModel.errorMessage.observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                tournamentViewModel.clearErrorMessage();
            }
        });

        // Observe loading state
        tournamentViewModel.isLoading.observe(this, isLoading -> {
            // Show/hide loading indicator
            // TODO: Add progress bar to layout
        });
    }

    private void updateUI(Tournament tournament) {
        if (tournament == null) {
            Log.w(TAG, "Tournament is null");
            return;
        }

        Log.d(TAG, "Updating UI with tournament: " + tournament.getName());

        // Update toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(tournament.getName());
        }

        // Update tournament info
        tvTournamentName.setText(tournament.getName());
        tvSportType.setText(tournament.getSportId() != null ? tournament.getSportId() : "");
        tvTournamentType.setText("• " + formatTournamentType(tournament.getTournamentType()));

        // Update status chip
        updateStatusChip(tournament.getStatus());
    }

    private String formatTournamentType(String type) {
        if (type == null) return "Unknown";
        
        switch (type.toUpperCase()) {
            case "KNOCKOUT":
                return "Knockout Tournament";
            case "LEAGUE":
            case "ROUND ROBIN":
                return "League / Round Robin";
            case "MIXED":
                return "Group Stage + Knockout";
            default:
                return type;
        }
    }

    private void updateStatusChip(String status) {
        if (status == null) {
            chipStatus.setVisibility(View.GONE);
            return;
        }

        chipStatus.setVisibility(View.VISIBLE);
        
        switch (status.toUpperCase()) {
            case "DRAFT":
                chipStatus.setText("Draft");
                chipStatus.setChipBackgroundColorResource(R.color.status_draft);
                break;
            case "SCHEDULED":
                chipStatus.setText("Scheduled");
                chipStatus.setChipBackgroundColorResource(R.color.status_scheduled);
                break;
            case "IN_PROGRESS":
            case "ACTIVE":
            case "LIVE":
                chipStatus.setText("Active");
                chipStatus.setChipBackgroundColorResource(R.color.status_live);
                break;
            case "COMPLETED":
                chipStatus.setText("Completed");
                chipStatus.setChipBackgroundColorResource(R.color.status_completed);
                break;
            default:
                chipStatus.setText(status);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up
    }

    /**
     * Public method for fragments to get tournament ID
     */
    public String getTournamentId() {
        return tournamentId;
    }

    /**
     * Public method for fragments to check online/offline mode
     */
    public boolean isOnlineMode() {
        return isOnline;
    }
}
