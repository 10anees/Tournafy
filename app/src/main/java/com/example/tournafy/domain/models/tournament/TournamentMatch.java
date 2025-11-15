package com.example.tournafy.domain.models.tournament;

/**
 * Maps to the TOURNAMENT_MATCH entity in the EERD.
 * This class defines a specific match's place within the tournament structure.
 */
public class TournamentMatch {

    private String tournamentMatchId;
    private String tournamentId;
    private String stageId;
    private String matchId;
    private int matchOrder; // The order of the match in the stage (e.g., Quarter-Final 1)

    public TournamentMatch() {
    }

    /**
     * @param tournamentId The ID of the tournament.
     * @param stageId      The ID of the stage this match belongs to.
     * @param matchId      The ID of the actual match.
     * @param matchOrder   The order of this match within the stage.
     */
    public TournamentMatch(String tournamentId, String stageId, String matchId, int matchOrder) {
        this.tournamentId = tournamentId;
        this.stageId = stageId;
        this.matchId = matchId;
        this.matchOrder = matchOrder;
    }

    // --- Getters and Setters ---

    public String getTournamentMatchId() {
        return tournamentMatchId;
    }

    public void setTournamentMatchId(String tournamentMatchId) {
        this.tournamentMatchId = tournamentMatchId;
    }

    public String getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(String tournamentId) {
        this.tournamentId = tournamentId;
    }

    public String getStageId() {
        return stageId;
    }

    public void setStageId(String stageId) {
        this.stageId = stageId;
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public int getMatchOrder() {
        return matchOrder;
    }

    public void setMatchOrder(int matchOrder) {
        this.matchOrder = matchOrder;
    }
}