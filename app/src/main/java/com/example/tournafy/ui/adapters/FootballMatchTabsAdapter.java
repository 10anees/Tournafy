package com.example.tournafy.ui.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.tournafy.ui.fragments.match.football.FootballStatisticsFragment;
import com.example.tournafy.ui.fragments.match.football.FootballLineupsFragment;
import com.example.tournafy.ui.fragments.match.football.FootballTimelineFragment;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;

/**
 * FragmentStateAdapter for football match details tabs.
 * Manages 3 tabs: Statistics, Lineups, and Timeline.
 */
public class FootballMatchTabsAdapter extends FragmentStateAdapter {

    public FootballMatchTabsAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    public FootballMatchTabsAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new FootballStatisticsFragment();
            case 1:
                return new FootballLineupsFragment();
            case 2:
                return new FootballTimelineFragment();
            default:
                return new FootballStatisticsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Statistics, Lineups, Timeline
    }
}
