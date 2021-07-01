package com.saltynote.service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.saltynote.service.entity.Note;

public interface NoteRepository
        extends JpaRepository<Note, String>, JpaSpecificationExecutor<Note> {
    List<Note> findAllByUserId(String userId);

    List<Note> findAllByUserIdAndUrl(String userId, String url);

    void deleteByUserId(String userId);
}
