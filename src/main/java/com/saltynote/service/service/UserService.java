package com.saltynote.service.service;

import com.saltynote.service.entity.LoginHistory;
import com.saltynote.service.entity.SiteUser;
import com.saltynote.service.repository.LoginHistoryRepository;
import com.saltynote.service.repository.NoteRepository;
import com.saltynote.service.repository.UserRepository;
import com.saltynote.service.repository.VaultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@CacheConfig(cacheNames = "user")
public class UserService implements RepositoryService<String, SiteUser> {

    private final UserRepository repository;

    private final NoteRepository noteRepository;

    private final VaultRepository vaultRepository;

    private final LoginHistoryRepository loginHistoryRepository;

    @Override
    public SiteUser create(SiteUser entity) {
        if (hasValidId(entity)) {
            log.warn("Note id must be empty: {}", entity);
            entity.setId(null);
        }
        return repository.save(entity);
    }

    @Override
    public SiteUser update(SiteUser entity) {
        checkIdExists(entity);
        return repository.save(entity);
    }

    @Override
    @Cacheable(key = "#id")
    public Optional<SiteUser> getById(String id) {
        log.info("Get user by id: {}", id);
        return repository.findById(id);
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }

    // This api will delete all database records with given user id, including the user
    // itself.
    @Transactional
    public void cleanupByUserId(String userId) {
        noteRepository.deleteByUserId(userId);
        vaultRepository.deleteByUserId(userId);
        loginHistoryRepository.deleteByUserId(userId);
        repository.deleteById(userId);
    }

    public Optional<SiteUser> getByEmail(String email) {
        return repository.findByEmail(email);
    }

    public SiteUser getByUsername(String username) {
        return repository.findByUsername(username);
    }

    public void saveLoginHistory(String userId, String ip, String userAgent) {
        LoginHistory loginHistory = new LoginHistory().setUserId(userId).setRemoteIp(ip).setUserAgent(userAgent);
        loginHistoryRepository.save(loginHistory);
    }

}
