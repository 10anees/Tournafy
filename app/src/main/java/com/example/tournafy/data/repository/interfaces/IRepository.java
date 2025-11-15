// Path: app/src/main/java/com/tournafy/data/repository/interfaces/IRepository.java
package com.example.tournafy.data.repository.interfaces;

import androidx.lifecycle.LiveData;
import com.google.android.gms.tasks.Task;
import java.util.List;

/**
 * @param <T>  The domain model type (e.g., User, Match, Tournament)
 * @param <ID> The type of the model's primary key (e.g., String)
 */
public interface IRepository<T, ID> {

    /**
     * @param entity The entity to add.
     * @return A Task that completes when the operation is finished.
     */
    Task<Void> add(T entity);

    /**
     * @param entity The entity with updated data.
     * @return A Task that completes when the operation is finished.
     */
    Task<Void> update(T entity);

    /**
     * @param id The ID of the entity to delete.
     * @return A Task that completes when the operation is finished.
     */
    Task<Void> delete(ID id);

    /**
     * @param id The ID of the entity to retrieve.
     * @return LiveData holding the entity.
     */
    LiveData<T> getById(ID id);

    /**
     * @return LiveData holding a list of all entities.
     */
    LiveData<List<T>> getAll();
}