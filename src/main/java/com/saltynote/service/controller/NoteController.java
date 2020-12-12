package com.saltynote.service.controller;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.saltynote.service.domain.transfer.JwtUser;
import com.saltynote.service.entity.Note;
import com.saltynote.service.repository.NoteRepository;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class NoteController {
  private final NoteRepository noteRepository;

  public NoteController(NoteRepository noteRepository) {
    this.noteRepository = noteRepository;
  }

  @GetMapping("/note/{id}")
  public ResponseEntity<Note> getNote(@PathVariable("id") Integer id) {
    Optional<Note> note = noteRepository.findById(id);
    return note.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
  }

  @GetMapping("/notes")
  public List<Note> getNotes(Authentication auth) {
    JwtUser user = (JwtUser) auth.getPrincipal();
    return noteRepository.findAllByUserId(user.getId());
  }

  @PostMapping("/note")
  public ResponseEntity<Note> createNote(@Valid @RequestBody Note note, Authentication auth) {
    JwtUser user = (JwtUser) auth.getPrincipal();
    note.setUserId(user.getId());
    note = noteRepository.save(note);
    if (note.getId() > 0) {
      return ResponseEntity.ok(note);
    } else {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
}
