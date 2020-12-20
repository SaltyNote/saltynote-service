package com.saltynote.service.service;

import org.springframework.stereotype.Service;

import com.saltynote.service.repository.NoteRepository;

@Service
public class NoteService implements RepositoryService<NoteRepository> {
  private final NoteRepository repository;

  public NoteService(NoteRepository repository) {
    this.repository = repository;
  }

  @Override
  public NoteRepository getRepository() {
    return repository;
  }
}
