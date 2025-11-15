package com.example.tournafy.domain.models.match.football;

import com.example.tournafy.domain.models.base.MatchEvent;

public class FootballEvent extends MatchEvent {

    // Fields are based on the EERD and file structure 
    private String eventCategory; // GOAL, CARD, SUBSTITUTION, etc. 
    private int matchMinute; // 
    private int addedTimeMinute; // 
    private String matchPeriod; // FIRST_HALF, SECOND_HALF, etc. 
    private int homeScoreAtEvent; // 
    private int awayScoreAtEvent; // 
    private String locationOnPitch; // 

    private FootballGoalDetail goalDetail; // 
    private FootballCardDetail cardDetail; // 
    private FootballSubstitutionDetail substitutionDetail; 
    private FootballShotDetail shotDetail; // 
    private FootballSaveDetail saveDetail; // 


    public FootballEvent() {
        super();
        this.eventType = "FOOTBALL_EVENT";
    }

    // --- Getters and Setters ---

    public String getEventCategory() {
        return eventCategory;
    }

    public void setEventCategory(String eventCategory) {
        this.eventCategory = eventCategory;
    }

    public int getMatchMinute() {
        return matchMinute;
    }

    public void setMatchMinute(int matchMinute) {
        this.matchMinute = matchMinute;
    }

    public int getAddedTimeMinute() {
        return addedTimeMinute;
    }

    public void setAddedTimeMinute(int addedTimeMinute) {
        this.addedTimeMinute = addedTimeMinute;
    }

    public String getMatchPeriod() {
        return matchPeriod;
    }

    public void setMatchPeriod(String matchPeriod) {
        this.matchPeriod = matchPeriod;
    }

    public int getHomeScoreAtEvent() {
        return homeScoreAtEvent;
    }

    public void setHomeScoreAtEvent(int homeScoreAtEvent) {
        this.homeScoreAtEvent = homeScoreAtEvent;
    }

    public int getAwayScoreAtEvent() {
        return awayScoreAtEvent;
    }

    public void setAwayScoreAtEvent(int awayScoreAtEvent) {
        this.awayScoreAtEvent = awayScoreAtEvent;
    }

    public String getLocationOnPitch() {
        return locationOnPitch;
    }

    public void setLocationOnPitch(String locationOnPitch) {
        this.locationOnPitch = locationOnPitch;
    }

    public FootballGoalDetail getGoalDetail() {
        return goalDetail;
    }

    public void setGoalDetail(FootballGoalDetail goalDetail) {
        this.goalDetail = goalDetail;
    }

    public FootballCardDetail getCardDetail() {
        return cardDetail;
    }

    public void setCardDetail(FootballCardDetail cardDetail) {
        this.cardDetail = cardDetail;
    }

    public FootballSubstitutionDetail getSubstitutionDetail() {
        return substitutionDetail;
    }

    public void setSubstitutionDetail(FootballSubstitutionDetail substitutionDetail) {
        this.substitutionDetail = substitutionDetail;
    }

    public FootballShotDetail getShotDetail() {
        return shotDetail;
    }

    public void setShotDetail(FootballShotDetail shotDetail) {
        this.shotDetail = shotDetail;
    }

    public FootballSaveDetail getSaveDetail() {
        return saveDetail;
    }

    public void setSaveDetail(FootballSaveDetail saveDetail) {
        this.saveDetail = saveDetail;
    }
}