package com.example.tournafy.command.interfaces;

/**
 * Defines the contract for all executable actions during a match
 * that support undo/redo functionality (e.g., AddBall, AddGoal). 
 */
public interface MatchCommand {

    /**
     * @return true if execution was successful, false otherwise.
     */
    boolean execute();

    /**
     * Reverts the effects of the execute() method.
     */
    void undo();

    /**
     * Re-applies the effects of the execute() method (for redo).
     */
    void redo();
}