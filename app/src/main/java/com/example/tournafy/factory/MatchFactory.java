package com.example.tournafy.factory;

import com.example.tournafy.domain.models.base.Match;
import com.example.tournafy.domain.models.match.cricket.CricketMatch;
import com.example.tournafy.domain.models.match.football.FootballMatch;
import com.example.tournafy.domain.models.sport.SportTypeEnum;

/**
 * Factory class for creating sport-specific Match objects.
 * This implements the Factory Pattern to decouple match creation logic
 * from the client code (ViewModels or Fragments).
 */
public class MatchFactory {

    /**
     * @param sportType The enum constant representing the sport (CRICKET, FOOTBALL, etc.).
     * @return A new instance of a class that extends Match (e.g., CricketMatch, FootballMatch).
     * @throws IllegalArgumentException If the provided sportType is null or not supported.
     */
    public static Match createMatch(SportTypeEnum sportType) {
        if (sportType == null) {
            throw new IllegalArgumentException("SportType cannot be null.");
        }

        switch (sportType) {
            case CRICKET:
                // We return a new CricketMatch, which inherits from Match 
                return new CricketMatch();
            case FOOTBALL: 
                return new FootballMatch();
            default:
                throw new IllegalArgumentException("Unsupported sport type: " + sportType);
        }
    }
}