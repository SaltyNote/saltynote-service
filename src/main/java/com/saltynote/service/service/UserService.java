package com.saltynote.service.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saltynote.service.repository.NoteRepository;
import com.saltynote.service.repository.UserRepository;
import com.saltynote.service.repository.VaultRepository;

@Service
public class UserService implements RepositoryService<UserRepository> {

  private final UserRepository userRepository;
  private final NoteRepository noteRepository;
  private final VaultRepository vaultRepository;

  public UserService(
      UserRepository userRepository,
      NoteRepository noteRepository,
      VaultRepository vaultRepository) {
    this.userRepository = userRepository;
    this.noteRepository = noteRepository;
    this.vaultRepository = vaultRepository;
  }

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
