package com.tournafy.domain.interfaces;
//import com.tournafy.domain.models.series.Series;

/**
 * Notifies listeners of changes to the series score (e.g., Team A wins).
 */
public interface SeriesObserver {
    /**
     * @param series The updated Series object.
     */
    void onSeriesScoreUpdated(Series series);
}