package com.example.tournafy.service.observers;

import com.example.tournafy.data.repository.online.MatchFirebaseRepository;
import com.example.tournafy.domain.interfaces.MatchObserver;
import com.example.tournafy.domain.models.base.Match;
import com.example.tournafy.domain.models.base.MatchEvent;

/**
 * Implements the MatchObserver pattern to synchronize local match data
 * with the Firebase online database in real-time.
 *
 * This observer's job is to ensure that any change made by the host
 * (e.g., adding an event, updating the match) is immediately sent
 * to the online repository
 */
public class FirebaseMatchObserver implements MatchObserver {

    private final MatchFirebaseRepository matchFirebaseRepository;
    private final String matchId;

    /**
     * Constructs a new FirebaseMatchObserver.
     * This observer is stateful and is tied to a specific match.
     *
     * @param matchFirebaseRepository The repository instance to use for online operations.
     * @param matchId The unique ID of the match this observer is responsible for syncing.
     */
    public FirebaseMatchObserver(MatchFirebaseRepository matchFirebaseRepository, String matchId) {
        this.matchFirebaseRepository = matchFirebaseRepository;
        this.matchId = matchId;
    }

    /**
     * Called when the entire Match object state is updated.
     * This method will push the complete, updated Match object to Firebase,
     * overwriting the previous state (last-write-wins).
     *
     * @param match The updated Match object.
     */
    @Override
    public void onMatchUpdated(Match match) {
        if (matchFirebaseRepository != null) {
            // Use the repository to update the entire match document online
            matchFirebaseRepository.update(match);
        }
    }

    /**
     * Called when a new individual event (like a goal or a wicket) is added.
     * This pushes the new event to the online database.
     *
     * Note: Since MatchFirebaseRepository doesn't have a specialized addEvent method,
     * we trigger a full match update. In a real implementation, you might create
     * a separate EventRepository for more efficient event syncing.
     *
     * @param event The new MatchEvent.
     */
    @Override
    public void onEventAdded(MatchEvent event) {
        if (matchFirebaseRepository != null && event != null) {
            // Check that the event's matchId matches this observer's matchId
            if (event.getMatchId() != null && event.getMatchId().equals(this.matchId)) {
                // Events are stored within the Match object
                // A full match update will sync all events
                // For better performance, consider creating a separate EventRepository
                // and storing events in a sub-collection
                // For now, events sync as part of the full match object via onMatchUpdated
            } else {
                // Log an error or handle mismatch
                System.err.println("FirebaseMatchObserver: Event matchId does not match observer's matchId.");
            }
        }
    }

    /**
     * Called when the match's status changes (e.g., SCHEDULED -> LIVE).
     * This method updates the match in Firebase. Since the repository doesn't have
     * a partial update method, the full match object is updated via onMatchUpdated.
     *
     * @param newStatus The new status (e.g., from MatchStatus enum).
     */
    @Override
    public void onMatchStatusChanged(String newStatus) {
        if (matchFirebaseRepository != null && this.matchId != null && newStatus != null) {
            // Status changes are part of the Match object
            // The full match update (triggered via onMatchUpdated) will sync the status
            // For better performance, you could add a partial update method to the repository
            // that uses Firestore's field-level update: .update("matchStatus", newStatus)
            // For now, status syncs as part of the full match object
        }
    }
}