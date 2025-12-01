package com.example.tournafy.command.interfaces;

/**
 * The Command Interface.
 * All scoring actions (Goal, Ball, Wicket, Card) must implement this.
 */
public interface MatchCommand {
    /**
     * Executes the logic to update the match state.
     * e.g., Adds runs to score, adds a goal to timeline.
     */
    void execute();

    /**
     * Reverses the logic performed in execute().
     * e.g., Removes the last ball, removes the goal.
     */
    void undo();
    
    /**
     * Returns the ID of the event created by this command.
     * Used for deleting the event from Firestore when undoing.
     * @return The event ID, or null if no event was created
     */
    default String getEventId() {
        return null;
    }
    
    /**
     * Returns the type of command for specialized undo handling.
     * @return The command type string (e.g., "GOAL", "CARD", "BALL", "WICKET")
     */
    default String getCommandType() {
        return "UNKNOWN";
    }
}