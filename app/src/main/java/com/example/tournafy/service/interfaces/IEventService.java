package com.tournafy.service.interfaces;

// Note: Imports will be valid once domain models are created.
// import com.tournafy.domain.models.base.Match;
// import com.tournafy.domain.models.base.MatchEvent;

/**
 * Defines the contract for recording, deleting, and managing
 * live match events (e.g., goals, wickets). 
 * This service will use the Command Pattern for undo/redo. 
 */
public interface IEventService {

    /**
     * Records a new event for a specific match.
     * @param match The match this event belongs to.
     * @param event The new MatchEvent to add.
     */
    void recordEvent(Match match, MatchEvent event);

    /**
     * Undoes the last recorded event for a specific match.
     * @param match The match to undo an event for.
     */
    void undoLastEvent(Match match);

    /**
     * Redoes the last undone event for a specific match.
     * @param match The match to redo an event for.
     */
    void redoLastEvent(Match match);
}