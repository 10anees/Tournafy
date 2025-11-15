package com.example.tournafy.domain.enums;

// Defines permission levels for co-hosts
public enum PermissionLevel {
    FULL_ACCESS,    // Can edit everything including settings
    EDIT_ONLY,      // Can edit scores and data but not settings
    VIEW_ONLY       // Can only view, no editing
}
