package com.example.tournafy.command.football;

import com.example.tournafy.command.interfaces.MatchCommand;
import com.example.tournafy.domain.models.match.football.FootballMatch;
import com.example.tournafy.domain.models.match.football.FootballEvent;
import com.example.tournafy.domain.models.match.football.FootballCardDetail;

public class AddCardCommand implements MatchCommand {

    private final FootballMatch match;
    private final FootballEvent event;
    private final FootballCardDetail cardDetail;

    public AddCardCommand(FootballMatch match, FootballEvent event, FootballCardDetail cardDetail) {
        this.match = match;
        this.event = event;
        this.cardDetail = cardDetail;
    }

    @Override
    public void execute() {
        // 1. Attach details and add event
        event.setCardDetail(cardDetail);
        match.addMatchEvent(event);

        // 2. Logic to check for Red Card (or second yellow) could trigger player status change
        // match.getPlayer(cardDetail.getPlayerId()).setSentOff(true);
    }

    @Override
    public void undo() {
        // 1. Remove event
        match.removeMatchEvent(event);
        
        // 2. Revert player status if they were sent off
        // match.getPlayer(cardDetail.getPlayerId()).setSentOff(false);
    }
    
    @Override
    public String getEventId() {
        return event != null ? event.getEventId() : null;
    }
    
    @Override
    public String getCommandType() {
        return "CARD";
    }
}