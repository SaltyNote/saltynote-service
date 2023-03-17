package com.saltynote.service.service;

import com.saltynote.service.repository.NoteRepository;
import org.springframework.stereotype.Service;

@Service
public class NoteService implements RepositoryService<NoteRepository> {
    private final NoteRepository repository;

    public NoteService(NoteRepository repository) {
        this.repository = repository;
    }

    @Override
    public NoteRepository getRepository() {
        return repository;
    }
}
