package com.example.tournafy.data.repository.offline;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.example.tournafy.domain.models.tournament.TournamentStage;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Repository for TournamentStage entities.
 * Stages are stored as a subcollection under tournaments/{tournamentId}/stages
 */
@Singleton
public class TournamentStageFirestoreRepository {

    private static final String TOURNAMENTS_COLLECTION = "tournaments";
    private static final String STAGES_SUBCOLLECTION = "stages";
    
    private final FirebaseFirestore firestore;

    @Inject
    public TournamentStageFirestoreRepository(FirebaseFirestore firestoreInstance) {
        this.firestore = firestoreInstance;
    }

    /**
     * Add a stage to a tournament
     */
    public Task<Void> add(String tournamentId, TournamentStage stage) {
        if (stage.getStageId() == null || stage.getStageId().isEmpty()) {
            String newId = getStagesCollection(tournamentId).document().getId();
            stage.setStageId(newId);
        }
        stage.setTournamentId(tournamentId);
        
        return getStagesCollection(tournamentId)
                .document(stage.getStageId())
                .set(stage);
    }

    /**
     * Update a tournament stage
     */
    public Task<Void> update(String tournamentId, TournamentStage stage) {
        if (stage.getStageId() == null) {
            return com.google.android.gms.tasks.Tasks.forException(
                new IllegalArgumentException("Stage ID cannot be null for update.")
            );
        }
        
        return getStagesCollection(tournamentId)
                .document(stage.getStageId())
                .set(stage);
    }

    /**
     * Delete a stage
     */
    public Task<Void> delete(String tournamentId, String stageId) {
        return getStagesCollection(tournamentId)
                .document(stageId)
                .delete();
    }

    /**
     * Get a specific stage by ID
     */
    public LiveData<TournamentStage> getById(String tournamentId, String stageId) {
        MutableLiveData<TournamentStage> liveData = new MutableLiveData<>();
        
        getStagesCollection(tournamentId)
                .document(stageId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        liveData.setValue(null);
                        return;
                    }
                    if (snapshot != null && snapshot.exists()) {
                        liveData.setValue(snapshot.toObject(TournamentStage.class));
                    } else {
                        liveData.setValue(null);
                    }
                });
        
        return liveData;
    }

    /**
     * Get all stages for a tournament, ordered by stage order
     */
    public LiveData<List<TournamentStage>> getAllForTournament(String tournamentId) {
        MutableLiveData<List<TournamentStage>> liveData = new MutableLiveData<>();
        
        getStagesCollection(tournamentId)
                .orderBy("stageOrder", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        liveData.setValue(null);
                        return;
                    }
                    if (snapshots != null) {
                        liveData.setValue(snapshots.toObjects(TournamentStage.class));
                    }
                });
        
        return liveData;
    }

    /**
     * Get current active stage (first incomplete stage)
     */
    public LiveData<TournamentStage> getCurrentStage(String tournamentId) {
        MutableLiveData<TournamentStage> liveData = new MutableLiveData<>();
        
        getStagesCollection(tournamentId)
                .whereEqualTo("completed", false)
                .orderBy("stageOrder", Query.Direction.ASCENDING)
                .limit(1)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        liveData.setValue(null);
                        return;
                    }
                    if (snapshots != null && !snapshots.isEmpty()) {
                        liveData.setValue(snapshots.toObjects(TournamentStage.class).get(0));
                    } else {
                        liveData.setValue(null);
                    }
                });
        
        return liveData;
    }

    /**
     * Get stages by type (e.g., all GROUP stages, all KNOCKOUT stages)
     */
    public LiveData<List<TournamentStage>> getByType(String tournamentId, String stageType) {
        MutableLiveData<List<TournamentStage>> liveData = new MutableLiveData<>();
        
        getStagesCollection(tournamentId)
                .whereEqualTo("stageType", stageType)
                .orderBy("stageOrder", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        liveData.setValue(null);
                        return;
                    }
                    if (snapshots != null) {
                        liveData.setValue(snapshots.toObjects(TournamentStage.class));
                    }
                });
        
        return liveData;
    }

    /**
     * Get completed stages
     */
    public LiveData<List<TournamentStage>> getCompletedStages(String tournamentId) {
        MutableLiveData<List<TournamentStage>> liveData = new MutableLiveData<>();
        
        getStagesCollection(tournamentId)
                .whereEqualTo("completed", true)
                .orderBy("stageOrder", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        liveData.setValue(null);
                        return;
                    }
                    if (snapshots != null) {
                        liveData.setValue(snapshots.toObjects(TournamentStage.class));
                    }
                });
        
        return liveData;
    }

    /**
     * Mark stage as completed
     */
    public Task<Void> markAsCompleted(String tournamentId, String stageId) {
        return getStagesCollection(tournamentId)
                .document(stageId)
                .update("completed", true);
    }

    /**
     * Helper method to get the stages subcollection reference
     */
    private com.google.firebase.firestore.CollectionReference getStagesCollection(String tournamentId) {
        return firestore.collection(TOURNAMENTS_COLLECTION)
                .document(tournamentId)
                .collection(STAGES_SUBCOLLECTION);
    }

    /**
     * Batch add multiple stages
     */
    public Task<Void> addStages(String tournamentId, List<TournamentStage> stages) {
        com.google.firebase.firestore.WriteBatch batch = firestore.batch();
        
        for (TournamentStage stage : stages) {
            if (stage.getStageId() == null || stage.getStageId().isEmpty()) {
                String newId = getStagesCollection(tournamentId).document().getId();
                stage.setStageId(newId);
            }
            stage.setTournamentId(tournamentId);
            
            com.google.firebase.firestore.DocumentReference docRef = 
                getStagesCollection(tournamentId).document(stage.getStageId());
            batch.set(docRef, stage);
        }
        
        return batch.commit();
    }

    /**
     * Delete all stages for a tournament (cascading delete)
     */
    public Task<Void> deleteAllForTournament(String tournamentId) {
        return getStagesCollection(tournamentId)
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
