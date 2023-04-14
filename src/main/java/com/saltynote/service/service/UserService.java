package com.saltynote.service.service;

import com.saltynote.service.entity.SiteUser;
import com.saltynote.service.repository.NoteRepository;
import com.saltynote.service.repository.UserRepository;
import com.saltynote.service.repository.VaultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements RepositoryService<SiteUser, UserRepository> {

    private final UserRepository userRepository;

    private final NoteRepository noteRepository;

    private final VaultRepository vaultRepository;

    @Override
    public UserRepository getRepository() {
        return userRepository;
    }

    @Override
    public Optional<SiteUser> getById(String id) {
        return userRepository.findById(id);
    }

    // This api will delete all database records with given user id, including the user
    // itself.
    @Transactional
    public void cleanupByUserId(String userId) {
        noteRepository.deleteByUserId(userId);
        vaultRepository.deleteByUserId(userId);
        userRepository.deleteById(userId);
    }

}
