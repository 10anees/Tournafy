package com.example.tournafy.domain.models.user;

import com.google.firebase.firestore.Exclude;
import java.io.Serializable;

/**
 * Represents a Co-Host relationship between a User and a HostedEntity (Match/Tournament/Series).
 * Mapped from EERD: CO_HOST entity
 */
public class CoHost implements Serializable {

    private String coHostId;       // PK: co_host_id
    private String hostedEntityId; // FK: entity_id (The Match/Tournament/Series ID)
    private String coHostUserId;   // FK: user_id (The User ID of the co-host)
    private long invitedAt;        // datetime: invited_at
    private String permissionLevel;// string: permission_level (READ, WRITE, ADMIN) 
    private String status;         // string: status (PENDING, ACCEPTED, REJECTED) 
    private long acceptedAt;       // datetime: accepted_at

    // Default constructor required for Firebase/Firestore serialization
    public CoHost() {
    }

    public CoHost(String coHostId, String hostedEntityId, String coHostUserId, long invitedAt, String permissionLevel, String status) {
        this.coHostId = coHostId;
        this.hostedEntityId = hostedEntityId;
        this.coHostUserId = coHostUserId;
        this.invitedAt = invitedAt;
        this.permissionLevel = permissionLevel;
        this.status = status;
    }

    // --- Getters and Setters ---

    public String getCoHostId() {
        return coHostId;
    }

    public void setCoHostId(String coHostId) {
        this.coHostId = coHostId;
    }

    /**
     * Helper method for Generic Repository usage.
     * Returns the Primary Key of this entity.
     */
    @Exclude
    public String getEntityId() {
        return coHostId;
    }

    @Exclude
    public void setEntityId(String id) {
        this.coHostId = id;
    }

    public String getHostedEntityId() {
        return hostedEntityId;
    }

    public void setHostedEntityId(String hostedEntityId) {
        this.hostedEntityId = hostedEntityId;
    }

    public String getCoHostUserId() {
        return coHostUserId;
    }

    public void setCoHostUserId(String coHostUserId) {
        this.coHostUserId = coHostUserId;
    }

    public long getInvitedAt() {
        return invitedAt;
    }

    public void setInvitedAt(long invitedAt) {
        this.invitedAt = invitedAt;
    }

    public String getPermissionLevel() {
        return permissionLevel;
    }

    public void setPermissionLevel(String permissionLevel) {
        this.permissionLevel = permissionLevel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(long acceptedAt) {
        this.acceptedAt = acceptedAt;
    }
}