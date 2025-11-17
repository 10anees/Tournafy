package com.example.tournafy.command.football;

import com.example.tournafy.command.interfaces.MatchCommand;
import com.example.tournafy.domain.models.match.football.FootballMatch;
import com.example.tournafy.domain.models.match.football.FootballEvent;
import com.example.tournafy.domain.models.match.football.FootballSaveDetail;

public class AddSaveCommand implements MatchCommand {

    private final FootballMatch match;
    private final FootballEvent event;
    private final FootballSaveDetail saveDetail;

    public AddSaveCommand(FootballMatch match, FootballEvent event, FootballSaveDetail saveDetail) {
        this.match = match;
        this.event = event;
        this.saveDetail = saveDetail;
    }

    @Override
    public void execute() {
        event.setSaveDetail(saveDetail);
        match.addMatchEvent(event);
        // Update goalkeeper stats
    }

    @Override
    public void undo() {
        match.removeMatchEvent(event);
    }
}