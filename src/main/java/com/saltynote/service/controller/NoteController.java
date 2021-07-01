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
import com.saltynote.service.exception.WebAppRuntimeException;
import com.saltynote.service.service.NoteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@Api(
        value = "Note Endpoint",
        description =
                "Everything about Note operation, all these operations require valid access token")
public class NoteController {
    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping("/note/{id}")
    @ApiOperation(
            "Get a single note by ID. You should be the owner of this note, otherwise, request is forbidden.")
    public ResponseEntity<Note> getNoteById(@PathVariable("id") String id, Authentication auth) {
        Optional<Note> note = noteService.getRepository().findById(id);
        checkNoteOwner(note, auth);
        return note.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @RequestMapping(
            value = "/note/{id}",
            method = {RequestMethod.POST, RequestMethod.PUT})
    @ApiOperation(
            "Update a note by ID, either with PUT or POST method. You should be the owner of this note, otherwise, request is forbidden.")
    public ResponseEntity<Note> updateNoteById(
            @PathVariable("id") String id, @RequestBody Note note, Authentication auth) {
        Optional<Note> queryNote = noteService.getRepository().findById(id);
        checkNoteOwner(queryNote, auth);
        Note noteTobeUpdate = queryNote.get();
        if (StringUtils.hasText(note.getNote())) {
            noteTobeUpdate.setNote(note.getNote());
        }

        if (StringUtils.hasText(note.getTags())) {
            noteTobeUpdate.setTags(note.getTags());
        }

        if (StringUtils.hasText(note.getHighlightColor())) {
            noteTobeUpdate.setHighlightColor(note.getHighlightColor());
        }
        noteTobeUpdate = noteService.getRepository().save(noteTobeUpdate);
        return ResponseEntity.ok(noteTobeUpdate);
    }

    @DeleteMapping("/note/{id}")
    @ApiOperation(
            "Delete a note by ID. You should be the owner of this note, otherwise, request is forbidden.")
    public ResponseEntity<ServiceResponse> deleteNoteById(
            @PathVariable("id") String id, Authentication auth) {
        Optional<Note> note = noteService.getRepository().findById(id);
        checkNoteOwner(note, auth);
        noteService.getRepository().deleteById(id);
        return ResponseEntity.ok(ServiceResponse.ok("Delete Successfully!"));
    }

    // TODO: this POST is required for chrome extension, as I find the PUT or DELETE requests will be
    // blocked by Chrome. Further investigation is required from me for this issue.
    @PostMapping("/note/{id}/delete")
    @ApiOperation(
            "Delete a note by ID as a POST request. You should be the owner of this note, otherwise, request is forbidden.")
    public ResponseEntity<ServiceResponse> postDeleteNoteById(
            @PathVariable("id") String id, Authentication auth) {
        Optional<Note> note = noteService.getRepository().findById(id);
        checkNoteOwner(note, auth);
        noteService.getRepository().deleteById(id);
        return ResponseEntity.ok(ServiceResponse.ok("Delete Successfully!"));
    }

    @GetMapping("/notes")
    @ApiOperation("Get all your notes")
    public List<Note> getNotes(Authentication auth) {
        JwtUser user = (JwtUser) auth.getPrincipal();
        return noteService.getRepository().findAllByUserId(user.getId());
    }

    @PostMapping("/notes")
    @ApiOperation("Get your notes with query conditions")
    public List<Note> getNotesByUrl(Authentication auth, @Valid @RequestBody NoteQuery noteQuery) {
        JwtUser user = (JwtUser) auth.getPrincipal();
        return noteService.getRepository().findAllByUserIdAndUrl(user.getId(), noteQuery.getUrl());
    }

    @PostMapping("/note")
    @ApiOperation("Create a new note")
    public ResponseEntity<Note> createNote(@Valid @RequestBody Note note, Authentication auth) {
        JwtUser user = (JwtUser) auth.getPrincipal();
        note.setUserId(user.getId());
        note = noteService.getRepository().save(note);
        if (StringUtils.hasText(note.getId())) {
            return ResponseEntity.ok(note);
        }
        throw new WebAppRuntimeException(
                HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save note into database: " + note);
    }

    private void checkNoteOwner(Optional<Note> note, Authentication auth) {
        JwtUser user = (JwtUser) auth.getPrincipal();
        if (note.isPresent() && user.getId().equals(note.get().getUserId())) {
            return;
        }
        throw new WebAppRuntimeException(
                HttpStatus.FORBIDDEN, "Permission Error: You are not the owner of the note.");
    }
}
