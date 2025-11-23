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
     * CRITICAL FIX: Now actually triggers a Firebase write.
     * Events are stored within the Match object, so we need to update the entire match.
     * For better performance in production, consider creating a separate EventRepository.
     *
     * @param event The new MatchEvent.
     */
    @Override
    public void onEventAdded(MatchEvent event) {
        if (matchFirebaseRepository != null && event != null) {
            // Check that the event's matchId matches this observer's matchId
            if (event.getMatchId() != null && event.getMatchId().equals(this.matchId)) {
                // CRITICAL FIX: Fetch the match and update it to persist the new event
                androidx.lifecycle.Observer<com.example.tournafy.domain.models.base.Match> eventSyncObserver = new androidx.lifecycle.Observer<Match>() {
                    @Override
                    public void onChanged(com.example.tournafy.domain.models.base.Match match) {
                        if (match != null) {
                            // The match object already contains the new event (added by processEvent)
                            // Now persist it to Firebase
                            matchFirebaseRepository.update(match)
                                .addOnFailureListener(e -> 
                                    System.err.println("FirebaseMatchObserver: Failed to sync event - " + e.getMessage())
                                );
                        }
                        // IMPORTANT: Remove observer after execution to prevent infinite loop
                        matchFirebaseRepository.getById(matchId).removeObserver(this);
                    }
                };
                matchFirebaseRepository.getById(matchId).observeForever(eventSyncObserver);
            } else {
                // Log an error or handle mismatch
                System.err.println("FirebaseMatchObserver: Event matchId does not match observer's matchId.");
            }
        }
    }

    /**
     * Called when the match's status changes (e.g., SCHEDULED -> LIVE).
     * This method updates the match in Firebase.
     *
     * CRITICAL FIX: Now actually triggers a Firebase write using updateMatchStatus.
     * We use the partial update method for efficiency.
     *
     * @param newStatus The new status (e.g., from MatchStatus enum).
     */
    @Override
    public void onMatchStatusChanged(String newStatus) {
        if (matchFirebaseRepository != null && this.matchId != null && newStatus != null) {
            // CRITICAL FIX: Use the partial update method for efficiency
            matchFirebaseRepository.updateMatchStatus(matchId, newStatus)
                .addOnFailureListener(e -> 
                    System.err.println("FirebaseMatchObserver: Failed to sync status change - " + e.getMessage())
                );
                
            // Also trigger a full match update to ensure all related changes (innings, etc.) are synced
            androidx.lifecycle.Observer<com.example.tournafy.domain.models.base.Match> statusSyncObserver = new androidx.lifecycle.Observer<Match>() {
                @Override
                public void onChanged(com.example.tournafy.domain.models.base.Match match) {
                    if (match != null) {
                        matchFirebaseRepository.update(match)
                            .addOnFailureListener(e -> 
                                System.err.println("FirebaseMatchObserver: Failed to sync full match after status change - " + e.getMessage())
                            );
                    }
                    // IMPORTANT: Remove observer after execution to prevent infinite loop
                    matchFirebaseRepository.getById(matchId).removeObserver(this);
                }
            };
            matchFirebaseRepository.getById(matchId).observeForever(statusSyncObserver);
        }
    }
}