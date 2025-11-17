package com.example.tournafy.data.repository.online;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.example.tournafy.domain.models.team.Player;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Firebase Realtime Database repository for ONLINE Player entities.
 * This handles cloud storage of player data with real-time updates.
 */
@Singleton
public class PlayerFirebaseRepository extends FirebaseRepository<Player> {

    public static final String DATABASE_PATH = "players";

    @Inject
    public PlayerFirebaseRepository(FirebaseDatabase firebaseDatabase) {
        super(firebaseDatabase, DATABASE_PATH, Player.class);
    }

    @Override
    protected String getEntityId(Player entity) {
        return entity.getPlayerId();
    }

    @Override
    public Task<Void> add(Player entity) {
        if (entity.getPlayerId() == null || entity.getPlayerId().isEmpty()) {
            String newId = databaseReference.push().getKey();
            entity.setPlayerId(newId);
        }
        return addOrUpdateWithId(entity.getPlayerId(), entity);
    }

    /**
     * Gets all players for a specific team.
     * @param teamId The team ID.
     * @return LiveData holding a list of players.
     */
    public LiveData<List<Player>> getPlayersByTeamId(String teamId) {
        MutableLiveData<List<Player>> liveData = new MutableLiveData<>();
        
        Query query = databaseReference.orderByChild("teamId").equalTo(teamId);
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Player> players = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Player player = childSnapshot.getValue(Player.class);
                    if (player != null) {
                        players.add(player);
                    }
                }
                liveData.setValue(players);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                liveData.setValue(null);
            }
        });
        
        return liveData;
    }

    /**
     * Gets a player by their name within a team.
     * @param teamId The team ID.
     * @param playerName The player name.
     * @return LiveData holding the player.
     */
    public LiveData<Player> getPlayerByName(String teamId, String playerName) {
        MutableLiveData<Player> liveData = new MutableLiveData<>();
        
        Query query = databaseReference.orderByChild("teamId").equalTo(teamId);
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Player player = childSnapshot.getValue(Player.class);
                    if (player != null && playerName.equals(player.getPlayerName())) {
                        liveData.setValue(player);
                        return;
                    }
                }
                liveData.setValue(null);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                liveData.setValue(null);
            }
        });
        
        return liveData;
    }

    /**
     * Searches for players by name across all teams.
     * @param namePrefix The name prefix to search for.
     * @return LiveData holding a list of matching players.
     */
    public LiveData<List<Player>> searchPlayersByName(String namePrefix) {
        MutableLiveData<List<Player>> liveData = new MutableLiveData<>();
        
        Query query = databaseReference.orderByChild("playerName")
                .startAt(namePrefix)
                .endAt(namePrefix + "\uf8ff");
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Player> players = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Player player = childSnapshot.getValue(Player.class);
                    if (player != null) {
                        players.add(player);
                    }
                }
                liveData.setValue(players);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                liveData.setValue(null);
            }
        });
        
        return liveData;
    }

    /**
     * Updates a player's statistics.
     * @param playerId The player ID.
     * @param statisticsId The statistics ID to link.
     * @return Task that completes when operation finishes.
     */
    public Task<Void> updatePlayerStatistics(String playerId, String statisticsId) {
        return updateField(playerId, "currentStatisticsId", statisticsId);
    }
}
