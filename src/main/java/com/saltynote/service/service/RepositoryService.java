package com.saltynote.service.service;

import java.util.Optional;

public interface RepositoryService<R, T> {

    T getRepository();

    Optional<R> getById(String id);

}
