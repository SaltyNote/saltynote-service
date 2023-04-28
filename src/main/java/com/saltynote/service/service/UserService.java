package com.saltynote.service.service;

import com.saltynote.service.entity.LoginHistory;
import com.saltynote.service.entity.SiteUser;
import com.saltynote.service.generator.IdGenerator;
import com.saltynote.service.repository.LoginHistoryRepository;
import com.saltynote.service.repository.NoteRepository;
import com.saltynote.service.repository.UserRepository;
import com.saltynote.service.repository.VaultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@CacheConfig(cacheNames = "user")
public class UserService implements RepositoryService<Long, SiteUser> {

    private final UserRepository repository;

    private final NoteRepository noteRepository;

    private final VaultRepository vaultRepository;

    private final LoginHistoryRepository loginHistoryRepository;

    private final IdGenerator snowflakeIdGenerator;

    @Override
    @Caching(put = { @CachePut(key = "#entity.id"), @CachePut(key = "#entity.username"),
            @CachePut(key = "#entity.email") })
    public SiteUser create(SiteUser entity) {
        if (hasValidId(entity)) {
            log.warn("Note id must be empty: {}", entity);
        }
        entity.setId(snowflakeIdGenerator.nextId());
        return repository.save(entity);
    }

    @Override
    @Caching(put = { @CachePut(key = "#entity.id"), @CachePut(key = "#entity.username"),
            @CachePut(key = "#entity.email") })
    public SiteUser update(SiteUser entity) {
        checkIdExists(entity);
        return repository.save(entity);
    }

    @Override
    @Cacheable(key = "#id")
    public Optional<SiteUser> getById(Long id) {
        return repository.findById(id);
    }

    @Override
    // No need to do cache evict here, since all stale content will be expired soon.
    public void delete(SiteUser entity) {
        repository.deleteById(entity.getId());
    }

    // This api will delete all database records with given user id, including the user
    // itself.
    // No need to do cache evict here, since all stale content will be expired soon.
    @Transactional
    @CacheEvict(key = "#userId")
    public void cleanupByUserId(Long userId) {
        noteRepository.deleteByUserId(userId);
        vaultRepository.deleteByUserId(userId);
        loginHistoryRepository.deleteByUserId(userId);
        repository.deleteById(userId);
    }

    @Cacheable(key = "#email")
    public Optional<SiteUser> getByEmail(String email) {
        return repository.findByEmail(email);
    }

    @Cacheable(key = "#username")
    public SiteUser getByUsername(String username) {
        return repository.findByUsername(username);
    }

    public void saveLoginHistory(Long userId, String ip, String userAgent) {
        LoginHistory loginHistory = new LoginHistory().setUserId(userId).setRemoteIp(ip).setUserAgent(userAgent);
        loginHistoryRepository.save(loginHistory);
    }

}
