package com.example.tournafy.domain.models.user;

import java.util.Date;

/**
 * Maps to the USER table in the EERD.
 * This class holds information about an authenticated user.
 */
public class User {

    private String userId;
    private String email;
    private String name;
    private String profilePicture;
    private String authProvider; // Using String to align with AuthProvider enum
    private Date createdAt;

    public User() {
        this.createdAt = new Date();
    }

    public User(String userId, String email, String name, String authProvider) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.authProvider = authProvider;
        this.createdAt = new Date();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getAuthProvider() {
        return authProvider;
    }

    public void setAuthProvider(String authProvider) {
        this.authProvider = authProvider;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}