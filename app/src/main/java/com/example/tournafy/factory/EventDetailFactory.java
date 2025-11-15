package com.example.tournafy.factory;

import com.example.tournafy.domain.models.match.cricket.CricketExtrasDetail;
import com.example.tournafy.domain.models.match.cricket.CricketWicketDetail;
import com.example.tournafy.domain.models.match.football.FootballCardDetail;
import com.example.tournafy.domain.models.match.football.FootballGoalDetail;
import com.example.tournafy.domain.models.match.football.FootballSaveDetail;
import com.example.tournafy.domain.models.match.football.FootballShotDetail;
import com.example.tournafy.domain.models.match.football.FootballSubstitutionDetail;

/**
 * This is used by the Command Pattern (e.g., AddGoalCommand) and
 * Service Layer (EventService) to create the objects that hold
 * detailed information about a specific event.
 */
public class EventDetailFactory {

    // --- Cricket Detail Creators ---

    /**
     * @return A new, empty CricketWicketDetail object.
     */
    public static CricketWicketDetail createWicketDetail() {
        return new CricketWicketDetail();
    }

    /**
     * @return A new, empty CricketExtrasDetail object.
     */
    public static CricketExtrasDetail createExtrasDetail() {
        return new CricketExtrasDetail();
    }

    // --- Football Detail Creators ---

    /**
     * @return A new, empty FootballGoalDetail object.
     */
    public static FootballGoalDetail createGoalDetail() {
        return new FootballGoalDetail();
    }

    /**
     * @return A new, empty FootballCardDetail object.
     */
    public static FootballCardDetail createCardDetail() {
        return new FootballCardDetail();
    }

    /**
     * @return A new, empty FootballSubstitutionDetail object.
     */
    public static FootballSubstitutionDetail createSubstitutionDetail() {
        return new FootballSubstitutionDetail();
    }

    /**
     * @return A new, empty FootballShotDetail object.
     */
    public static FootballShotDetail createShotDetail() {
        return new FootballShotDetail();
    }

    /**
     * @return A new, empty FootballSaveDetail object.
     */
    public static FootballSaveDetail createSaveDetail() {
        return new FootballSaveDetail();
    }
}