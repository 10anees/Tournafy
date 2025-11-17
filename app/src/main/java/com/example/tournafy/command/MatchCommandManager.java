package com.example.tournafy.command;

import com.example.tournafy.command.interfaces.MatchCommand;
import java.util.Stack;

/**
 * Invoker Class.
 * Manages the history of commands to enable Undo/Redo functionality.
 * Linked to the 'Undo' button in your XML layouts.
 */
public class MatchCommandManager {
    
    // Stack to store history of executed commands
    private final Stack<MatchCommand> commandHistory = new Stack<>();
    
    // Stack to store undone commands (for Redo functionality)
    private final Stack<MatchCommand> redoStack = new Stack<>();

    /**
     * Executes a command and pushes it onto the history stack.
     * Clears the redo stack because a new path of history has started.
     * * @param command The command to execute (e.g., AddBallCommand)
     */
    public void executeCommand(MatchCommand command) {
        command.execute();
        commandHistory.push(command);
        redoStack.clear();
    }

    /**
     * Undoes the most recent command.
     * Called when R.id.btnUndo or R.id.cardUndo is clicked.
     */
    public void undo() {
        if (!commandHistory.isEmpty()) {
            MatchCommand command = commandHistory.pop();
            command.undo();
            redoStack.push(command);
        }
    }

    /**
     * Redoes the most recently undone command.
     */
    public void redo() {
        if (!redoStack.isEmpty()) {
            MatchCommand command = redoStack.pop();
            command.execute();
            commandHistory.push(command);
        }
    }

    public boolean canUndo() {
        return !commandHistory.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
}