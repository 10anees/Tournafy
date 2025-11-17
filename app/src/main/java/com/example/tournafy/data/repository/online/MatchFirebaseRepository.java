package com.example.tournafy.data.repository.online;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.example.tournafy.domain.models.base.Match;
import com.example.tournafy.domain.models.match.cricket.CricketMatch;
import com.example.tournafy.domain.models.match.football.FootballMatch;
import com.example.tournafy.domain.models.sport.SportTypeEnum;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Firebase Realtime Database repository for ONLINE Match entities.
 * This is used when a match is hosted online (isOnline=true) and needs to be
 * accessible via a link for real-time viewing by multiple users.
 * Differences from MatchFirestoreRepository (offline):
 * - Uses Firebase Realtime Database for cloud storage
 * - No offline persistence
 * - Optimized for real-time updates
 * - Accessible via visibility links
 */
@Singleton
public class MatchFirebaseRepository extends FirebaseRepository<Match> {

    public static final String DATABASE_PATH = "matches";

    @Inject
    public MatchFirebaseRepository(FirebaseDatabase firebaseDatabase) {
        super(firebaseDatabase, DATABASE_PATH, Match.class);
    }

    @Override
    protected String getEntityId(Match entity) {
        return entity.getEntityId();
    }
    
    /**
     * CRITICAL: Deserializes a DataSnapshot to the correct concrete Match subclass.
     * This prevents crashes from trying to instantiate abstract Match class.
     * @param snapshot The DataSnapshot containing match data.
     * @return The correct Match subclass instance (CricketMatch or FootballMatch).
     */
    private Match deserializeMatch(DataSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) {
            return null;
        }
        
        // Read the sportType field to determine which class to use
        String sportType = snapshot.child("sportType").getValue(String.class);
        
        if (sportType == null) {
            return null;
        }
        
        // Deserialize to the correct concrete class
        if (SportTypeEnum.CRICKET.name().equals(sportType)) {
            return snapshot.getValue(CricketMatch.class);
        } else if (SportTypeEnum.FOOTBALL.name().equals(sportType)) {
            return snapshot.getValue(FootballMatch.class);
        }
        
        return null;
    }
    
    /**
     * OVERRIDE: Custom implementation that handles polymorphism.
     */
    @Override
    public LiveData<Match> getById(String id) {
        MutableLiveData<Match> liveData = new MutableLiveData<>();
        
        databaseReference.child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Match match = deserializeMatch(snapshot);
                liveData.setValue(match);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                liveData.setValue(null);
            }
        });
        
        return liveData;
    }
    
    /**
     * OVERRIDE: Custom implementation that handles polymorphism.
     */
    @Override
    public LiveData<List<Match>> getAll() {
        MutableLiveData<List<Match>> liveData = new MutableLiveData<>();
        
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Match> matches = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Match match = deserializeMatch(childSnapshot);
                    if (match != null) {
                        matches.add(match);
                    }
                }
                liveData.setValue(matches);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                liveData.setValue(null);
            }
        });
        
        return liveData;
    }

    /**
     * Overrides the generic 'add' method to ensure a Match ID is set.
     */
    @Override
    public Task<Void> add(Match entity) {
        // If the entity doesn't have an ID, create one.
        if (entity.getEntityId() == null || entity.getEntityId().isEmpty()) {
            String newId = databaseReference.push().getKey();
            entity.setEntityId(newId);
        }
        return addOrUpdateWithId(entity.getEntityId(), entity);
    }

    /**
     * Gets all online matches hosted by a specific user.
     * This uses Firebase queries for efficient filtering.
     * FIXED: Uses deserializeMatch() to handle polymorphism.
     * @param hostId The ID of the host user.
     * @return LiveData holding a list of matches.
     */
    public LiveData<List<Match>> getMatchesByHostId(String hostId) {
        MutableLiveData<List<Match>> liveData = new MutableLiveData<>();
        
        Query query = databaseReference.orderByChild("hostUserId").equalTo(hostId);
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Match> matches = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Match match = deserializeMatch(childSnapshot);
                    if (match != null) {
                        matches.add(match);
                    }
                }
                liveData.setValue(matches);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                liveData.setValue(null);
            }
        });
        
        return liveData;
    }

    /**
     * Gets all live (currently ongoing) matches.
     * This can be used for a "Live Matches" feed.
     * FIXED: Uses deserializeMatch() to handle polymorphism.
     * @return LiveData holding a list of live matches.
     */
    public LiveData<List<Match>> getLiveMatches() {
        MutableLiveData<List<Match>> liveData = new MutableLiveData<>();
        
        Query query = databaseReference.orderByChild("matchStatus").equalTo("LIVE");
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Match> matches = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Match match = deserializeMatch(childSnapshot);
                    if (match != null) {
                        matches.add(match);
                    }
                }
                liveData.setValue(matches);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                liveData.setValue(null);
            }
        });
        
        return liveData;
    }

    /**
     * Gets a match by its visibility link.
     * This is the primary way viewers access online matches.
     * FIXED: Uses deserializeMatch() to handle polymorphism.
     * @param visibilityLink The unique link for the match.
     * @return LiveData holding the match.
     */
    public LiveData<Match> getMatchByVisibilityLink(String visibilityLink) {
        MutableLiveData<Match> liveData = new MutableLiveData<>();
        
        Query query = databaseReference.orderByChild("visibilityLink").equalTo(visibilityLink);
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Match match = deserializeMatch(childSnapshot);
                    liveData.setValue(match);
                    return; // Only one match should have this link
                }
                liveData.setValue(null); // No match found
            }

            @Override
            public void onCancelled(DatabaseError error) {
                liveData.setValue(null);
            }
        });
        
        return liveData;
    }

    /**
     * Updates only the match status field.
     * More efficient than updating the entire match object.
     * @param matchId The match ID.
     * @param newStatus The new status (e.g., "LIVE", "COMPLETED").
     * @return Task that completes when operation finishes.
     */
    public Task<Void> updateMatchStatus(String matchId, String newStatus) {
        return updateField(matchId, "matchStatus", newStatus);
    }

    /**
     * Updates only the match score fields.
     * Useful for frequent score updates without syncing the entire match.
     * @param matchId The match ID.
     * @param homeScore The home team score.
     * @param awayScore The away team score.
     * @return Task that completes when operation finishes.
     */
    public Task<Void> updateScores(String matchId, int homeScore, int awayScore) {
        return databaseReference.child(matchId).updateChildren(
            new java.util.HashMap<String, Object>() {{
                put("homeScore", homeScore);
                put("awayScore", awayScore);
            }}
        );
    }
}
