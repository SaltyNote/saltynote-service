package com.saltynote.service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.saltynote.service.entity.Note;

public interface NoteRepository
    extends JpaRepository<Note, Integer>, JpaSpecificationExecutor<Note> {
  List<Note> findAllByUserId(Integer userId);

  List<Note> findAllByUserIdAndUrl(Integer userId, String url);
}
