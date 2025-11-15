package com.example.tournafy.domain.models.sync;

import java.util.Date;

/**
 * Domain Model for a Sync Log.
 * Corresponds to SYNC_LOG in the EERD.
 * Tracks the synchronization status of offline entities.
 */
public class SyncLog {

    private String syncId;
    private String entityId; // FK to the entity that was synced
    private String entityType; // "MATCH", "TOURNAMENT", etc.
    private boolean isSynced;
    private Date syncedAt;
    private String syncDirection; // "UP", "DOWN"
    private String errorMessage;
    private int retryCount;

    // No-arg constructor for Firestore
    public SyncLog() {}

    // Getters and Setters
    public String getSyncId() {
        return syncId;
    }

    public void setSyncId(String syncId) {
        this.syncId = syncId;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced(boolean synced) {
        isSynced = synced;
    }

    public Date getSyncedAt() {
        return syncedAt;
    }

    public void setSyncedAt(Date syncedAt) {
        this.syncedAt = syncedAt;
    }

    public String getSyncDirection() {
        return syncDirection;
    }

    public void setSyncDirection(String syncDirection) {
        this.syncDirection = syncDirection;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
}