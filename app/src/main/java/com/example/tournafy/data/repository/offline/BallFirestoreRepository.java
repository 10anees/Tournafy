package com.example.tournafy.data.repository.offline;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.example.tournafy.domain.models.match.cricket.Ball;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Firestore repository for OFFLINE Ball entities.
 * This handles local storage of individual cricket ball deliveries with offline persistence.
 * Each ball is stored as a separate document for scalability and fast real-time updates.
 */
@Singleton
public class BallFirestoreRepository extends FirestoreRepository<Ball> {

    public static final String COLLECTION_NAME = "balls";

    @Inject
    public BallFirestoreRepository(FirebaseFirestore firestore) {
        super(firestore, COLLECTION_NAME, Ball.class);
    }

    @Override
    protected String getEntityId(Ball entity) {
        return entity.getBallId();
    }

    /**
     * Gets all balls for a specific over.
     * @param overId The over ID.
     * @return LiveData holding a list of balls ordered by ball number.
     */
    public LiveData<List<Ball>> getBallsByOverId(String overId) {
        MutableLiveData<List<Ball>> liveData = new MutableLiveData<>();
        
        collectionReference.whereEqualTo("overId", overId)
                .orderBy("ballNumber", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        liveData.setValue(null);
                        return;
                    }
                    if (snapshot != null) {
                        List<Ball> balls = snapshot.toObjects(Ball.class);
                        liveData.setValue(balls);
                    }
                });
        
        return liveData;
    }

    /**
     * Gets all balls for a specific innings.
     * @param inningsId The innings ID.
     * @return LiveData holding a list of balls.
     */
    public LiveData<List<Ball>> getBallsByInningsId(String inningsId) {
        MutableLiveData<List<Ball>> liveData = new MutableLiveData<>();
        
        collectionReference.whereEqualTo("inningsId", inningsId)
                .orderBy("overNumber", Query.Direction.ASCENDING)
                .orderBy("ballNumber", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        liveData.setValue(null);
                        return;
                    }
                    if (snapshot != null) {
                        List<Ball> balls = snapshot.toObjects(Ball.class);
                        liveData.setValue(balls);
                    }
                });
        
        return liveData;
    }

    /**
     * Gets all balls for a specific match.
     * @param matchId The match ID.
     * @return LiveData holding a list of balls.
     */
    public LiveData<List<Ball>> getBallsByMatchId(String matchId) {
        MutableLiveData<List<Ball>> liveData = new MutableLiveData<>();
        
        collectionReference.whereEqualTo("matchId", matchId)
                .orderBy("inningsNumber", Query.Direction.ASCENDING)
                .orderBy("overNumber", Query.Direction.ASCENDING)
                .orderBy("ballNumber", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        liveData.setValue(null);
                        return;
                    }
                    if (snapshot != null) {
                        List<Ball> balls = snapshot.toObjects(Ball.class);
                        liveData.setValue(balls);
                    }
                });
        
        return liveData;
    }

    /**
     * Gets a specific ball by over ID and ball number.
     * @param overId The over ID.
     * @param ballNumber The ball number.
     * @return Task holding the ball.
     */
    public Task<Ball> getBallByNumber(String overId, int ballNumber) {
        return collectionReference
                .whereEqualTo("overId", overId)
                .whereEqualTo("ballNumber", ballNumber)
                .limit(1)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        return task.getResult().getDocuments().get(0).toObject(Ball.class);
                    }
                    return null;
                });
    }

    /**
     * Gets all wicket balls for a match.
     * @param matchId The match ID.
     * @return LiveData holding a list of wicket balls.
     */
    public LiveData<List<Ball>> getWicketBalls(String matchId) {
        MutableLiveData<List<Ball>> liveData = new MutableLiveData<>();
        
        collectionReference.whereEqualTo("matchId", matchId)
                .whereEqualTo("isWicket", true)
                .orderBy("inningsNumber", Query.Direction.ASCENDING)
                .orderBy("overNumber", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        liveData.setValue(null);
                        return;
                    }
                    if (snapshot != null) {
                        List<Ball> balls = snapshot.toObjects(Ball.class);
                        liveData.setValue(balls);
                    }
                });
        
        return liveData;
    }

    /**
     * Gets all boundary balls for a match.
     * @param matchId The match ID.
     * @return LiveData holding a list of boundary balls.
     */
    public LiveData<List<Ball>> getBoundaryBalls(String matchId) {
        MutableLiveData<List<Ball>> liveData = new MutableLiveData<>();
        
        collectionReference.whereEqualTo("matchId", matchId)
                .whereEqualTo("isBoundary", true)
                .orderBy("inningsNumber", Query.Direction.ASCENDING)
                .orderBy("overNumber", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        liveData.setValue(null);
                        return;
                    }
                    if (snapshot != null) {
                        List<Ball> balls = snapshot.toObjects(Ball.class);
                        liveData.setValue(balls);
                    }
                });
        
        return liveData;
    }
}
