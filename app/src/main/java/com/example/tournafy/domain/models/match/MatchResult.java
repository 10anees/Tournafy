package com.example.tournafy.domain.models.match;

/**
 * Represents the final result of a Match (File: MatchResult.java).
 *
 * Maps to the MATCH_RESULT entity in the EERD.
 * This class stores the outcome of a completed match, including
 * winner, loser, scores, and win margin.
 */
public class MatchResult {

    private String resultId;
    private String matchId;
    private String winnerTeamId;
    private String loserTeamId;
    private int winnerScore;
    private int loserScore;
    private String resultType; // e.g., "WIN", "DRAW", "NO_RESULT"
    private String winMargin;  // e.g., "5 wickets", "10 runs", "2 goals"

    public MatchResult() {
    }

    /**
     * @param matchId      The ID of the match this result is for.
     * @param resultType   The type of result (WIN, DRAW, etc.).
     * @param winnerTeamId The ID of the winning team (can be null for a draw).
     * @param loserTeamId  The ID of the losing team (can be null for a draw).
     */
    public MatchResult(String matchId, String resultType, String winnerTeamId, String loserTeamId) {
        this.matchId = matchId;
        this.resultType = resultType;
        this.winnerTeamId = winnerTeamId;
        this.loserTeamId = loserTeamId;
    }

    // --- Getters and Setters ---

    public String getResultId() {
        return resultId;
    }

    public void setResultId(String resultId) {
        this.resultId = resultId;
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public String getWinnerTeamId() {
        return winnerTeamId;
    }

    public void setWinnerTeamId(String winnerTeamId) {
        this.winnerTeamId = winnerTeamId;
    }

    public String getLoserTeamId() {
        return loserTeamId;
    }

    public void setLoserTeamId(String loserTeamId) {
        this.loserTeamId = loserTeamId;
    }

    public int getWinnerScore() {
        return winnerScore;
    }

    public void setWinnerScore(int winnerScore) {
        this.winnerScore = winnerScore;
    }

    public int getLoserScore() {
        return loserScore;
    }

    public void setLoserScore(int loserScore) {
        this.loserScore = loserScore;
    }

    public String getResultType() {
        return resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public String getWinMargin() {
        return winMargin;
    }

    public void setWinMargin(String winMargin) {
        this.winMargin = winMargin;
    }
}