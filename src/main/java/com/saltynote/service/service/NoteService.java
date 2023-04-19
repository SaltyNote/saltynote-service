package com.saltynote.service.service;

import com.saltynote.service.entity.Note;
import com.saltynote.service.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "note")
public class NoteService implements RepositoryService<String, Note, NoteRepository> {

    private final NoteRepository repository;

    @Override
    public NoteRepository getRepository() {
        return repository;
    }

    @Override
    public Note save(Note entity) {
        return repository.save(entity);
    }

    @Override
    public Optional<Note> getById(String id) {
        return repository.findById(id);
    }

    // cache evict here for user id and url
    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }

    @Cacheable(key = "#userId")
    public List<Note> getAllByUserId(String userId) {
        return repository.findAllByUserId(userId);
    }

    @Cacheable(key = "#userId-#url")
    public List<Note> getAllByUserIdAndUrl(String userId, String url) {
        return repository.findAllByUserIdAndUrl(userId, url);
    }

}
