package com.example.tournafy.service.interfaces;

// Note: Imports will be valid once domain models are created.
import com.example.tournafy.domain.models.base.HostedEntity;

/**
 * Defines the contract for syncing offline data (from Firestore)
 * to the online database (Firebase).
 * This will use a Strategy Pattern for conflict resolution. 
 */
public interface ISyncService {

    /**
     * Enqueues a specific entity (Match, Tournament, Series) for synchronization.
     * @param entity The HostedEntity to sync.
     */
    void syncEntity(com.example.tournafy.domain.models.base.HostedEntity entity);

    /**
     * Attempts to sync all entities that are marked as pending.
     */
    void syncAllPending();

    /**
     * Checks the current network status to determine if sync is possible.
     * @return true if online, false otherwise.
     */
    boolean isNetworkAvailable();
}