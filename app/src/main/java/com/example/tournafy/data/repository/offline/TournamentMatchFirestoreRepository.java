package com.example.tournafy.data.repository.offline;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.example.tournafy.domain.models.tournament.TournamentMatch;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Repository for TournamentMatch entities.
 * Links Match entities to Tournament Stages with ordering.
 * Stored as subcollection under tournaments/{tournamentId}/tournament_matches
 */
@Singleton
public class TournamentMatchFirestoreRepository {

    private static final String TOURNAMENTS_COLLECTION = "tournaments";
    private static final String TOURNAMENT_MATCHES_SUBCOLLECTION = "tournament_matches";
    
    private final FirebaseFirestore firestore;

    @Inject
    public TournamentMatchFirestoreRepository(FirebaseFirestore firestoreInstance) {
        this.firestore = firestoreInstance;
    }

    /**
     * Add a tournament match link
     */
    public Task<Void> add(String tournamentId, TournamentMatch tournamentMatch) {
        if (tournamentMatch.getTournamentMatchId() == null || tournamentMatch.getTournamentMatchId().isEmpty()) {
            String newId = getTournamentMatchesCollection(tournamentId).document().getId();
            tournamentMatch.setTournamentMatchId(newId);
        }
        tournamentMatch.setTournamentId(tournamentId);
        
        return getTournamentMatchesCollection(tournamentId)
                .document(tournamentMatch.getTournamentMatchId())
                .set(tournamentMatch);
    }

    /**
     * Update tournament match
     */
    public Task<Void> update(String tournamentId, TournamentMatch tournamentMatch) {
        if (tournamentMatch.getTournamentMatchId() == null) {
            return com.google.android.gms.tasks.Tasks.forException(
                new IllegalArgumentException("TournamentMatch ID cannot be null for update.")
            );
        }
        
        return getTournamentMatchesCollection(tournamentId)
                .document(tournamentMatch.getTournamentMatchId())
                .set(tournamentMatch);
    }

    /**
     * Delete a tournament match link
     */
    public Task<Void> delete(String tournamentId, String tournamentMatchId) {
        return getTournamentMatchesCollection(tournamentId)
                .document(tournamentMatchId)
                .delete();
    }

    /**
     * Get a specific tournament match by ID
     */
    public LiveData<TournamentMatch> getById(String tournamentId, String tournamentMatchId) {
        MutableLiveData<TournamentMatch> liveData = new MutableLiveData<>();
        
        getTournamentMatchesCollection(tournamentId)
                .document(tournamentMatchId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        liveData.setValue(null);
                        return;
                    }
                    if (snapshot != null && snapshot.exists()) {
                        liveData.setValue(snapshot.toObject(TournamentMatch.class));
                    } else {
                        liveData.setValue(null);
                    }
                });
        
        return liveData;
    }

    /**
     * Get all matches for a tournament
     */
    public LiveData<List<TournamentMatch>> getAllForTournament(String tournamentId) {
        MutableLiveData<List<TournamentMatch>> liveData = new MutableLiveData<>();
        
        getTournamentMatchesCollection(tournamentId)
                .orderBy("matchOrder", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        liveData.setValue(null);
                        return;
                    }
                    if (snapshots != null) {
                        liveData.setValue(snapshots.toObjects(TournamentMatch.class));
                    }
                });
        
        return liveData;
    }

    /**
     * Get all matches for a specific stage
     */
    public LiveData<List<TournamentMatch>> getMatchesByStage(String tournamentId, String stageId) {
        MutableLiveData<List<TournamentMatch>> liveData = new MutableLiveData<>();
        
        getTournamentMatchesCollection(tournamentId)
                .whereEqualTo("stageId", stageId)
                .orderBy("matchOrder", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        liveData.setValue(null);
                        return;
                    }
                    if (snapshots != null) {
                        liveData.setValue(snapshots.toObjects(TournamentMatch.class));
                    }
                });
        
        return liveData;
    }

    /**
     * Get tournament match by actual match ID (for reverse lookup)
     */
    public LiveData<TournamentMatch> getByMatchId(String tournamentId, String matchId) {
        MutableLiveData<TournamentMatch> liveData = new MutableLiveData<>();
        
        getTournamentMatchesCollection(tournamentId)
                .whereEqualTo("matchId", matchId)
                .limit(1)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        liveData.setValue(null);
                        return;
                    }
                    if (snapshots != null && !snapshots.isEmpty()) {
                        liveData.setValue(snapshots.toObjects(TournamentMatch.class).get(0));
                    } else {
                        liveData.setValue(null);
                    }
                });
        
        return liveData;
    }

    /**
     * Count matches in a stage
     */
    public Task<Integer> countMatchesInStage(String tournamentId, String stageId) {
        return getTournamentMatchesCollection(tournamentId)
                .whereEqualTo("stageId", stageId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        return task.getResult().size();
                    }
                    return 0;
                });
    }

    /**
     * Helper method to get the tournament_matches subcollection reference
     */
    private com.google.firebase.firestore.CollectionReference getTournamentMatchesCollection(String tournamentId) {
        return firestore.collection(TOURNAMENTS_COLLECTION)
                .document(tournamentId)
                .collection(TOURNAMENT_MATCHES_SUBCOLLECTION);
    }

    /**
     * Batch add multiple tournament matches
     */
    public Task<Void> addMatches(String tournamentId, List<TournamentMatch> tournamentMatches) {
        com.google.firebase.firestore.WriteBatch batch = firestore.batch();
        
        for (TournamentMatch tm : tournamentMatches) {
            if (tm.getTournamentMatchId() == null || tm.getTournamentMatchId().isEmpty()) {
                String newId = getTournamentMatchesCollection(tournamentId).document().getId();
                tm.setTournamentMatchId(newId);
            }
            tm.setTournamentId(tournamentId);
            
            com.google.firebase.firestore.DocumentReference docRef = 
                getTournamentMatchesCollection(tournamentId).document(tm.getTournamentMatchId());
            batch.set(docRef, tm);
        }
        
        return batch.commit();
    }

    /**
     * Delete all tournament matches for a stage
     */
    public Task<Void> deleteAllForStage(String tournamentId, String stageId) {
        return getTournamentMatchesCollection(tournamentId)
                .whereEqualTo("stageId", stageId)
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        return com.google.android.gms.tasks.Tasks.forException(task.getException());
                    }
                    
                    com.google.firebase.firestore.WriteBatch batch = firestore.batch();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : task.getResult()) {
                        batch.delete(doc.getReference());
                    }
                    return batch.commit();
                });
    }

    /**
     * Delete all tournament matches for a tournament (cascading delete)
     */
    public Task<Void> deleteAllForTournament(String tournamentId) {
        return getTournamentMatchesCollection(tournamentId)
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        return com.google.android.gms.tasks.Tasks.forException(task.getException());
                    }
                    
                    com.google.firebase.firestore.WriteBatch batch = firestore.batch();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : task.getResult()) {
                        batch.delete(doc.getReference());
                    }
                    return batch.commit();
                });
    }
}
