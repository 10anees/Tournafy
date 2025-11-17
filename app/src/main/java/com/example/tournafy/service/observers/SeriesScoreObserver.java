package com.example.tournafy.service.observers;

import com.example.tournafy.domain.interfaces.SeriesObserver;
import com.example.tournafy.domain.models.series.Series;
import com.example.tournafy.service.interfaces.IHostingService;

/**
 * Implements the SeriesObserver pattern.
 * Delegates updates to the HostingService.
 */
public class SeriesScoreObserver implements SeriesObserver {

    private final IHostingService hostingService;
    private final String seriesId;

    public SeriesScoreObserver(IHostingService hostingService, String seriesId) {
        this.hostingService = hostingService;
        this.seriesId = seriesId;
    }

    @Override
    public void onSeriesScoreUpdated(Series series) {
        if (hostingService != null && series != null) {
            // FIX: Added the required HostingCallback parameter
            hostingService.updateSeries(series, new IHostingService.HostingCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    // Update successful
                }

                @Override
                public void onError(Exception e) {
                    // Log error or handle failure
                }
            });
        }
    }
}