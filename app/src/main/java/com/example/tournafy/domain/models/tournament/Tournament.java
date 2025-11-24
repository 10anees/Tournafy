package com.example.tournafy.domain.models.tournament;

import com.example.tournafy.domain.models.base.HostedEntity;
import com.example.tournafy.domain.enums.EntityStatus;
import com.example.tournafy.domain.interfaces.TournamentObserver;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.Map;
import com.example.tournafy.domain.models.team.TournamentTeam;
import com.google.firebase.firestore.Exclude;

public class Tournament extends HostedEntity {

    private String sportId;
    private String tournamentType;
    private Map<String, Object> tournamentConfig;
    private Date startDate;
    private String winnerTeamId;

    // FIX: Uncommented and initialized to avoid NullPointerException
    private List<TournamentTeam> teams = new ArrayList<>();

    // We keep stages commented out if not used yet, or uncomment if needed
    // private List<TournamentStage> stages;

    private transient List<TournamentObserver> observers = new ArrayList<>();

    private Tournament(Builder builder) {
        super();
        this.entityType = "TOURNAMENT";
        this.name = builder.name;
        this.hostUserId = builder.hostUserId;
        this.status = EntityStatus.DRAFT.name();
        this.createdAt = new Date();
        this.isOnline = false;
        this.sportId = builder.sportId;
        this.tournamentType = builder.tournamentType;
        this.tournamentConfig = builder.tournamentConfig;
        this.startDate = builder.startDate;
    }

    public Tournament() {
        super();
        this.entityType = "TOURNAMENT";
    }

    public static class Builder {
        private final String name;
        private final String hostUserId;
        private final String sportId;
        private final String tournamentType;
        private Map<String, Object> tournamentConfig;
        private Date startDate;

        public Builder(String name, String hostUserId, String sportId, String tournamentType) {
            this.name = name;
            this.hostUserId = hostUserId;
            this.sportId = sportId;
            this.tournamentType = tournamentType;
            this.startDate = new Date();
        }

        public Builder withTournamentConfig(Map<String, Object> config) {
            this.tournamentConfig = config;
            return this;
        }

        public Builder withStartDate(Date startDate) {
            this.startDate = startDate;
            return this;
        }

        public Tournament build() {
            return new Tournament(this);
        }
    }

    // --- Getters and Setters ---

    public String getSportId() { return sportId; }
    public void setSportId(String sportId) { this.sportId = sportId; }

    public String getSportType() { return sportId; } // Alias for sportId
    public void setSportType(String sportType) { this.sportId = sportType; }

    public String getTournamentType() { return tournamentType; }
    public void setTournamentType(String tournamentType) { this.tournamentType = tournamentType; }

    public Map<String, Object> getTournamentConfig() { return tournamentConfig; }
    public void setTournamentConfig(Map<String, Object> tournamentConfig) { this.tournamentConfig = tournamentConfig; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public String getWinnerTeamId() { return winnerTeamId; }
    public void setWinnerTeamId(String winnerTeamId) { this.winnerTeamId = winnerTeamId; }

    // FIX: Added Getter and Setter for Teams
    // We use @Exclude for getters we don't want automatically saved to Firestore
    // if the teams are stored in a subcollection, but here we assume they might be part of the object
    // or populated manually.
    public List<TournamentTeam> getTeams() {
        return teams;
    }

    public void setTeams(List<TournamentTeam> teams) {
        this.teams = teams;
    }

    // --- Observer Pattern ---

    public void addObserver(TournamentObserver observer) {
        if (this.observers == null) this.observers = new ArrayList<>();
        if (!this.observers.contains(observer)) this.observers.add(observer);
    }

    public void removeObserver(TournamentObserver observer) {
        if (this.observers != null) this.observers.remove(observer);
    }

    public void notifyStandingsUpdated() {
        if (this.observers == null) return;
        for (TournamentObserver observer : new ArrayList<>(this.observers)) {
            observer.onStandingsUpdated(this);
        }
    }

    public void notifyMatchCompleted(String completedMatchId) {
        if (this.observers == null) return;
        for (TournamentObserver observer : new ArrayList<>(this.observers)) {
            observer.onMatchCompleted(completedMatchId);
        }
    }
}