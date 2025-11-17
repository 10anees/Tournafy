package com.example.tournafy.command.football;

import com.example.tournafy.command.interfaces.MatchCommand;
import com.example.tournafy.domain.models.match.football.FootballMatch;
import com.example.tournafy.domain.models.match.football.FootballEvent;
import com.example.tournafy.domain.models.match.football.FootballShotDetail;

public class AddShotCommand implements MatchCommand {

    private final FootballMatch match;
    private final FootballEvent event;
    private final FootballShotDetail shotDetail;

    public AddShotCommand(FootballMatch match, FootballEvent event, FootballShotDetail shotDetail) {
        this.match = match;
        this.event = event;
        this.shotDetail = shotDetail;
    }

    @Override
    public void execute() {
        event.setShotDetail(shotDetail);
        match.addMatchEvent(event);
        // Additional logic: Update team shot counts stats if maintained separately
    }

    @Override
    public void undo() {
        match.removeMatchEvent(event);
    }
}