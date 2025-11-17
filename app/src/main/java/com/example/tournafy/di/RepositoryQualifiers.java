package com.example.tournafy.di;

import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Qualifiers to distinguish between Online (Firebase) and Offline (Firestore) repositories.
 */
public class RepositoryQualifiers {

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    public @interface OnlineRepo {}

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    public @interface OfflineRepo {}
}