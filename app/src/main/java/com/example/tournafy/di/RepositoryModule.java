package com.example.tournafy.di;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.tournafy.data.repository.offline.*;
import com.example.tournafy.data.repository.online.*;

// --- FIXED IMPORTS HERE ---
import com.example.tournafy.di.RepositoryQualifiers.OfflineRepo;
import com.example.tournafy.di.RepositoryQualifiers.OnlineRepo;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;

@Module
@InstallIn(SingletonComponent.class)
public class RepositoryModule {

    // --- 1. Ball Repositories ---
    @Provides
    @Singleton
    @OnlineRepo
    public BallFirebaseRepository provideBallFirebaseRepository(FirebaseDatabase db) {
        return new BallFirebaseRepository(db);
    }

    @Provides
    @Singleton
    @OfflineRepo
    public BallFirestoreRepository provideBallFirestoreRepository(FirebaseFirestore fs) {
        return new BallFirestoreRepository(fs);
    }

    // --- 2. CoHost Repositories ---
    @Provides
    @Singleton
    @OnlineRepo
    public CoHostFirebaseRepository provideCoHostFirebaseRepository(FirebaseDatabase db) {
        return new CoHostFirebaseRepository(db);
    }

    @Provides
    @Singleton
    @OfflineRepo
    public CoHostFirestoreRepository provideCoHostFirestoreRepository(FirebaseFirestore fs) {
        return new CoHostFirestoreRepository(fs);
    }

    // --- 3. FootballEvent Repositories ---
    @Provides
    @Singleton
    @OnlineRepo
    public FootballEventFirebaseRepository provideFootballEventFirebaseRepository(FirebaseDatabase db) {
        return new FootballEventFirebaseRepository(db);
    } 

    @Provides
    @Singleton
    @OfflineRepo
    public FootballEventFirestoreRepository provideFootballEventFirestoreRepository(FirebaseFirestore fs) {
        return new FootballEventFirestoreRepository(fs);
    }

    // --- 4. Innings Repositories ---
    @Provides
    @Singleton
    @OnlineRepo
    public InningsFirebaseRepository provideInningsFirebaseRepository(FirebaseDatabase db) {
        return new InningsFirebaseRepository(db);
    }      
    
    @Provides
    @Singleton
    @OfflineRepo
    public InningsFirestoreRepository provideInningsFirestoreRepository(FirebaseFirestore fs) {
        return new InningsFirestoreRepository(fs);
    }

    // --- 5. Match Repositories ---
    @Provides
    @Singleton
    @OnlineRepo
    public MatchFirebaseRepository provideMatchFirebaseRepository(FirebaseDatabase db) {
        return new MatchFirebaseRepository(db);
    }

    @Provides
    @Singleton
    @OfflineRepo
    public MatchFirestoreRepository provideMatchFirestoreRepository(FirebaseFirestore fs) {
        return new MatchFirestoreRepository(fs);
    }

    // --- 6. Over Repositories ---
    @Provides
    @Singleton
    @OnlineRepo
    public OverFirebaseRepository provideOverFirebaseRepository(FirebaseDatabase db) {
        return new OverFirebaseRepository(db);
    }

    @Provides
    @Singleton
    @OfflineRepo
    public OverFirestoreRepository provideOverFirestoreRepository(FirebaseFirestore fs) {
        return new OverFirestoreRepository(fs);
    }

    // --- 7. Player Repositories ---
    @Provides
    @Singleton
    @OnlineRepo
    public PlayerFirebaseRepository providePlayerFirebaseRepository(FirebaseDatabase db) {
        return new PlayerFirebaseRepository(db);
    }

    @Provides
    @Singleton
    @OfflineRepo
    public PlayerFirestoreRepository providePlayerFirestoreRepository(FirebaseFirestore fs) {
        return new PlayerFirestoreRepository(fs);
    }

    // --- 8. PlayerStatistics Repositories ---
    @Provides
    @Singleton
    @OnlineRepo
    public PlayerStatisticsFirebaseRepository providePlayerStatsFirebaseRepo(FirebaseDatabase db) {
        return new PlayerStatisticsFirebaseRepository(db);
    }

    @Provides
    @Singleton
    @OfflineRepo
    public PlayerStatisticsFirestoreRepository providePlayerStatsFirestoreRepo(FirebaseFirestore fs) {
        return new PlayerStatisticsFirestoreRepository(fs);
    }

    // --- 9. Series Repositories ---
    @Provides
    @Singleton
    @OfflineRepo
    public SeriesFirestoreRepository provideSeriesFirestoreRepository(FirebaseFirestore fs) {
        return new SeriesFirestoreRepository(fs);
    }

    @Provides
    @Singleton
    @OnlineRepo
    public SeriesFirebaseRepository provideSeriesFirebaseRepository(FirebaseDatabase db) {
        return new SeriesFirebaseRepository(db);
    }

    // --- 10. SyncLog Repositories ---
    @Provides
    @Singleton
    @OnlineRepo
    public SyncLogFirebaseRepository provideSyncLogFirebaseRepository(FirebaseDatabase db) {
        return new SyncLogFirebaseRepository(db);
    }   

    @Provides
    @Singleton
    @OfflineRepo
    public SyncLogFirestoreRepository provideSyncLogFirestoreRepository(FirebaseFirestore fs) {
        return new SyncLogFirestoreRepository(fs);
    }   

    // --- 11. Team Repositories ---
    @Provides
    @Singleton
    @OnlineRepo
    public TeamFirebaseRepository provideTeamFirebaseRepository(FirebaseDatabase db) {
        return new TeamFirebaseRepository(db);
    }

    @Provides
    @Singleton
    @OfflineRepo
    public TeamFirestoreRepository provideTeamFirestoreRepository(FirebaseFirestore fs) {
        return new TeamFirestoreRepository(fs);
    }

    // --- 12. Tournament Repositories ---
    @Provides
    @Singleton
    @OnlineRepo
    public TournamentFirebaseRepository provideTournamentFirebaseRepository(FirebaseDatabase db) {
        return new TournamentFirebaseRepository(db);
    }   

    @Provides
    @Singleton
    @OfflineRepo
    public TournamentFirestoreRepository provideTournamentFirestoreRepository(FirebaseFirestore fs) {
        return new TournamentFirestoreRepository(fs);
    }

    // --- 13. User Repositories ---
    @Provides
    @Singleton
    @OnlineRepo
    public UserFirebaseRepository provideUserFirebaseRepository(FirebaseDatabase db) {
        return new UserFirebaseRepository(db);
    }

    @Provides
    @Singleton
    @OfflineRepo
    public UserFirestoreRepository provideUserFirestoreRepository(FirebaseFirestore fs) {
        return new UserFirestoreRepository(fs);
    }   
}