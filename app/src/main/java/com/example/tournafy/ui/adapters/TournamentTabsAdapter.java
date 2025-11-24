package com.example.tournafy.ui.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.tournafy.ui.fragments.tournament.TournamentOverviewFragment;
import com.example.tournafy.ui.fragments.tournament.TournamentTableFragment;
import com.example.tournafy.ui.fragments.tournament.TournamentKnockoutFragment;
import com.example.tournafy.ui.fragments.tournament.TournamentMatchesFragment;
import com.example.tournafy.ui.fragments.tournament.TournamentTopPlayersFragment;

/**
 * ViewPager2 adapter for tournament tabs.
 * Displays 5 tabs: Overview, Table, Knockout, Matches, Top Players
 */
public class TournamentTabsAdapter extends FragmentStateAdapter {

    private final String tournamentId;
    private final boolean isOnline;

    public TournamentTabsAdapter(@NonNull FragmentActivity fragmentActivity, 
                                  String tournamentId, 
                                  boolean isOnline) {
        super(fragmentActivity);
        this.tournamentId = tournamentId;
        this.isOnline = isOnline;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return TournamentOverviewFragment.newInstance(tournamentId, isOnline);
            case 1:
                return TournamentTableFragment.newInstance(tournamentId, isOnline);
            case 2:
                return TournamentKnockoutFragment.newInstance(tournamentId, isOnline);
            case 3:
                return TournamentMatchesFragment.newInstance(tournamentId, isOnline);
            case 4:
                return TournamentTopPlayersFragment.newInstance(tournamentId, isOnline);
            default:
                return TournamentOverviewFragment.newInstance(tournamentId, isOnline);
        }
    }

    @Override
    public int getItemCount() {
        return 5; // 5 tabs
    }
}
