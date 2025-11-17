package com.example.tournafy.service.observers;

import com.example.tournafy.domain.interfaces.MatchObserver;
import com.example.tournafy.domain.interfaces.SeriesObserver;
import com.example.tournafy.domain.interfaces.TournamentObserver;
import com.example.tournafy.domain.models.base.Match;
import com.example.tournafy.domain.models.base.MatchEvent;
import com.example.tournafy.domain.models.series.Series;
import com.example.tournafy.domain.models.tournament.Tournament;
import com.example.tournafy.ui.viewmodels.MatchViewModel;
import com.example.tournafy.ui.viewmodels.SeriesViewModel;
import com.example.tournafy.ui.viewmodels.TournamentViewModel;

/**
 * Implements the Observer patterns to update ViewModels in real-time.
 */
public class UIObserver implements MatchObserver, TournamentObserver, SeriesObserver {

    private MatchViewModel matchViewModel;
    private TournamentViewModel tournamentViewModel;
    private SeriesViewModel seriesViewModel;

    public UIObserver(MatchViewModel matchViewModel) {
        this.matchViewModel = matchViewModel;
    }

    public UIObserver(TournamentViewModel tournamentViewModel) {
        this.tournamentViewModel = tournamentViewModel;
    }

    public UIObserver(SeriesViewModel seriesViewModel) {
        this.seriesViewModel = seriesViewModel;
    }

    public UIObserver(MatchViewModel matchViewModel, TournamentViewModel tournamentViewModel, SeriesViewModel seriesViewModel) {
        this.matchViewModel = matchViewModel;
        this.tournamentViewModel = tournamentViewModel;
        this.seriesViewModel = seriesViewModel;
    }

    // --- MatchObserver Implementation ---

    @Override
    public void onMatchUpdated(Match match) {
        // LiveData updates automatically via Repository
    }

    @Override
    public void onEventAdded(MatchEvent event) {
        // LiveData updates automatically via Repository
    }

    @Override
    public void onMatchStatusChanged(String newStatus) {
        // LiveData updates automatically via Repository
    }

    // --- TournamentObserver Implementation ---

    @Override
    public void onStandingsUpdated(Tournament tournament) {
        // LiveData updates automatically via Repository
    }

    @Override
    public void onMatchCompleted(String completedMatchId) {
        if (tournamentViewModel != null) {
            tournamentViewModel.onMatchCompleted(completedMatchId);
        }
    }

    // --- SeriesObserver Implementation ---

    @Override
    public void onSeriesScoreUpdated(Series series) {
        if (seriesViewModel != null) {
            // FIX: Changed 'updateSeries' to 'updateOfflineSeries' to match SeriesViewModel
            seriesViewModel.updateOfflineSeries(series);
        }
    }
}