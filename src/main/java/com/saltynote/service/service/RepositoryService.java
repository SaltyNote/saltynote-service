package com.saltynote.service.service;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RepositoryService<K, T, R extends JpaRepository<T, K>> {

    R getRepository();

    T save(T entity);

    Optional<T> getById(K id);

    void deleteById(K id);

}
