package com.example.tournafy.data.repository.offline;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.example.tournafy.domain.models.match.football.FootballEvent;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Firestore repository for OFFLINE FootballEvent entities.
 * This handles local storage of individual football match events with offline persistence.
 * Each event (goal, card, substitution, etc.) is stored as a separate document 
 * for scalability and fast real-time updates.
 */
@Singleton
public class FootballEventFirestoreRepository extends FirestoreRepository<FootballEvent> {

    public static final String COLLECTION_NAME = "football_events";

    @Inject
    public FootballEventFirestoreRepository(FirebaseFirestore firestore) {
        super(firestore, COLLECTION_NAME, FootballEvent.class);
    }

    @Override
    protected String getEntityId(FootballEvent entity) {
        return entity.getEventId();
    }

    /**
     * Gets all events for a specific match.
     * @param matchId The match ID.
     * @return LiveData holding a list of events ordered by match minute.
     */
    public LiveData<List<FootballEvent>> getEventsByMatchId(String matchId) {
        MutableLiveData<List<FootballEvent>> liveData = new MutableLiveData<>();
        
        collectionReference.whereEqualTo("matchId", matchId)
                .orderBy("matchMinute", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        liveData.setValue(null);
                        return;
                    }
                    if (snapshot != null) {
                        List<FootballEvent> events = snapshot.toObjects(FootballEvent.class);
                        liveData.setValue(events);
                    }
                });
        
        return liveData;
    }

    /**
     * Gets all events of a specific category for a match.
     * @param matchId The match ID.
     * @param eventCategory The event category (e.g., "GOAL", "CARD", "SUBSTITUTION").
     * @return LiveData holding a list of events.
     */
    public LiveData<List<FootballEvent>> getEventsByCategory(String matchId, String eventCategory) {
        MutableLiveData<List<FootballEvent>> liveData = new MutableLiveData<>();
        
        collectionReference.whereEqualTo("matchId", matchId)
                .whereEqualTo("eventCategory", eventCategory)
                .orderBy("matchMinute", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        liveData.setValue(null);
                        return;
                    }
                    if (snapshot != null) {
                        List<FootballEvent> events = snapshot.toObjects(FootballEvent.class);
                        liveData.setValue(events);
                    }
                });
        
        return liveData;
    }

    /**
     * Gets all goal events for a match.
     * @param matchId The match ID.
     * @return LiveData holding a list of goal events.
     */
    public LiveData<List<FootballEvent>> getGoalEvents(String matchId) {
        return getEventsByCategory(matchId, "GOAL");
    }

    /**
     * Gets all card events for a match.
     * @param matchId The match ID.
     * @return LiveData holding a list of card events.
     */
    public LiveData<List<FootballEvent>> getCardEvents(String matchId) {
        return getEventsByCategory(matchId, "CARD");
    }

    /**
     * Gets all substitution events for a match.
     * @param matchId The match ID.
     * @return LiveData holding a list of substitution events.
     */
    public LiveData<List<FootballEvent>> getSubstitutionEvents(String matchId) {
        return getEventsByCategory(matchId, "SUBSTITUTION");
    }

    /**
     * Gets all events for a specific team in a match.
     * @param matchId The match ID.
     * @param teamId The team ID.
     * @return LiveData holding a list of events.
     */
    public LiveData<List<FootballEvent>> getEventsByTeam(String matchId, String teamId) {
        MutableLiveData<List<FootballEvent>> liveData = new MutableLiveData<>();
        
        collectionReference.whereEqualTo("matchId", matchId)
                .whereEqualTo("teamId", teamId)
                .orderBy("matchMinute", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        liveData.setValue(null);
                        return;
                    }
                    if (snapshot != null) {
                        List<FootballEvent> events = snapshot.toObjects(FootballEvent.class);
                        liveData.setValue(events);
                    }
                });
        
        return liveData;
    }

    /**
     * Gets all events involving a specific player.
     * @param matchId The match ID.
     * @param playerId The player ID.
     * @return LiveData holding a list of events.
     */
    public LiveData<List<FootballEvent>> getEventsByPlayer(String matchId, String playerId) {
        MutableLiveData<List<FootballEvent>> liveData = new MutableLiveData<>();
        
        collectionReference.whereEqualTo("matchId", matchId)
                .whereEqualTo("playerId", playerId)
                .orderBy("matchMinute", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        liveData.setValue(null);
                        return;
                    }
                    if (snapshot != null) {
                        List<FootballEvent> events = snapshot.toObjects(FootballEvent.class);
                        liveData.setValue(events);
                    }
                });
        
        return liveData;
    }

    /**
     * Gets all events for a specific match period.
     * @param matchId The match ID.
     * @param matchPeriod The match period (e.g., "FIRST_HALF", "SECOND_HALF").
     * @return LiveData holding a list of events.
     */
    public LiveData<List<FootballEvent>> getEventsByPeriod(String matchId, String matchPeriod) {
        MutableLiveData<List<FootballEvent>> liveData = new MutableLiveData<>();
        
        collectionReference.whereEqualTo("matchId", matchId)
                .whereEqualTo("matchPeriod", matchPeriod)
                .orderBy("matchMinute", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        liveData.setValue(null);
                        return;
                    }
                    if (snapshot != null) {
                        List<FootballEvent> events = snapshot.toObjects(FootballEvent.class);
                        liveData.setValue(events);
                    }
                });
        
        return liveData;
    }
}
