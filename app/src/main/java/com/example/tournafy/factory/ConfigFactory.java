package com.example.tournafy.factory;

import com.example.tournafy.domain.models.base.MatchConfig;
import com.example.tournafy.domain.models.match.cricket.CricketMatchConfig;
import com.example.tournafy.domain.models.match.football.FootballMatchConfig;
import com.example.tournafy.domain.models.sport.SportTypeEnum;

/**
 * It creates the correct config object (e.g., CricketMatchConfig)
 * based on the selected sport type.
 */
public class ConfigFactory {

    /**
     * @param sportType The enum constant representing the sport (CRICKET, FOOTBALL, etc.).
     * @return A new instance of a class that extends MatchConfig (e.g., CricketMatchConfig).
     * @throws IllegalArgumentException If the provided sportType is null or not supported.
     */
    public static MatchConfig createConfig(SportTypeEnum sportType) {
        if (sportType == null) {
            throw new IllegalArgumentException("SportType cannot be null.");
        }

        switch (sportType) {
            case CRICKET:
                // We return a new CricketMatchConfig, which inherits from MatchConfig
                return new CricketMatchConfig();
            case FOOTBALL:
                return new FootballMatchConfig();
            default:
                // This handles any future SportTypeEnum values that aren't implemented yet
                throw new IllegalArgumentException("Unsupported sport type: " + sportType);
        }
    }
}