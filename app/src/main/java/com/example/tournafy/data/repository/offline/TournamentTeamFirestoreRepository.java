package com.example.tournafy.data.repository.offline;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.example.tournafy.domain.models.team.TournamentTeam;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Repository for TournamentTeam entities.
 * TournamentTeams are stored as a subcollection under tournaments/{tournamentId}/teams
 */
@Singleton
public class TournamentTeamFirestoreRepository {

    private static final String TOURNAMENTS_COLLECTION = "tournaments";
    private static final String TEAMS_SUBCOLLECTION = "teams";
    
    private final FirebaseFirestore firestore;

    @Inject
    public TournamentTeamFirestoreRepository(FirebaseFirestore firestoreInstance) {
        this.firestore = firestoreInstance;
    }

    /**
     * Add a team to a tournament
     */
    public Task<Void> add(String tournamentId, TournamentTeam tournamentTeam) {
        if (tournamentTeam.getTournamentTeamId() == null || tournamentTeam.getTournamentTeamId().isEmpty()) {
            String newId = getTeamsCollection(tournamentId).document().getId();
            tournamentTeam.setTournamentTeamId(newId);
        }
        tournamentTeam.setTournamentId(tournamentId);
        
        return getTeamsCollection(tournamentId)
                .document(tournamentTeam.getTournamentTeamId())
                .set(tournamentTeam);
    }

    /**
     * Update tournament team statistics
     */
    public Task<Void> update(String tournamentId, TournamentTeam tournamentTeam) {
        if (tournamentTeam.getTournamentTeamId() == null) {
            return com.google.android.gms.tasks.Tasks.forException(
                new IllegalArgumentException("TournamentTeam ID cannot be null for update.")
            );
        }
        
        return getTeamsCollection(tournamentId)
                .document(tournamentTeam.getTournamentTeamId())
                .set(tournamentTeam);
    }

    /**
     * Delete a team from tournament
     */
    public Task<Void> delete(String tournamentId, String tournamentTeamId) {
        return getTeamsCollection(tournamentId)
                .document(tournamentTeamId)
                .delete();
    }

    /**
     * Get a specific tournament team by ID
     */
    public LiveData<TournamentTeam> getById(String tournamentId, String tournamentTeamId) {
        MutableLiveData<TournamentTeam> liveData = new MutableLiveData<>();
        
        getTeamsCollection(tournamentId)
                .document(tournamentTeamId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        liveData.setValue(null);
                        return;
                    }
                    if (snapshot != null && snapshot.exists()) {
                        liveData.setValue(snapshot.toObject(TournamentTeam.class));
                    } else {
                        liveData.setValue(null);
                    }
                });
        
        return liveData;
    }

    /**
     * Get all teams for a tournament
     */
    public LiveData<List<TournamentTeam>> getAllForTournament(String tournamentId) {
        MutableLiveData<List<TournamentTeam>> liveData = new MutableLiveData<>();
        
        getTeamsCollection(tournamentId)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        liveData.setValue(null);
                        return;
                    }
                    if (snapshots != null) {
                        liveData.setValue(snapshots.toObjects(TournamentTeam.class));
                    }
                });
        
        return liveData;
    }

    /**
     * Get teams sorted by points (for standings table)
     * Primary sort: Points (descending)
     * Secondary sort: Net Run Rate / Goal Difference (descending)
     */
    public LiveData<List<TournamentTeam>> getStandingsSorted(String tournamentId) {
        MutableLiveData<List<TournamentTeam>> liveData = new MutableLiveData<>();
        
        getTeamsCollection(tournamentId)
                .orderBy("points", Query.Direction.DESCENDING)
                .orderBy("netRunRate", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        liveData.setValue(null);
                        return;
                    }
                    if (snapshots != null) {
                        liveData.setValue(snapshots.toObjects(TournamentTeam.class));
                    }
                });
        
        return liveData;
    }

    /**
     * Get tournament team by team ID (useful for lookup)
     */
    public LiveData<TournamentTeam> getByTeamId(String tournamentId, String teamId) {
        MutableLiveData<TournamentTeam> liveData = new MutableLiveData<>();
        
        getTeamsCollection(tournamentId)
                .whereEqualTo("teamId", teamId)
                .limit(1)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        liveData.setValue(null);
                        return;
                    }
                    if (snapshots != null && !snapshots.isEmpty()) {
                        liveData.setValue(snapshots.toObjects(TournamentTeam.class).get(0));
                    } else {
                        liveData.setValue(null);
                    }
                });
        
        return liveData;
    }

    /**
     * Get top N teams by points (for qualification scenarios)
     */
    public LiveData<List<TournamentTeam>> getTopTeams(String tournamentId, int limit) {
        MutableLiveData<List<TournamentTeam>> liveData = new MutableLiveData<>();
        
        getTeamsCollection(tournamentId)
                .orderBy("points", Query.Direction.DESCENDING)
                .orderBy("netRunRate", Query.Direction.DESCENDING)
                .limit(limit)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        liveData.setValue(null);
                        return;
                    }
                    if (snapshots != null) {
                        liveData.setValue(snapshots.toObjects(TournamentTeam.class));
                    }
                });
        
        return liveData;
    }

    /**
     * Helper method to get the teams subcollection reference
     */
    private com.google.firebase.firestore.CollectionReference getTeamsCollection(String tournamentId) {
        return firestore.collection(TOURNAMENTS_COLLECTION)
                .document(tournamentId)
                .collection(TEAMS_SUBCOLLECTION);
    }

    /**
     * Batch add multiple teams to a tournament
     */
    public Task<Void> addTeams(String tournamentId, List<TournamentTeam> teams) {
        com.google.firebase.firestore.WriteBatch batch = firestore.batch();
        
        for (TournamentTeam team : teams) {
            if (team.getTournamentTeamId() == null || team.getTournamentTeamId().isEmpty()) {
                String newId = getTeamsCollection(tournamentId).document().getId();
                team.setTournamentTeamId(newId);
            }
            team.setTournamentId(tournamentId);
            
            com.google.firebase.firestore.DocumentReference docRef = 
                getTeamsCollection(tournamentId).document(team.getTournamentTeamId());
            batch.set(docRef, team);
        }
        
        return batch.commit();
    }

    /**
     * Delete all teams for a tournament (cascading delete)
     */
    public Task<Void> deleteAllForTournament(String tournamentId) {
        return getTeamsCollection(tournamentId)
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
