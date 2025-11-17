package com.example.tournafy.command.cricket;

import com.example.tournafy.command.interfaces.MatchCommand;
import com.example.tournafy.domain.models.match.cricket.CricketMatch;
import com.example.tournafy.domain.models.match.cricket.Over;

public class EndOverCommand implements MatchCommand {

    private final CricketMatch match;
    private final Over completedOver;
    
    public EndOverCommand(CricketMatch match) {
        this.match = match;
        this.completedOver = match.getCurrentOver();
    }

    @Override
    public void execute() {
        if (match.getCurrentInnings() == null || completedOver == null) {
            return;
        }
        
        // 1. Mark current over as complete
        completedOver.setCompleted(true);
        
        // 2. Increment overs completed in innings
        match.getCurrentInnings().setOversCompleted(match.getCurrentInnings().getOversCompleted() + 1);
        
        // 3. Initialize new over (Managed by match logic)
        match.startNewOver(); 
    }

    @Override
    public void undo() {
        if (match.getCurrentInnings() == null) {
            return;
        }
        
        // 1. Remove the newly created empty over
        match.removeLastOver();
        
        // 2. Re-open the previous over
        completedOver.setCompleted(false);
        match.setCurrentOver(completedOver);
        
        // 3. Decrement overs completed
        match.getCurrentInnings().setOversCompleted(match.getCurrentInnings().getOversCompleted() - 1);
    }
}