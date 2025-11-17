package com.example.tournafy.data.repository.online;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.example.tournafy.domain.models.match.football.FootballEvent;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Firebase Realtime Database repository for ONLINE FootballEvent entities.
 * This handles cloud storage of individual football match events with real-time updates.
 * 
 * CRITICAL DESIGN: Each event (goal, card, substitution, etc.) is stored as a separate 
 * document for scalability. This allows viewers to receive real-time updates for each 
 * event without re-downloading the entire match object, similar to SofaScore's live updates.
 */
@Singleton
public class FootballEventFirebaseRepository extends FirebaseRepository<FootballEvent> {

    public static final String DATABASE_PATH = "football_events";

    @Inject
    public FootballEventFirebaseRepository(FirebaseDatabase firebaseDatabase) {
        super(firebaseDatabase, DATABASE_PATH, FootballEvent.class);
    }

    @Override
    protected String getEntityId(FootballEvent entity) {
        return entity.getEventId();
    }

    @Override
    public Task<Void> add(FootballEvent entity) {
        if (entity.getEventId() == null || entity.getEventId().isEmpty()) {
            String newId = databaseReference.push().getKey();
            entity.setEventId(newId);
        }
        return addOrUpdateWithId(entity.getEventId(), entity);
    }

    /**
     * Gets all events for a specific match with real-time updates.
     * @param matchId The match ID.
     * @return LiveData holding a list of events ordered by match minute.
     */
    public LiveData<List<FootballEvent>> getEventsByMatchId(String matchId) {
        MutableLiveData<List<FootballEvent>> liveData = new MutableLiveData<>();
        
        Query query = databaseReference.orderByChild("matchId").equalTo(matchId);
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<FootballEvent> events = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    FootballEvent event = childSnapshot.getValue(FootballEvent.class);
                    if (event != null) {
                        events.add(event);
                    }
                }
                // Sort by match minute
                events.sort((e1, e2) -> Integer.compare(e1.getMatchMinute(), e2.getMatchMinute()));
                liveData.setValue(events);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                liveData.setValue(null);
            }
        });
        
        return liveData;
    }

    /**
     * Gets all events of a specific category for a match with real-time updates.
     * @param matchId The match ID.
     * @param eventCategory The event category (e.g., "GOAL", "CARD", "SUBSTITUTION").
     * @return LiveData holding a list of events.
     */
    public LiveData<List<FootballEvent>> getEventsByCategory(String matchId, String eventCategory) {
        MutableLiveData<List<FootballEvent>> liveData = new MutableLiveData<>();
        
        Query query = databaseReference.orderByChild("matchId").equalTo(matchId);
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<FootballEvent> events = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    FootballEvent event = childSnapshot.getValue(FootballEvent.class);
                    if (event != null && eventCategory.equals(event.getEventCategory())) {
                        events.add(event);
                    }
                }
                events.sort((e1, e2) -> Integer.compare(e1.getMatchMinute(), e2.getMatchMinute()));
                liveData.setValue(events);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                liveData.setValue(null);
            }
        });
        
        return liveData;
    }

    /**
     * Gets all goal events for a match with real-time updates.
     * @param matchId The match ID.
     * @return LiveData holding a list of goal events.
     */
    public LiveData<List<FootballEvent>> getGoalEvents(String matchId) {
        return getEventsByCategory(matchId, "GOAL");
    }

    /**
     * Gets all card events for a match with real-time updates.
     * @param matchId The match ID.
     * @return LiveData holding a list of card events.
     */
    public LiveData<List<FootballEvent>> getCardEvents(String matchId) {
        return getEventsByCategory(matchId, "CARD");
    }

    /**
     * Gets all substitution events for a match with real-time updates.
     * @param matchId The match ID.
     * @return LiveData holding a list of substitution events.
     */
    public LiveData<List<FootballEvent>> getSubstitutionEvents(String matchId) {
        return getEventsByCategory(matchId, "SUBSTITUTION");
    }

    /**
     * Gets all events for a specific team in a match with real-time updates.
     * @param matchId The match ID.
     * @param teamId The team ID.
     * @return LiveData holding a list of events.
     */
    public LiveData<List<FootballEvent>> getEventsByTeam(String matchId, String teamId) {
        MutableLiveData<List<FootballEvent>> liveData = new MutableLiveData<>();
        
        Query query = databaseReference.orderByChild("matchId").equalTo(matchId);
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<FootballEvent> events = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    FootballEvent event = childSnapshot.getValue(FootballEvent.class);
                    if (event != null && teamId.equals(event.getTeamId())) {
                        events.add(event);
                    }
                }
                events.sort((e1, e2) -> Integer.compare(e1.getMatchMinute(), e2.getMatchMinute()));
                liveData.setValue(events);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                liveData.setValue(null);
            }
        });
        
        return liveData;
    }

    /**
     * Gets all events involving a specific player with real-time updates.
     * @param matchId The match ID.
     * @param playerId The player ID.
     * @return LiveData holding a list of events.
     */
    public LiveData<List<FootballEvent>> getEventsByPlayer(String matchId, String playerId) {
        MutableLiveData<List<FootballEvent>> liveData = new MutableLiveData<>();
        
        Query query = databaseReference.orderByChild("matchId").equalTo(matchId);
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<FootballEvent> events = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    FootballEvent event = childSnapshot.getValue(FootballEvent.class);
                    if (event != null && playerId.equals(event.getPlayerId())) {
                        events.add(event);
                    }
                }
                events.sort((e1, e2) -> Integer.compare(e1.getMatchMinute(), e2.getMatchMinute()));
                liveData.setValue(events);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                liveData.setValue(null);
            }
        });
        
        return liveData;
    }

    /**
     * Gets all events for a specific match period with real-time updates.
     * @param matchId The match ID.
     * @param matchPeriod The match period (e.g., "FIRST_HALF", "SECOND_HALF").
     * @return LiveData holding a list of events.
     */
    public LiveData<List<FootballEvent>> getEventsByPeriod(String matchId, String matchPeriod) {
        MutableLiveData<List<FootballEvent>> liveData = new MutableLiveData<>();
        
        Query query = databaseReference.orderByChild("matchId").equalTo(matchId);
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<FootballEvent> events = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    FootballEvent event = childSnapshot.getValue(FootballEvent.class);
                    if (event != null && matchPeriod.equals(event.getMatchPeriod())) {
                        events.add(event);
                    }
                }
                events.sort((e1, e2) -> Integer.compare(e1.getMatchMinute(), e2.getMatchMinute()));
                liveData.setValue(events);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                liveData.setValue(null);
            }
        });
        
        return liveData;
    }
}
