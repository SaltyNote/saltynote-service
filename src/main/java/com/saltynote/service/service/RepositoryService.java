package com.saltynote.service.service;

import java.util.Optional;

public interface RepositoryService<K, T> {

    T save(T entity);

    Optional<T> getById(K id);

    void deleteById(K id);

}
