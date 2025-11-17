package com.example.tournafy.data.repository.online;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.example.tournafy.domain.models.match.cricket.Ball;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Firebase Realtime Database repository for ONLINE Ball entities.
 * This handles cloud storage of individual cricket ball deliveries with real-time updates.
 * 
 * CRITICAL DESIGN: Each ball is stored as a separate document for scalability.
 * This allows viewers to receive real-time updates for each ball without
 * re-downloading the entire match object, similar to ESPN Cricinfo's live ball-by-ball.
 */
@Singleton
public class BallFirebaseRepository extends FirebaseRepository<Ball> {

    public static final String DATABASE_PATH = "balls";

    @Inject
    public BallFirebaseRepository(FirebaseDatabase firebaseDatabase) {
        super(firebaseDatabase, DATABASE_PATH, Ball.class);
    }

    @Override
    protected String getEntityId(Ball entity) {
        return entity.getBallId();
    }

    @Override
    public Task<Void> add(Ball entity) {
        if (entity.getBallId() == null || entity.getBallId().isEmpty()) {
            String newId = databaseReference.push().getKey();
            entity.setBallId(newId);
        }
        return addOrUpdateWithId(entity.getBallId(), entity);
    }

    /**
     * Gets all balls for a specific over with real-time updates.
     * @param overId The over ID.
     * @return LiveData holding a list of balls ordered by ball number.
     */
    public LiveData<List<Ball>> getBallsByOverId(String overId) {
        MutableLiveData<List<Ball>> liveData = new MutableLiveData<>();
        
        Query query = databaseReference.orderByChild("overId").equalTo(overId);
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Ball> balls = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Ball ball = childSnapshot.getValue(Ball.class);
                    if (ball != null) {
                        balls.add(ball);
                    }
                }
                // Sort by ball number
                balls.sort((b1, b2) -> Integer.compare(b1.getBallNumber(), b2.getBallNumber()));
                liveData.setValue(balls);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                liveData.setValue(null);
            }
        });
        
        return liveData;
    }

    /**
     * Gets all balls for a specific innings with real-time updates.
     * @param inningsId The innings ID.
     * @return LiveData holding a list of balls.
     */
    public LiveData<List<Ball>> getBallsByInningsId(String inningsId) {
        MutableLiveData<List<Ball>> liveData = new MutableLiveData<>();
        
        Query query = databaseReference.orderByChild("inningsId").equalTo(inningsId);
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Ball> balls = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Ball ball = childSnapshot.getValue(Ball.class);
                    if (ball != null) {
                        balls.add(ball);
                    }
                }
                // Sort by over number, then ball number
                balls.sort((b1, b2) -> {
                    int overCompare = Integer.compare(b1.getOverNumber(), b2.getOverNumber());
                    if (overCompare != 0) return overCompare;
                    return Integer.compare(b1.getBallNumber(), b2.getBallNumber());
                });
                liveData.setValue(balls);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                liveData.setValue(null);
            }
        });
        
        return liveData;
    }

    /**
     * Gets all balls for a specific match with real-time updates.
     * @param matchId The match ID.
     * @return LiveData holding a list of balls.
     */
    public LiveData<List<Ball>> getBallsByMatchId(String matchId) {
        MutableLiveData<List<Ball>> liveData = new MutableLiveData<>();
        
        Query query = databaseReference.orderByChild("matchId").equalTo(matchId);
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Ball> balls = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Ball ball = childSnapshot.getValue(Ball.class);
                    if (ball != null) {
                        balls.add(ball);
                    }
                }
                // Sort by innings, over, then ball number
                balls.sort((b1, b2) -> {
                    int inningsCompare = Integer.compare(b1.getInningsNumber(), b2.getInningsNumber());
                    if (inningsCompare != 0) return inningsCompare;
                    int overCompare = Integer.compare(b1.getOverNumber(), b2.getOverNumber());
                    if (overCompare != 0) return overCompare;
                    return Integer.compare(b1.getBallNumber(), b2.getBallNumber());
                });
                liveData.setValue(balls);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                liveData.setValue(null);
            }
        });
        
        return liveData;
    }

    /**
     * Gets all wicket balls for a match with real-time updates.
     * @param matchId The match ID.
     * @return LiveData holding a list of wicket balls.
     */
    public LiveData<List<Ball>> getWicketBalls(String matchId) {
        MutableLiveData<List<Ball>> liveData = new MutableLiveData<>();
        
        Query query = databaseReference.orderByChild("matchId").equalTo(matchId);
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Ball> balls = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Ball ball = childSnapshot.getValue(Ball.class);
                    if (ball != null && ball.isWicket()) {
                        balls.add(ball);
                    }
                }
                balls.sort((b1, b2) -> {
                    int inningsCompare = Integer.compare(b1.getInningsNumber(), b2.getInningsNumber());
                    if (inningsCompare != 0) return inningsCompare;
                    return Integer.compare(b1.getOverNumber(), b2.getOverNumber());
                });
                liveData.setValue(balls);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                liveData.setValue(null);
            }
        });
        
        return liveData;
    }

    /**
     * Gets all boundary balls for a match with real-time updates.
     * @param matchId The match ID.
     * @return LiveData holding a list of boundary balls.
     */
    public LiveData<List<Ball>> getBoundaryBalls(String matchId) {
        MutableLiveData<List<Ball>> liveData = new MutableLiveData<>();
        
        Query query = databaseReference.orderByChild("matchId").equalTo(matchId);
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Ball> balls = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Ball ball = childSnapshot.getValue(Ball.class);
                    if (ball != null && ball.isBoundary()) {
                        balls.add(ball);
                    }
                }
                balls.sort((b1, b2) -> {
                    int inningsCompare = Integer.compare(b1.getInningsNumber(), b2.getInningsNumber());
                    if (inningsCompare != 0) return inningsCompare;
                    return Integer.compare(b1.getOverNumber(), b2.getOverNumber());
                });
                liveData.setValue(balls);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                liveData.setValue(null);
            }
        });
        
        return liveData;
    }
}
