package com.example.tournafy.command.football;

import com.example.tournafy.command.interfaces.MatchCommand;
import com.example.tournafy.domain.models.match.football.FootballMatch;
import com.example.tournafy.domain.models.match.football.FootballEvent;
import com.example.tournafy.domain.models.match.football.FootballGoalDetail;

public class AddGoalCommand implements MatchCommand {

    private final FootballMatch match;
    private final FootballEvent event;
    private final FootballGoalDetail goalDetail;

    public AddGoalCommand(FootballMatch match, FootballEvent event, FootballGoalDetail goalDetail) {
        this.match = match;
        this.event = event;
        this.goalDetail = goalDetail;
    }

    @Override
    public void execute() {
        event.setGoalDetail(goalDetail);
        match.addMatchEvent(event);
        
        // Update score based on which team scored
        if (event.getTeamId().equals(match.getHomeTeamId())) {
            match.setHomeScore(match.getHomeScore() + 1);
        } else {
            match.setAwayScore(match.getAwayScore() + 1);
        }
    }

    @Override
    public void undo() {
        match.removeMatchEvent(event);
        
        // Revert score
        if (event.getTeamId().equals(match.getHomeTeamId())) {
            match.setHomeScore(match.getHomeScore() - 1);
        } else {
            match.setAwayScore(match.getAwayScore() - 1);
        }
    }
    
    @Override
    public String getEventId() {
        return event != null ? event.getEventId() : null;
    }
    
    @Override
    public String getCommandType() {
        return "GOAL";
    }
    
    /**
     * Returns the scorer's player ID for stat reversal.
     */
    public String getScorerId() {
        return goalDetail != null ? goalDetail.getScorerId() : null;
    }
    
    /**
     * Returns the assister's player ID for stat reversal.
     */
    public String getAssisterId() {
        return goalDetail != null ? goalDetail.getAssistPlayerId() : null;
    }
}