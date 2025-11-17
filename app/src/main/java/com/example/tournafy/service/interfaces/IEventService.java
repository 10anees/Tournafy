package com.example.tournafy.service.interfaces;

import com.example.tournafy.command.MatchCommandManager;

/**
 * Service interface for recording match events. 
 * This service will be responsible for creating and executing
 * commands (AddBallCommand, AddGoalCommand, etc.) using the
 * MatchCommandManager.
 */
public interface IEventService {

    /**
     * Executes a match command (e.g., AddBall, AddGoal) and
     * adds it to the command manager's stack.
     *
     * @param command The MatchCommand to be executed.
     * @param manager The MatchCommandManager for the current match.
     * @param callback Callback to signal success or error.
     */
    void executeCommand(com.example.tournafy.command.interfaces.MatchCommand command, MatchCommandManager manager, EventCallback<Void> callback);

    /**
     * Undoes the last executed command.
     *
     * @param manager The MatchCommandManager for the current match.
     * @param callback Callback to signal success or error.
     */
    void undoLastCommand(MatchCommandManager manager, EventCallback<Void> callback);

    /**
     * Redoes the last undone command.
     *
     * @param manager The MatchCommandManager for the current match.
     * @param callback Callback to signal success or error.
     */
    void redoLastCommand(MatchCommandManager manager, EventCallback<Void> callback);

    /**
     * A generic callback interface for event operations.
     *
     * @param <T> The type of the successful result.
     */
    interface EventCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }
}