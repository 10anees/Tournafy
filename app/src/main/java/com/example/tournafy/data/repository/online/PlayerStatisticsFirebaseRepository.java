package com.example.tournafy.data.repository.online;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tournafy.domain.models.statistics.PlayerStatistics;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Firebase Realtime Database repository for ONLINE PlayerStatistics entities.
 */
@Singleton
public class PlayerStatisticsFirebaseRepository extends FirebaseRepository<PlayerStatistics> {

    public static final String DATABASE_PATH = "player_statistics";

    @Inject
    public PlayerStatisticsFirebaseRepository(FirebaseDatabase firebaseDatabase) {
        super(firebaseDatabase, DATABASE_PATH, PlayerStatistics.class);
    }

    @Override
    protected String getEntityId(PlayerStatistics entity) {
        return entity.getStatId(); // Assuming getStatId() is the primary key
    }

    @Override
    public Task<Void> add(PlayerStatistics entity) {
        if (entity.getStatId() == null || entity.getStatId().isEmpty()) {
            String newId = databaseReference.push().getKey();
            entity.setStatId(newId);
        }
        return addOrUpdateWithId(entity.getStatId(), entity);
    }

    public LiveData<List<PlayerStatistics>> getStatisticsByPlayerId(String playerId) {
        MutableLiveData<List<PlayerStatistics>> liveData = new MutableLiveData<>();
        Query query = databaseReference.orderByChild("playerId").equalTo(playerId);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<PlayerStatistics> statsList = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    PlayerStatistics stats = childSnapshot.getValue(PlayerStatistics.class);
                    if (stats != null) {
                        statsList.add(stats);
                    }
                }
                liveData.setValue(statsList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                liveData.setValue(null);
            }
        });
        return liveData;
    }

    public LiveData<List<PlayerStatistics>> getStatisticsByMatchId(String matchId) {
        return getStatisticsByEntityId(matchId);
    }

    public LiveData<List<PlayerStatistics>> getStatisticsByTournamentId(String tournamentId) {
        return getStatisticsByEntityId(tournamentId);
    }

    public LiveData<List<PlayerStatistics>> getStatisticsBySeriesId(String seriesId) {
        return getStatisticsByEntityId(seriesId);
    }

    // Helper method to avoid code duplication since all use 'entityId'
    private LiveData<List<PlayerStatistics>> getStatisticsByEntityId(String entityId) {
        MutableLiveData<List<PlayerStatistics>> liveData = new MutableLiveData<>();
        Query query = databaseReference.orderByChild("entityId").equalTo(entityId);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<PlayerStatistics> statsList = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    PlayerStatistics stats = childSnapshot.getValue(PlayerStatistics.class);
                    if (stats != null) {
                        statsList.add(stats);
                    }
                }
                liveData.setValue(statsList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                liveData.setValue(null);
            }
        });
        return liveData;
    }

    /**
     * Gets statistics for a player in a specific match.
     */
    public LiveData<PlayerStatistics> getPlayerMatchStatistics(String playerId, String matchId) {
        MutableLiveData<PlayerStatistics> liveData = new MutableLiveData<>();
        Query query = databaseReference.orderByChild("playerId").equalTo(playerId);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    PlayerStatistics stats = childSnapshot.getValue(PlayerStatistics.class);
                    // FIX: Check getEntityId() instead of getMatchId()
                    if (stats != null && matchId.equals(stats.getEntityId()) && "MATCH".equals(stats.getEntityType())) {
                        liveData.setValue(stats);
                        return;
                    }
                }
                liveData.setValue(null);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                liveData.setValue(null);
            }
        });
        return liveData;
    }

    public Task<Void> updateStatField(String statsId, String fieldName, Object value) {
        return updateField(statsId, "footballStats/" + fieldName, value);
    }

    public Task<Void> incrementStat(String statsId, String fieldName, int incrementBy) {
        com.google.android.gms.tasks.TaskCompletionSource<Void> taskSource =
                new com.google.android.gms.tasks.TaskCompletionSource<>();

        databaseReference.child(statsId).child("footballStats").child(fieldName)
                .runTransaction(new com.google.firebase.database.Transaction.Handler() {
                    @Override
                    public com.google.firebase.database.Transaction.Result doTransaction(
                            @NonNull com.google.firebase.database.MutableData mutableData) {
                        Integer currentValue = mutableData.getValue(Integer.class);
                        if (currentValue == null) {
                            mutableData.setValue(incrementBy);
                        } else {
                            mutableData.setValue(currentValue + incrementBy);
                        }
                        return com.google.firebase.database.Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {
                        if (error != null) {
                            taskSource.setException(error.toException());
                        } else if (!committed) {
                            taskSource.setException(new Exception("Transaction not committed"));
                        } else {
                            taskSource.setResult(null);
                        }
                    }
                });

        return taskSource.getTask();
    }

    public Task<Void> incrementStatForPlayerInMatch(String playerId, String matchId, String fieldName, int incrementBy) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
        Query query = databaseReference.orderByChild("playerId").equalTo(playerId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String statId = null;
                boolean found = false;
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    PlayerStatistics stats = childSnapshot.getValue(PlayerStatistics.class);
                    if (stats != null && matchId.equals(stats.getEntityId())) {
                        statId = stats.getStatId();
                        found = true;
                        break;
                    }
                }

                if (found) {
                    incrementStat(statId, fieldName, incrementBy).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            tcs.setResult(null);
                        } else {
                            tcs.setException(Objects.requireNonNull(task.getException()));
                        }
                    });
                } else {
                    // If no stats document exists, we don't create one here.
                    // This assumes they are pre-created when a player joins a match/tournament.
                    // To be safe, we'll just complete the task successfully.
                    tcs.setResult(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tcs.setException(error.toException());
            }
        });
        return tcs.getTask();
    }
}