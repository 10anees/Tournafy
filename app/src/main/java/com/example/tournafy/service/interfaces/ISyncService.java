package com.example.tournafy.service.interfaces;

import com.example.tournafy.domain.models.base.HostedEntity;
import com.example.tournafy.domain.models.sync.SyncLog;
import com.example.tournafy.service.strategies.sync.ISyncStrategy;
import androidx.lifecycle.LiveData;
import java.util.List;

/**
 * Service interface for handling data synchronization. 
 * Implements the Strategy Pattern for different sync conflict
 * resolutions.
 */
public interface ISyncService {

    /**
     * Syncs a locally hosted entity (match, tournament, series)
     * to the online Firebase repository.
     *
     * @param entity   The HostedEntity to be synced.
     * @param strategy The sync strategy to use for conflict resolution.
     * @param callback Callback to signal success or error.
     */
    void syncEntityToOnline(HostedEntity entity, ISyncStrategy strategy, SyncCallback<Void> callback);

    /**
     * Downloads an online entity for offline viewing (not editing).
     *
     * @param entityId The ID of the entity to download.
     * @param callback Callback to return the downloaded entity or an error.
     */
    void syncEntityToOffline(String entityId, SyncCallback<HostedEntity> callback);

    /**
     * Checks for any pending or failed sync operations.
     *
     * @return LiveData wrapping a list of SyncLog objects.
     */
    LiveData<List<SyncLog>> getPendingSyncs();

    /**
     * Retries a failed sync operation.
     *
     * @param syncLog The log entry for the failed sync.
     * @param strategy The strategy to use for this retry.
     * @param callback Callback to signal success or error.
     */
    void retrySync(SyncLog syncLog, ISyncStrategy strategy, SyncCallback<Void> callback);

    /**
     * A generic callback interface for sync operations.
     *
     * @param <T> The type of the successful result.
     */
    interface SyncCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }
}