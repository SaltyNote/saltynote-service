package com.saltynote.service.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saltynote.service.repository.NoteRepository;
import com.saltynote.service.repository.RefreshTokenRepository;
import com.saltynote.service.repository.UserRepository;
import com.saltynote.service.repository.VaultRepository;

@Service
public class UserService implements RepositoryService<UserRepository> {

  private final UserRepository userRepository;
  private final NoteRepository noteRepository;
  private final VaultRepository vaultRepository;
  private final RefreshTokenRepository refreshTokenRepository;

  public UserService(
      UserRepository userRepository,
      NoteRepository noteRepository,
      VaultRepository vaultRepository,
      RefreshTokenRepository refreshTokenRepository) {
    this.userRepository = userRepository;
    this.noteRepository = noteRepository;
    this.vaultRepository = vaultRepository;
    this.refreshTokenRepository = refreshTokenRepository;
  }

  @Override
  public UserRepository getRepository() {
    return userRepository;
  }

  // This api will delete all database records with given user id, including the user itself.
  @Transactional
  public void cleanupByUserId(String userId) {
    refreshTokenRepository.deleteByUserId(userId);
    noteRepository.deleteByUserId(userId);
    vaultRepository.deleteByUserId(userId);
    userRepository.deleteById(userId);
  }
}
