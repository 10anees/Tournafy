// Path: app/src/main/java/com/tournafy/data/repository/offline/TeamFirestoreRepository.java
package com.example.tournafy.data.repository.offline;

import com.google.firebase.firestore.FirebaseFirestore;
import com.example.tournafy.domain.models.team.Team;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TeamFirestoreRepository extends FirestoreRepository<Team> {

    public static final String COLLECTION_PATH = "teams";

    @Inject
    public TeamFirestoreRepository(FirebaseFirestore firestoreInstance) {
        super(firestoreInstance, COLLECTION_PATH, Team.class);
    }

    @Override
    protected String getEntityId(Team entity) {
        return entity.getTeamId();
    }

    @Override
    public com.google.android.gms.tasks.Task<Void> add(Team entity) {
        if (entity.getTeamId() == null || entity.getTeamId().isEmpty()) {
            String newId = collectionReference.document().getId();
            entity.setTeamId(newId);
        }
        return addOrUpdateWithId(entity.getTeamId(), entity);
    }
}