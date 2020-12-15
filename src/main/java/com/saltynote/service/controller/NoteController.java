package com.saltynote.service.controller;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.saltynote.service.domain.transfer.JwtUser;
import com.saltynote.service.domain.transfer.NoteQuery;
import com.saltynote.service.domain.transfer.ServiceResponse;
import com.saltynote.service.entity.Note;
import com.saltynote.service.exception.WebClientRuntimeException;
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
  public ResponseEntity<Note> getNoteById(@PathVariable("id") String id, Authentication auth) {
    Optional<Note> note = noteRepository.findById(id);
    checkNoteOwner(note, auth);
    return note.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
  }

  @RequestMapping(
      value = "/note/{id}",
      method = {RequestMethod.POST, RequestMethod.PUT})
  public ResponseEntity<Note> updateNoteById(
      @PathVariable("id") String id, @RequestBody Note note, Authentication auth) {
    Optional<Note> queryNote = noteRepository.findById(id);
    checkNoteOwner(queryNote, auth);
    Note noteTobeUpdate = queryNote.get();
    if (StringUtils.hasText(note.getNote())) {
      noteTobeUpdate.setNote(note.getNote());
    }
    if (StringUtils.hasText(note.getHighlightColor())) {
      noteTobeUpdate.setHighlightColor(note.getHighlightColor());
    }
    noteTobeUpdate = noteRepository.save(noteTobeUpdate);
    return ResponseEntity.ok(noteTobeUpdate);
  }

  @DeleteMapping("/note/{id}")
  public ResponseEntity<ServiceResponse> deleteNoteById(
      @PathVariable("id") String id, Authentication auth) {
    Optional<Note> note = noteRepository.findById(id);
    checkNoteOwner(note, auth);
    noteRepository.deleteById(id);
    return ResponseEntity.ok(ServiceResponse.ok("Delete Successfully!"));
  }

  // TODO: this POST is required for chrome extension, which will block PUT and DELETE by default so
  // far. Further investigation is required for this issue.
  @PostMapping("/note/{id}/delete")
  public ResponseEntity<ServiceResponse> postDeleteNoteById(
      @PathVariable("id") String id, Authentication auth) {
    Optional<Note> note = noteRepository.findById(id);
    checkNoteOwner(note, auth);
    noteRepository.deleteById(id);
    return ResponseEntity.ok(ServiceResponse.ok("Delete Successfully!"));
  }

  @GetMapping("/notes")
  public List<Note> getNotes(Authentication auth) {
    JwtUser user = (JwtUser) auth.getPrincipal();
    return noteRepository.findAllByUserId(user.getId());
  }

  @PostMapping("/notes")
  public List<Note> getNotesByUrl(Authentication auth, @Valid @RequestBody NoteQuery noteQuery) {
    JwtUser user = (JwtUser) auth.getPrincipal();
    return noteRepository.findAllByUserIdAndUrl(user.getId(), noteQuery.getUrl());
  }

  @PostMapping("/note")
  public ResponseEntity<Note> createNote(@Valid @RequestBody Note note, Authentication auth) {
    JwtUser user = (JwtUser) auth.getPrincipal();
    note.setUserId(user.getId());
    note = noteRepository.save(note);
    if (StringUtils.hasText(note.getId())) {
      return ResponseEntity.ok(note);
    }
    throw new RuntimeException("Failed to save note into database: " + note);
  }

  private void checkNoteOwner(Optional<Note> note, Authentication auth) {
    JwtUser user = (JwtUser) auth.getPrincipal();
    if (note.isPresent() && user.getId().equals(note.get().getUserId())) {
      return;
    }
    throw new WebClientRuntimeException(
        HttpStatus.FORBIDDEN, "Permission Error: You are not the owner of the note.");
  }
}
