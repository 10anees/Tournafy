package com.example.tournafy.command.cricket;

import com.example.tournafy.command.interfaces.MatchCommand;
import com.example.tournafy.domain.models.match.cricket.CricketMatch;
import com.example.tournafy.domain.models.match.cricket.Ball;

/**
 * Concrete Command for adding a regular delivery (Runs/Dot ball).
 * Maps to buttons: 0, 1, 2, 3, 4, 5, 6 in your GridKeypad.
 */
public class AddBallCommand implements MatchCommand {

    private final CricketMatch match;
    private final Ball ball;

    /**
     * @param match The current match instance being hosted.
     * @param ball The ball object created by the Factory containing run data.
     */
    public AddBallCommand(CricketMatch match, Ball ball) {
        this.match = match;
        this.ball = ball;
    }

    @Override
    public void execute() {
        // Get current innings and over
        if (match.getCurrentInnings() == null || match.getCurrentOver() == null) {
            return;
        }
        
        // 1. Add the ball to the current over
        if (match.getCurrentOver().getBalls() == null) {
            match.getCurrentOver().setBalls(new java.util.ArrayList<>());
        }
        match.getCurrentOver().getBalls().add(ball);
        
        // 2. Update total score
        int currentScore = match.getCurrentInnings().getTotalRuns();
        match.getCurrentInnings().setTotalRuns(currentScore + ball.getRunsScored());
        
        // 3. Update over runs
        match.getCurrentOver().setRunsInOver(match.getCurrentOver().getRunsInOver() + ball.getRunsScored());
    }

    @Override
    public void undo() {
        // Get current innings and over
        if (match.getCurrentInnings() == null || match.getCurrentOver() == null) {
            return;
        }
        
        // 1. Remove the ball from the current over
        if (match.getCurrentOver().getBalls() != null && !match.getCurrentOver().getBalls().isEmpty()) {
            match.getCurrentOver().getBalls().remove(match.getCurrentOver().getBalls().size() - 1);
        }
        
        // 2. Revert total score
        int currentScore = match.getCurrentInnings().getTotalRuns();
        match.getCurrentInnings().setTotalRuns(currentScore - ball.getRunsScored());
        
        // 3. Revert over runs
        match.getCurrentOver().setRunsInOver(match.getCurrentOver().getRunsInOver() - ball.getRunsScored());
    }
    
    @Override
    public String getEventId() {
        return ball != null ? ball.getBallId() : null;
    }
    
    @Override
    public String getCommandType() {
        return "BALL";
    }
}