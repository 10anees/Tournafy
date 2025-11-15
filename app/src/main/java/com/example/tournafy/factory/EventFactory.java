package com.example.tournafy.factory;

import com.example.tournafy.domain.models.base.MatchEvent;
import com.example.tournafy.domain.models.match.cricket.CricketEvent;
import com.example.tournafy.domain.models.match.football.FootballEvent;
import com.example.tournafy.domain.models.sport.SportTypeEnum;

public class EventFactory {

    /**
     * @param sportType The enum constant representing the sport (CRICKET, FOOTBALL).
     * @return A new instance of a class that extends MatchEvent (e.g., CricketEvent).
     * @throws IllegalArgumentException If the provided sportType is null or not supported.
     */
    public static MatchEvent createEvent(SportTypeEnum sportType) {
        if (sportType == null) {
            throw new IllegalArgumentException("SportType cannot be null.");
        }

        switch (sportType) {
            case CRICKET:
                // We return a new CricketEvent, which inherits from MatchEvent
                return new CricketEvent();
            case FOOTBALL:
                // We return a new FootballEvent, which inherits from MatchEvent
                return new FootballEvent();
            default:
                // This handles any future SportTypeEnum values that aren't implemented yet
                throw new IllegalArgumentException("Unsupported sport type: " + sportType);
        }
    }
}