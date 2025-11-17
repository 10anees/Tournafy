package com.example.tournafy.data.repository.online;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.example.tournafy.domain.models.tournament.Tournament;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Firebase Realtime Database repository for ONLINE Tournament entities.
 * This is used when a tournament is hosted online (isOnline=true) and needs to be
 * accessible via a link for real-time viewing of standings, brackets, and matches.
 *
 * Differences from TournamentFirestoreRepository (offline):
 * - Uses Firebase Realtime Database for cloud storage
 * - No offline persistence
 * - Optimized for real-time updates
 * - Accessible via visibility links
 */
@Singleton
public class TournamentFirebaseRepository extends FirebaseRepository<Tournament> {

    public static final String DATABASE_PATH = "tournaments";

    @Inject
    public TournamentFirebaseRepository(FirebaseDatabase firebaseDatabase) {
        super(firebaseDatabase, DATABASE_PATH, Tournament.class);
    }

    @Override
    protected String getEntityId(Tournament entity) {
        return entity.getEntityId();
    }

    /**
     * Overrides the generic 'add' method to ensure a Tournament ID is set.
     */
    @Override
    public Task<Void> add(Tournament entity) {
        // If the entity doesn't have an ID, create one.
        if (entity.getEntityId() == null || entity.getEntityId().isEmpty()) {
            String newId = databaseReference.push().getKey();
            entity.setEntityId(newId);
        }
        return addOrUpdateWithId(entity.getEntityId(), entity);
    }

    /**
     * Gets all online tournaments hosted by a specific user.
     * @param hostId The ID of the host user.
     * @return LiveData holding a list of tournaments.
     */
    public LiveData<List<Tournament>> getTournamentsByHostId(String hostId) {
        MutableLiveData<List<Tournament>> liveData = new MutableLiveData<>();
        
        Query query = databaseReference.orderByChild("hostUserId").equalTo(hostId);
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Tournament> tournaments = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Tournament tournament = childSnapshot.getValue(Tournament.class);
                    if (tournament != null) {
                        tournaments.add(tournament);
                    }
                }
                liveData.setValue(tournaments);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                liveData.setValue(null);
            }
        });
        
        return liveData;
    }

    /**
     * Gets all active (ongoing) tournaments.
     * This can be used for a "Live Tournaments" feed.
     * @return LiveData holding a list of active tournaments.
     */
    public LiveData<List<Tournament>> getActiveTournaments() {
        MutableLiveData<List<Tournament>> liveData = new MutableLiveData<>();
        
        Query query = databaseReference.orderByChild("status").equalTo("ACTIVE");
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Tournament> tournaments = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Tournament tournament = childSnapshot.getValue(Tournament.class);
                    if (tournament != null) {
                        tournaments.add(tournament);
                    }
                }
                liveData.setValue(tournaments);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                liveData.setValue(null);
            }
        });
        
        return liveData;
    }

    /**
     * Gets a tournament by its visibility link.
     * This is the primary way viewers access online tournaments.
     * @param visibilityLink The unique link for the tournament.
     * @return LiveData holding the tournament.
     */
    public LiveData<Tournament> getTournamentByVisibilityLink(String visibilityLink) {
        MutableLiveData<Tournament> liveData = new MutableLiveData<>();
        
        Query query = databaseReference.orderByChild("visibilityLink").equalTo(visibilityLink);
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Tournament tournament = childSnapshot.getValue(Tournament.class);
                    liveData.setValue(tournament);
                    return; // Only one tournament should have this link
                }
                liveData.setValue(null); // No tournament found
            }

            @Override
            public void onCancelled(DatabaseError error) {
                liveData.setValue(null);
            }
        });
        
        return liveData;
    }

    /**
     * Updates only the tournament status field.
     * More efficient than updating the entire tournament object.
     * @param tournamentId The tournament ID.
     * @param newStatus The new status (e.g., "ACTIVE", "COMPLETED").
     * @return Task that completes when operation finishes.
     */
    public Task<Void> updateTournamentStatus(String tournamentId, String newStatus) {
        return updateField(tournamentId, "status", newStatus);
    }
}
