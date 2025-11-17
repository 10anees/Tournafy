package com.example.tournafy.data.repository.online;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.example.tournafy.domain.models.match.cricket.Innings;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class InningsFirebaseRepository extends FirebaseRepository<Innings> {

    public static final String DATABASE_PATH = "innings";

    @Inject
    public InningsFirebaseRepository(FirebaseDatabase firebaseDatabase) {
        super(firebaseDatabase, DATABASE_PATH, Innings.class);
    }

    @Override
    protected String getEntityId(Innings entity) {
        return entity.getInningsId();
    }

    @Override
    public Task<Void> add(Innings entity) {
        if (entity.getInningsId() == null || entity.getInningsId().isEmpty()) {
            String newId = databaseReference.push().getKey();
            entity.setInningsId(newId);
        }
        return addOrUpdateWithId(entity.getInningsId(), entity);
    }

    public LiveData<List<Innings>> getInningsByMatchId(String matchId) {
        MutableLiveData<List<Innings>> liveData = new MutableLiveData<>();

        Query query = databaseReference.orderByChild("matchId").equalTo(matchId);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Innings> inningsList = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Innings innings = childSnapshot.getValue(Innings.class);
                    if (innings != null) {
                        inningsList.add(innings);
                    }
                }
                // Sort by innings number logic would go here if needed
                liveData.setValue(inningsList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                liveData.setValue(null);
            }
        });

        return liveData;
    }
}