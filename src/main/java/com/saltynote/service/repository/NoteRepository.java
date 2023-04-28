package com.saltynote.service.repository;

import com.saltynote.service.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long>, JpaSpecificationExecutor<Note> {

    List<Note> findAllByUserId(Long userId);

    List<Note> findAllByUserIdAndUrl(Long userId, String url);

    void deleteByUserId(Long userId);

}
