package com.example.tournafy.command.football;

import com.example.tournafy.command.interfaces.MatchCommand;
import com.example.tournafy.domain.models.match.football.FootballMatch;
import com.example.tournafy.domain.models.match.football.FootballEvent;
import com.example.tournafy.domain.models.match.football.FootballSubstitutionDetail;

public class SubstitutePlayerCommand implements MatchCommand {

    private final FootballMatch match;
    private final FootballEvent event;
    private final FootballSubstitutionDetail subDetail;

    public SubstitutePlayerCommand(FootballMatch match, FootballEvent event, FootballSubstitutionDetail subDetail) {
        this.match = match;
        this.event = event;
        this.subDetail = subDetail;
    }

    @Override
    public void execute() {
        event.setSubstitutionDetail(subDetail);
        match.addMatchEvent(event);
        
        // Perform the swap in the team lineup
        match.performSubstitution(subDetail.getPlayerOutId(), subDetail.getPlayerInId());
    }

    @Override
    public void undo() {
        match.removeMatchEvent(event);
        
        // Reverse the swap (In becomes Out, Out becomes In)
        match.performSubstitution(subDetail.getPlayerInId(), subDetail.getPlayerOutId());
    }
}