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
     */
    private Match deserializeMatch(DataSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) {
            return null;
        }
        
        // CRITICAL FIX: Read 'sportId' instead of 'sportType' to match Domain Model
        String sportId = snapshot.child("sportId").getValue(String.class);
        
        if (sportId == null) {
             // Fallback for legacy data
             sportId = snapshot.child("sportType").getValue(String.class);
        }
        
        if (sportId == null) {
            return null;
        }
        
        // Deserialize to the correct concrete class
        if (SportTypeEnum.CRICKET.name().equals(sportId)) {
            return snapshot.getValue(CricketMatch.class);
        } else if (SportTypeEnum.FOOTBALL.name().equals(sportId)) {
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

    @Override
    public Task<Void> add(Match entity) {
        if (entity.getEntityId() == null || entity.getEntityId().isEmpty()) {
            String newId = databaseReference.push().getKey();
            entity.setEntityId(newId);
        }
        return addOrUpdateWithId(entity.getEntityId(), entity);
    }

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

    public LiveData<Match> getMatchByVisibilityLink(String visibilityLink) {
        MutableLiveData<Match> liveData = new MutableLiveData<>();
        
        Query query = databaseReference.orderByChild("visibilityLink").equalTo(visibilityLink);
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Match match = deserializeMatch(childSnapshot);
                    liveData.setValue(match);
                    return; 
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

    public Task<Void> updateMatchStatus(String matchId, String newStatus) {
        return updateField(matchId, "matchStatus", newStatus);
    }

    public Task<Void> updateScores(String matchId, int homeScore, int awayScore) {
        return databaseReference.child(matchId).updateChildren(
            new java.util.HashMap<String, Object>() {{
                put("homeScore", homeScore);
                put("awayScore", awayScore);
            }}
        );
    }
}