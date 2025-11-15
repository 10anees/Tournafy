package com.example.tournafy.domain.models.series;

/**
 * Represents the link between a Series and a specific Match.
 * (File: SeriesMatch.java)
 *
 * Maps to the SERIES_MATCH entity in the EERD.
 * This class associates a Match with a Series and defines its order.
 */
public class SeriesMatch {

    private String seriesMatchId;
    private String seriesId;
    private String matchId;
    private int matchNumber;

    /**
     * Default constructor.
     */
    public SeriesMatch() {
    }

    /**
     * Constructs a new SeriesMatch link.
     *
     * @param seriesId    The ID of the series.
     * @param matchId     The ID of the match.
     * @param matchNumber The order of this match in the series (e.g., 1, 2, 3).
     */
    public SeriesMatch(String seriesId, String matchId, int matchNumber) {
        this.seriesId = seriesId;
        this.matchId = matchId;
        this.matchNumber = matchNumber;
    }

    // --- Getters and Setters ---

    public String getSeriesMatchId() {
        return seriesMatchId;
    }

    public void setSeriesMatchId(String seriesMatchId) {
        this.seriesMatchId = seriesMatchId;
    }

    public String getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(String seriesId) {
        this.seriesId = seriesId;
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public int getMatchNumber() {
        return matchNumber;
    }

    public void setMatchNumber(int matchNumber) {
        this.matchNumber = matchNumber;
    }
}