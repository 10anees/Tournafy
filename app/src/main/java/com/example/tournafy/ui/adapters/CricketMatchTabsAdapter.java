package com.example.tournafy.ui.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.tournafy.ui.fragments.match.cricket.CricketLineupsFragment;
import com.example.tournafy.ui.fragments.match.cricket.CricketScorecardFragment;

public class CricketMatchTabsAdapter extends FragmentStateAdapter {

    public CricketMatchTabsAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new CricketScorecardFragment();
            case 1:
                return new CricketLineupsFragment();
            default:
                return new CricketScorecardFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Scorecard and Lineups
    }
}
