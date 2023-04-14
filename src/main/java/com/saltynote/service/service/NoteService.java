package com.saltynote.service.service;

import com.saltynote.service.entity.Note;
import com.saltynote.service.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NoteService implements RepositoryService<Note, NoteRepository> {

    private final NoteRepository repository;

    @Override
    public NoteRepository getRepository() {
        return repository;
    }

    @Override
    public Optional<Note> getById(String id) {
        return repository.findById(id);
    }

}
