package com.example.tournafy.data.repository.online;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tournafy.domain.models.match.cricket.CricketMatchConfig;
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
import com.example.tournafy.domain.models.match.cricket.CricketEvent;
import com.example.tournafy.domain.models.match.cricket.CricketMatchConfig;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Firebase Realtime Database repository for ONLINE Match entities.
 */
@Singleton
public class MatchFirebaseRepository extends FirebaseRepository<Match> {

    private static final String TAG = "MatchFirebaseRepository";
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
     * CRITICAL FIX: Now properly deserializes cricket-specific nested fields (innings, events, teams, currentOvers).
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
            // Create match manually to avoid abstract MatchConfig deserialization issue
            Log.d(TAG, "Using manual deserialization for CricketMatch to handle abstract MatchConfig");
            CricketMatch match = new CricketMatch();
            
            // Deserialize basic Match fields
            match.setEntityId(snapshot.child("entityId").getValue(String.class));
            match.setName(snapshot.child("name").getValue(String.class));
            match.setSportId(snapshot.child("sportId").getValue(String.class));
            match.setMatchStatus(snapshot.child("matchStatus").getValue(String.class));
            match.setVenue(snapshot.child("venue").getValue(String.class));
            match.setHostUserId(snapshot.child("hostUserId").getValue(String.class));
            match.setStatus(snapshot.child("status").getValue(String.class));
            
            // Deserialize CricketMatchConfig
            DataSnapshot configSnapshot = snapshot.child("matchConfig");
            if (configSnapshot.exists()) {
                CricketMatchConfig config = new CricketMatchConfig();
                if (configSnapshot.child("numberOfOvers").exists()) {
                    config.setNumberOfOvers(configSnapshot.child("numberOfOvers").getValue(Integer.class));
                }
                if (configSnapshot.child("wideOn").exists()) {
                    config.setWideOn(configSnapshot.child("wideOn").getValue(Boolean.class));
                }
                match.setMatchConfig(config);
            }
            
            // CRITICAL FIX: Manually deserialize nested cricket-specific fields
            if (match != null) {
                // Deserialize currentInningsNumber using reflection (private field)
                try {
                    Integer currentInningsNumber = snapshot.child("currentInningsNumber").getValue(Integer.class);
                    if (currentInningsNumber != null) {
                        java.lang.reflect.Field field = CricketMatch.class.getDeclaredField("currentInningsNumber");
                        field.setAccessible(true);
                        field.set(match, currentInningsNumber);
                        android.util.Log.d(TAG, "Set currentInningsNumber to " + currentInningsNumber + " for match " + match.getEntityId());
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    android.util.Log.e(TAG, "Failed to set currentInningsNumber via reflection", e);
                }
                
                // Deserialize targetScore using reflection (private field)
                try {
                    Integer targetScore = snapshot.child("targetScore").getValue(Integer.class);
                    if (targetScore != null) {
                        java.lang.reflect.Field field = CricketMatch.class.getDeclaredField("targetScore");
                        field.setAccessible(true);
                        field.set(match, targetScore);
                        android.util.Log.d(TAG, "Set targetScore to " + targetScore + " for match " + match.getEntityId());
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    android.util.Log.e(TAG, "Failed to set targetScore via reflection", e);
                }
                
                // Deserialize innings list
                com.google.firebase.database.GenericTypeIndicator<List<com.example.tournafy.domain.models.match.cricket.Innings>> inningsType = 
                    new com.google.firebase.database.GenericTypeIndicator<List<com.example.tournafy.domain.models.match.cricket.Innings>>() {};
                List<com.example.tournafy.domain.models.match.cricket.Innings> innings = snapshot.child("innings").getValue(inningsType);
                if (innings != null) {
                    match.setInnings(innings);
                    android.util.Log.d(TAG, "Deserialized " + innings.size() + " innings for match " + match.getEntityId());
                }
                
                // Deserialize cricket events list
                com.google.firebase.database.GenericTypeIndicator<List<CricketEvent>> eventsType = 
                    new com.google.firebase.database.GenericTypeIndicator<List<CricketEvent>>() {};
                List<CricketEvent> cricketEvents = snapshot.child("cricketEvents").getValue(eventsType);
                if (cricketEvents != null) {
                    match.setCricketEvents(cricketEvents);
                }
                
                // Deserialize teams list
                com.google.firebase.database.GenericTypeIndicator<List<com.example.tournafy.domain.models.team.MatchTeam>> teamsType = 
                    new com.google.firebase.database.GenericTypeIndicator<List<com.example.tournafy.domain.models.team.MatchTeam>>() {};
                List<com.example.tournafy.domain.models.team.MatchTeam> teams = snapshot.child("teams").getValue(teamsType);
                if (teams != null) {
                    match.setTeams(teams);
                }
            }
            
            return match;
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