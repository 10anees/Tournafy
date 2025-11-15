package com.example.tournafy.domain.interfaces;

import com.example.tournafy.domain.models.base.Match;
import com.example.tournafy.domain.models.base.MatchEvent;

/**
 * Used to notify listeners (like UI, StatisticsService, FirebaseSync)
 * of real-time events during a match.
 */
public interface MatchObserver {
    /**
     * @param match The updated Match object.
     */
    void onMatchUpdated(Match match);

    /**
     * @param event The new MatchEvent.
     */
    void onEventAdded(MatchEvent event);

    /**
     * @param newStatus The new status (e.g., from MatchStatus enum).
     */
    void onMatchStatusChanged(String newStatus);
}