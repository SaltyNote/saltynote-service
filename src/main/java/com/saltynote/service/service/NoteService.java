package com.saltynote.service.service;

import com.saltynote.service.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NoteService implements RepositoryService<NoteRepository> {

    private final NoteRepository repository;

    @Override
    public NoteRepository getRepository() {
        return repository;
    }

}
