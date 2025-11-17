package com.example.tournafy.data.repository.online;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.example.tournafy.domain.models.team.Team;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Firebase Realtime Database repository for ONLINE Team entities.
 * This is used for online team profiles and data synchronization.
 *
 * Note: This is different from TeamFirestoreRepository (offline).
 * - Uses Firebase Realtime Database for cloud storage
 * - No offline persistence
 * - Optimized for real-time updates
 * - Can be used for public team profile viewing
 */
@Singleton
public class TeamFirebaseRepository extends FirebaseRepository<Team> {

    public static final String DATABASE_PATH = "teams";

    @Inject
    public TeamFirebaseRepository(FirebaseDatabase firebaseDatabase) {
        super(firebaseDatabase, DATABASE_PATH, Team.class);
    }

    @Override
    protected String getEntityId(Team entity) {
        return entity.getTeamId();
    }

    /**
     * Overrides the generic 'add' method to ensure a Team ID is set.
     */
    @Override
    public Task<Void> add(Team entity) {
        // If the entity doesn't have an ID, create one.
        if (entity.getTeamId() == null || entity.getTeamId().isEmpty()) {
            String newId = databaseReference.push().getKey();
            entity.setTeamId(newId);
        }
        return addOrUpdateWithId(entity.getTeamId(), entity);
    }

    /**
     * Gets teams by owner user ID.
     * @param ownerId The ID of the user who owns the teams.
     * @return LiveData holding a list of teams.
     */
    public LiveData<List<Team>> getTeamsByOwnerId(String ownerId) {
        MutableLiveData<List<Team>> liveData = new MutableLiveData<>();
        
        Query query = databaseReference.orderByChild("ownerId").equalTo(ownerId);
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Team> teams = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Team team = childSnapshot.getValue(Team.class);
                    if (team != null) {
                        teams.add(team);
                    }
                }
                liveData.setValue(teams);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                liveData.setValue(null);
            }
        });
        
        return liveData;
    }

    /**
     * Gets teams by sport type.
     * @param sportType The sport type (e.g., "CRICKET", "FOOTBALL").
     * @return LiveData holding a list of teams.
     */
    public LiveData<List<Team>> getTeamsBySport(String sportType) {
        MutableLiveData<List<Team>> liveData = new MutableLiveData<>();
        
        Query query = databaseReference.orderByChild("sportType").equalTo(sportType);
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Team> teams = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Team team = childSnapshot.getValue(Team.class);
                    if (team != null) {
                        teams.add(team);
                    }
                }
                liveData.setValue(teams);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                liveData.setValue(null);
            }
        });
        
        return liveData;
    }

    /**
     * Gets a team by its name.
     * @param teamName The team name to search for.
     * @return LiveData holding the team.
     */
    public LiveData<Team> getTeamByName(String teamName) {
        MutableLiveData<Team> liveData = new MutableLiveData<>();
        
        Query query = databaseReference.orderByChild("teamName").equalTo(teamName);
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Team team = childSnapshot.getValue(Team.class);
                    liveData.setValue(team);
                    return; // Return first match
                }
                liveData.setValue(null); // No team found
            }

            @Override
            public void onCancelled(DatabaseError error) {
                liveData.setValue(null);
            }
        });
        
        return liveData;
    }

    /**
     * Searches for teams by a partial name match.
     * @param namePrefix The prefix to search for.
     * @return LiveData holding a list of matching teams.
     */
    public LiveData<List<Team>> searchTeamsByName(String namePrefix) {
        MutableLiveData<List<Team>> liveData = new MutableLiveData<>();
        
        Query query = databaseReference.orderByChild("teamName")
                .startAt(namePrefix)
                .endAt(namePrefix + "\uf8ff");
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Team> teams = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Team team = childSnapshot.getValue(Team.class);
                    if (team != null) {
                        teams.add(team);
                    }
                }
                liveData.setValue(teams);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                liveData.setValue(null);
            }
        });
        
        return liveData;
    }

    /**
     * Updates the team's logo URL.
     * @param teamId The team ID.
     * @param logoUrl The new logo URL.
     * @return Task that completes when operation finishes.
     */
    public Task<Void> updateTeamLogo(String teamId, String logoUrl) {
        return updateField(teamId, "logoUrl", logoUrl);
    }

    /**
     * Updates the team's player roster.
     * @param teamId The team ID.
     * @param playerIds The list of player IDs.
     * @return Task that completes when operation finishes.
     */
    public Task<Void> updatePlayerRoster(String teamId, List<String> playerIds) {
        return updateField(teamId, "playerIds", playerIds);
    }
}
