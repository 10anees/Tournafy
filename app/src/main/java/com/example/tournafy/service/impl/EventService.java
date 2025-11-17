package com.example.tournafy.service.impl;

import com.example.tournafy.command.interfaces.MatchCommand;
import com.example.tournafy.command.MatchCommandManager;
import com.example.tournafy.service.interfaces.IEventService;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Concrete implementation of the IEventService.
 *
 * This service acts as an orchestrator for the Command Pattern.
 * It takes commands from the ViewModel, executes them, and manages
 * the undo/redo stack via the MatchCommandManager.
 */
@Singleton
public class EventService implements IEventService {

    /**
     * Constructor for Hilt Dependency Injection.
     * This service is a stateless orchestrator.
     */
    @Inject
    public EventService() {
    }

    @Override
    public void executeCommand(MatchCommand command, MatchCommandManager manager, EventCallback<Void> callback) {
        try {
            // FIX: We delegate to the manager.
            // The manager.executeCommand() method handles command.execute()
            // AND pushing it to the stack internally.
            manager.executeCommand(command);

            callback.onSuccess(null);
        } catch (Exception e) {
            callback.onError(e);
        }
    }

    @Override
    public void undoLastCommand(MatchCommandManager manager, EventCallback<Void> callback) {
        try {
            if (manager.canUndo()) {
                manager.undo();
                callback.onSuccess(null);
            } else {
                callback.onError(new IllegalStateException("There is nothing to undo."));
            }
        } catch (Exception e) {
            callback.onError(e);
        }
    }

    @Override
    public void redoLastCommand(MatchCommandManager manager, EventCallback<Void> callback) {
        try {
            if (manager.canRedo()) {
                manager.redo();
                callback.onSuccess(null);
            } else {
                callback.onError(new IllegalStateException("There is nothing to redo."));
            }
        } catch (Exception e) {
            callback.onError(e);
        }
    }
}