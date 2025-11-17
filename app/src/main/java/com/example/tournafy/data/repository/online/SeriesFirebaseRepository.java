package com.example.tournafy.data.repository.online;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.example.tournafy.domain.models.series.Series;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Firebase Realtime Database repository for ONLINE Series entities.
 * This is used when a series is hosted online (isOnline=true) and needs to be
 * accessible via a link for real-time viewing of scores and schedules.
 *
 * Differences from SeriesFirestoreRepository (offline):
 * - Uses Firebase Realtime Database for cloud storage
 * - No offline persistence
 * - Optimized for real-time updates
 * - Accessible via visibility links
 */
@Singleton
public class SeriesFirebaseRepository extends FirebaseRepository<Series> {

    public static final String DATABASE_PATH = "series";

    @Inject
    public SeriesFirebaseRepository(FirebaseDatabase firebaseDatabase) {
        super(firebaseDatabase, DATABASE_PATH, Series.class);
    }

    @Override
    protected String getEntityId(Series entity) {
        return entity.getEntityId();
    }

    /**
     * Overrides the generic 'add' method to ensure a Series ID is set.
     */
    @Override
    public Task<Void> add(Series entity) {
        // If the entity doesn't have an ID, create one.
        if (entity.getEntityId() == null || entity.getEntityId().isEmpty()) {
            String newId = databaseReference.push().getKey();
            entity.setEntityId(newId);
        }
        return addOrUpdateWithId(entity.getEntityId(), entity);
    }

    /**
     * Gets all online series hosted by a specific user.
     * @param hostId The ID of the host user.
     * @return LiveData holding a list of series.
     */
    public LiveData<List<Series>> getSeriesByHostId(String hostId) {
        MutableLiveData<List<Series>> liveData = new MutableLiveData<>();
        
        Query query = databaseReference.orderByChild("hostUserId").equalTo(hostId);
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Series> seriesList = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Series series = childSnapshot.getValue(Series.class);
                    if (series != null) {
                        seriesList.add(series);
                    }
                }
                liveData.setValue(seriesList);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                liveData.setValue(null);
            }
        });
        
        return liveData;
    }

    /**
     * Gets all active (ongoing) series.
     * This can be used for a "Live Series" feed.
     * @return LiveData holding a list of active series.
     */
    public LiveData<List<Series>> getActiveSeries() {
        MutableLiveData<List<Series>> liveData = new MutableLiveData<>();
        
        Query query = databaseReference.orderByChild("status").equalTo("ACTIVE");
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Series> seriesList = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Series series = childSnapshot.getValue(Series.class);
                    if (series != null) {
                        seriesList.add(series);
                    }
                }
                liveData.setValue(seriesList);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                liveData.setValue(null);
            }
        });
        
        return liveData;
    }

    /**
     * Gets a series by its visibility link.
     * This is the primary way viewers access online series.
     * @param visibilityLink The unique link for the series.
     * @return LiveData holding the series.
     */
    public LiveData<Series> getSeriesByVisibilityLink(String visibilityLink) {
        MutableLiveData<Series> liveData = new MutableLiveData<>();
        
        Query query = databaseReference.orderByChild("visibilityLink").equalTo(visibilityLink);
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Series series = childSnapshot.getValue(Series.class);
                    liveData.setValue(series);
                    return; // Only one series should have this link
                }
                liveData.setValue(null); // No series found
            }

            @Override
            public void onCancelled(DatabaseError error) {
                liveData.setValue(null);
            }
        });
        
        return liveData;
    }

    /**
     * Updates only the series status field.
     * More efficient than updating the entire series object.
     * @param seriesId The series ID.
     * @param newStatus The new status (e.g., "ACTIVE", "COMPLETED").
     * @return Task that completes when operation finishes.
     */
    public Task<Void> updateSeriesStatus(String seriesId, String newStatus) {
        return updateField(seriesId, "status", newStatus);
    }

    /**
     * Updates the aggregate scores for a series.
     * This would typically be called by the SeriesScoreObserver after each match.
     * @param seriesId The series ID.
     * @param teamId The team ID to update.
     * @param newScore The new aggregate score (e.g., "2-1" for 2 wins, 1 loss).
     * @return Task that completes when operation finishes.
     */
    public Task<Void> updateTeamScore(String seriesId, String teamId, String newScore) {
        // This assumes the Series model has a field like "team1Score", "team2Score"
        // or a Map<String, String> of teamId -> score.
        // Adjust the field path based on your actual Series model.
        return updateField(seriesId, "scores/" + teamId, newScore);
    }
}
