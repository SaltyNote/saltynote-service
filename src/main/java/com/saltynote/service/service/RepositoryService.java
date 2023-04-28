package com.saltynote.service.service;

import com.saltynote.service.domain.Identifiable;
import lombok.NonNull;

import java.util.Optional;

public interface RepositoryService<K, T extends Identifiable> {

    T create(T entity);

    T update(T entity);

    Optional<T> getById(K id);

    void delete(T entity);

    default void checkIdExists(@NonNull T entity) {
        if (!hasValidId(entity)) {
            throw new IllegalArgumentException("Id must not be empty: " + entity);
        }
    }

    default boolean hasValidId(@NonNull T entity) {
        return entity.getId() != null && entity.getId() > 0;
    }

}
