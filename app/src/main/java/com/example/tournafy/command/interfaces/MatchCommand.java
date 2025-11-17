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
}