package com.example.tournafy.domain.models.tournament;

import com.example.tournafy.domain.models.tournament.TournamentMatch;
import java.util.List;

/**
 * Maps to the TOURNAMENT_STAGE entity in the EERD.
 */
public class TournamentStage {

    private String stageId;
    private String tournamentId;
    private String stageName;
    private String stageType; // Using String to align with StageType enum
    private int stageOrder;
    private boolean isCompleted;

    // Relationship: A stage organizes multiple matches.
    // This list is typically loaded by the ViewModel/Service, not the model itself.
    private List<TournamentMatch> matches;

    public TournamentStage() {
        this.isCompleted = false;
    }

    // --- Getters and Setters ---

    public String getStageId() {
        return stageId;
    }

    public void setStageId(String stageId) {
        this.stageId = stageId;
    }

    public String getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(String tournamentId) {
        this.tournamentId = tournamentId;
    }

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public String getStageType() {
        return stageType;
    }

    public void setStageType(String stageType) {
        this.stageType = stageType;
    }

    public int getStageOrder() {
        return stageOrder;
    }

    public void setStageOrder(int stageOrder) {
        this.stageOrder = stageOrder;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    // --- Getter/Setter for relationship (optional) ---
    public List<TournamentMatch> getMatches() {
        return matches;
    }
    
    public void setMatches(List<TournamentMatch> matches) {
        this.matches = matches;
    }
}