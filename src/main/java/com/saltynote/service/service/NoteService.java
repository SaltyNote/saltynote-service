package com.saltynote.service.service;

import com.google.common.annotations.VisibleForTesting;
import com.saltynote.service.entity.Note;
import com.saltynote.service.repository.NoteRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@CacheConfig(cacheNames = "note")
public class NoteService implements RepositoryService<String, Note> {

    private final NoteRepository repository;

    @Override
    @Caching(evict = { @CacheEvict(key = "#entity.userId + #entity.url"), @CacheEvict(key = "#entity.userId") })
    public Note create(Note entity) {
        if (hasValidId(entity)) {
            log.warn("Note id must be empty: {}", entity);
        }
        return repository.save(entity);
    }

    @Override
    @Caching(evict = { @CacheEvict(key = "#entity.id"), @CacheEvict(key = "#entity.userId + #entity.url"),
            @CacheEvict(key = "#entity.userId") })
    public Note update(Note entity) {
        checkIdExists(entity);
        return repository.save(entity);
    }

    @Override
    @Cacheable(key = "#id")
    public Optional<Note> getById(String id) {
        return repository.findById(id);
    }

    @Override
    @Caching(evict = { @CacheEvict(key = "#entity.id"), @CacheEvict(key = "#entity.userId + #entity.url"),
            @CacheEvict(key = "#entity.userId") })
    public void delete(@NonNull Note entity) {
        repository.deleteById(entity.getId());
    }

    @Cacheable(key = "#userId")
    public List<Note> getAllByUserId(String userId) {
        return repository.findAllByUserId(userId);
    }

    @Cacheable(key = "#userId + #url")
    public List<Note> getAllByUserIdAndUrl(String userId, String url) {
        return repository.findAllByUserIdAndUrl(userId, url);
    }

    @VisibleForTesting
    @CacheEvict(allEntries = true)
    public void deleteAll(List<Note> notesToCleaned) {
        repository.deleteAll(notesToCleaned);
    }

}
