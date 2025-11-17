package com.example.tournafy.data.repository.offline;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.tournafy.domain.models.team.Player;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Firestore repository for OFFLINE Player entities.
 */
@Singleton
public class PlayerFirestoreRepository extends FirestoreRepository<Player> {

    public static final String COLLECTION_NAME = "players";

    @Inject
    public PlayerFirestoreRepository(FirebaseFirestore firestore) {
        super(firestore, COLLECTION_NAME, Player.class);
    }

    @Override
    protected String getEntityId(Player entity) {
        return entity.getPlayerId();
    }

    public LiveData<List<Player>> getPlayersByTeamId(String teamId) {
        MutableLiveData<List<Player>> liveData = new MutableLiveData<>();
        collectionReference.whereEqualTo("teamId", teamId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        liveData.setValue(null);
                        return;
                    }
                    if (snapshot != null) {
                        liveData.setValue(snapshot.toObjects(Player.class));
                    }
                });
        return liveData;
    }

    public Task<Player> getPlayerByName(String teamId, String playerName) {
        return collectionReference
                .whereEqualTo("teamId", teamId)
                .whereEqualTo("playerName", playerName)
                .limit(1)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        return task.getResult().getDocuments().get(0).toObject(Player.class);
                    }
                    return null;
                });
    }

    public Task<List<Player>> searchPlayersByName(String namePrefix) {
        return collectionReference
                .orderBy("playerName")
                .startAt(namePrefix)
                .endAt(namePrefix + "\uf8ff")
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        return task.getResult().toObjects(Player.class);
                    }
                    return new ArrayList<>();
                });
    }

    /**
     * Updates a player's statistics.
     */
    public Task<Void> updatePlayerStatistics(String playerId, String statisticsId) {
        // FIX: Use direct Firestore update call instead of missing updateField helper
        return collectionReference.document(playerId).update("currentStatisticsId", statisticsId);
    }
}