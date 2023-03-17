package com.saltynote.service.service;

import com.saltynote.service.repository.NoteRepository;
import com.saltynote.service.repository.UserRepository;
import com.saltynote.service.repository.VaultRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class UserService implements RepositoryService<UserRepository> {

    @Resource
    private UserRepository userRepository;
    @Resource
    private NoteRepository noteRepository;
    @Resource
    private VaultRepository vaultRepository;

    @Override
    public UserRepository getRepository() {
        return userRepository;
    }

    // This api will delete all database records with given user id, including the user itself.
    @Transactional
    public void cleanupByUserId(String userId) {
        noteRepository.deleteByUserId(userId);
        vaultRepository.deleteByUserId(userId);
        userRepository.deleteById(userId);
    }
}
