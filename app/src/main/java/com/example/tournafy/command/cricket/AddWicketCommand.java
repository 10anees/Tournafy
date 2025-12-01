package com.example.tournafy.command.cricket;

import com.example.tournafy.command.interfaces.MatchCommand;
import com.example.tournafy.domain.models.match.cricket.CricketMatch;
import com.example.tournafy.domain.models.match.cricket.Ball;
import com.example.tournafy.domain.models.match.cricket.CricketEvent;
import com.example.tournafy.domain.models.match.cricket.CricketWicketDetail;

public class AddWicketCommand implements MatchCommand {

    private final CricketMatch match;
    private final Ball ball; // The ball on which wicket fell
    private final CricketEvent event;
    private final CricketWicketDetail wicketDetail;

    public AddWicketCommand(CricketMatch match, Ball ball, CricketEvent event, CricketWicketDetail wicketDetail) {
        this.match = match;
        this.ball = ball;
        this.event = event;
        this.wicketDetail = wicketDetail;
    }

    @Override
    public void execute() {
        // Get current innings and over
        if (match.getCurrentInnings() == null || match.getCurrentOver() == null) {
            return;
        }
        
        // 1. Register the ball (even wickets count as a ball unless specified otherwise)
        if (match.getCurrentOver().getBalls() == null) {
            match.getCurrentOver().setBalls(new java.util.ArrayList<>());
        }
        match.getCurrentOver().getBalls().add(ball);
        
        // 2. Update Wicket Count
        int wickets = match.getCurrentInnings().getWicketsFallen();
        match.getCurrentInnings().setWicketsFallen(wickets + 1);
        match.getCurrentOver().setWicketsInOver(match.getCurrentOver().getWicketsInOver() + 1);

        // 3. Add runs if any (e.g. run out while taking a run)
        int currentRuns = match.getCurrentInnings().getTotalRuns();
        match.getCurrentInnings().setTotalRuns(currentRuns + ball.getRunsScored());
        match.getCurrentOver().setRunsInOver(match.getCurrentOver().getRunsInOver() + ball.getRunsScored());

        // 4. Save detail
        event.setWicketDetail(wicketDetail);
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
        
        // Revert Wickets
        int wickets = match.getCurrentInnings().getWicketsFallen();
        match.getCurrentInnings().setWicketsFallen(wickets - 1);
        match.getCurrentOver().setWicketsInOver(match.getCurrentOver().getWicketsInOver() - 1);
        
        // Revert runs
        int currentRuns = match.getCurrentInnings().getTotalRuns();
        match.getCurrentInnings().setTotalRuns(currentRuns - ball.getRunsScored());
        match.getCurrentOver().setRunsInOver(match.getCurrentOver().getRunsInOver() - ball.getRunsScored());

        match.removeMatchEvent(event);
    }
    
    @Override
    public String getEventId() {
        return event != null ? event.getEventId() : null;
    }
    
    @Override
    public String getCommandType() {
        return "WICKET";
    }
}