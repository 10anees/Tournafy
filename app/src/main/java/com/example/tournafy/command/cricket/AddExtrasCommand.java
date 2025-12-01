package com.example.tournafy.command.cricket;

import com.example.tournafy.command.interfaces.MatchCommand;
import com.example.tournafy.domain.models.match.cricket.CricketMatch;
import com.example.tournafy.domain.models.match.cricket.Ball;
import com.example.tournafy.domain.models.match.cricket.CricketEvent;
import com.example.tournafy.domain.models.match.cricket.CricketExtrasDetail;

public class AddExtrasCommand implements MatchCommand {

    private final CricketMatch match;
    private final Ball ball; // Can represent the extra delivery
    private final CricketEvent event;
    private final CricketExtrasDetail extrasDetail;

    public AddExtrasCommand(CricketMatch match, Ball ball, CricketEvent event, CricketExtrasDetail extrasDetail) {
        this.match = match;
        this.ball = ball;
        this.event = event;
        this.extrasDetail = extrasDetail;
    }

    @Override
    public void execute() {
        // Get current innings and over
        if (match.getCurrentInnings() == null || match.getCurrentOver() == null) {
            return;
        }
        
        // 1. Logic for legal delivery check (Wide/No-Ball doesn't count as legal ball in over)
        if (ball.isLegalDelivery()) {
            if (match.getCurrentOver().getBalls() == null) {
                match.getCurrentOver().setBalls(new java.util.ArrayList<>());
            }
            match.getCurrentOver().getBalls().add(ball);
        } else {
            // Add as extra delivery (still tracked but doesn't count as legal ball)
            if (match.getCurrentOver().getBalls() == null) {
                match.getCurrentOver().setBalls(new java.util.ArrayList<>());
            }
            match.getCurrentOver().getBalls().add(ball);
        }

        // 2. Add Extras to total score
        int currentRuns = match.getCurrentInnings().getTotalRuns();
        match.getCurrentInnings().setTotalRuns(currentRuns + extrasDetail.getExtrasRuns());
        match.getCurrentOver().setRunsInOver(match.getCurrentOver().getRunsInOver() + extrasDetail.getExtrasRuns());

        event.setExtrasDetail(extrasDetail);
        match.addMatchEvent(event);
    }

    @Override
    public void undo() {
        // Get current innings and over
        if (match.getCurrentInnings() == null || match.getCurrentOver() == null) {
            return;
        }
        
        // Remove the ball
        if (match.getCurrentOver().getBalls() != null && !match.getCurrentOver().getBalls().isEmpty()) {
            match.getCurrentOver().getBalls().remove(match.getCurrentOver().getBalls().size() - 1);
        }

        int currentRuns = match.getCurrentInnings().getTotalRuns();
        match.getCurrentInnings().setTotalRuns(currentRuns - extrasDetail.getExtrasRuns());
        match.getCurrentOver().setRunsInOver(match.getCurrentOver().getRunsInOver() - extrasDetail.getExtrasRuns());

        match.removeMatchEvent(event);
    }
    
    @Override
    public String getEventId() {
        return event != null ? event.getEventId() : null;
    }
    
    @Override
    public String getCommandType() {
        return "EXTRAS";
    }
}