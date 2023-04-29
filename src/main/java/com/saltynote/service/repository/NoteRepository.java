package com.saltynote.service.repository;

import com.saltynote.service.entity.Note;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NoteRepository extends MongoRepository<Note, String> {

    List<Note> findAllByUserId(String userId);

    List<Note> findAllByUserIdAndUrl(String userId, String url);

    void deleteByUserId(String userId);

}
