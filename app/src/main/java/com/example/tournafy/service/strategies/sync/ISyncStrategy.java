package com.example.tournafy.service.strategies.sync;

/**
 * Defines the contract for different conflict resolution strategies
 * when syncing data between offline and online.
 */
public interface ISyncStrategy {

    /**
     * Resolves a conflict between a local and remote entity.
     * @param localEntity  The entity from the local (offline) database.
     * @param remoteEntity The entity from the remote (online) database.
     * @return The resolved entity to be saved in both places.
     */
    <T> T resolveConflict(T localEntity, T remoteEntity);
}