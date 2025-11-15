package com.tournafy.data.repository.interfaces;

import java.util.List;

/**
 * @param <T>  The domain model class (e.g., Match, Tournament, User)
 * @param <ID> The data type of the entity's Primary Key (usually String)
 */
public interface IRepository<T, ID> {

    /**
     * @param id The unique ID of the entity.
     * @return The entity object, or null if not found. (Implementation may vary)
     */
    T getById(ID id);

    /**
     * @return A List of all entities. (Implementation may vary)
     */
    List<T> getAll();

    /**
     * @param entity The entity object to add.
     */
    void add(T entity);

    /**
     * @param entity The entity object to update.
     */
    void update(T entity);

    /**
     * @param entity The entity object to delete.
     */
    void delete(T entity);
}