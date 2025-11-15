package com.example.tournafy.domain.models.base;

import com.example.tournafy.domain.enums.EntityStatus;
import java.util.Date;

/**
 * Abstract base class for all entities that can be hosted
 * This includes Match, Tournament, and Series.
 * Maps to the HOSTED_ENTITY table in the EERD. 
 */
public abstract class HostedEntity {

    protected String entityId;
    protected String entityType; // e.g., "MATCH", "TOURNAMENT", "SERIES"
    protected String name;
    protected boolean isOnline; 
    protected Date createdAt;
    protected String hostUserId; 
    protected String status; // Using String to align with EntityStatus enum
    protected String visibilityLink; 

    // Constructors
    public HostedEntity() {
        this.createdAt = new Date();
        this.isOnline = false;
        // this.status = EntityStatus.DRAFT.name();
    }

    public HostedEntity(String entityId, String name, String hostUserId, boolean isOnline, String status) {
        this.entityId = entityId;
        this.name = name;
        this.hostUserId = hostUserId;
        this.isOnline = isOnline;
        this.status = status;
        this.createdAt = new Date();
    }

    // Abstract methods (if any)
    // public abstract void validate();

    // Getters and Setters
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getHostUserId() {
        return hostUserId;
    }

    public void setHostUserId(String hostUserId) {
        this.hostUserId = hostUserId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVisibilityLink() {
        return visibilityLink;
    }

    public void setVisibilityLink(String visibilityLink) {
        this.visibilityLink = visibilityLink;
    }
}