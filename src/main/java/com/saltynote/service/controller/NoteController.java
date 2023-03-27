package com.saltynote.service.controller;

import com.google.common.base.Splitter;
import com.saltynote.service.domain.converter.NoteConverter;
import com.saltynote.service.domain.transfer.JwtUser;
import com.saltynote.service.domain.transfer.NoteDto;
import com.saltynote.service.domain.transfer.NoteQuery;
import com.saltynote.service.domain.transfer.ServiceResponse;
import com.saltynote.service.entity.Note;
import com.saltynote.service.exception.WebAppRuntimeException;
import com.saltynote.service.service.NoteService;
import com.saltynote.service.utils.BaseUtils;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class NoteController {
    private final NoteService noteService;
    private final NoteConverter noteConverter;

    public NoteController(NoteService noteService, NoteConverter noteConverter) {
        this.noteService = noteService;
        this.noteConverter = noteConverter;
    }

    @GetMapping("/note/{id}")
    public ResponseEntity<Note> getNoteById(@PathVariable("id") String id, Authentication auth) {
        Optional<Note> note = noteService.getRepository().findById(id);
        checkNoteOwner(note, auth);
        return note.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @RequestMapping(
            value = "/note/{id}",
            method = {RequestMethod.POST, RequestMethod.PUT})
    public ResponseEntity<Note> updateNoteById(
            @PathVariable("id") String id, @RequestBody NoteDto noteDto, Authentication auth) {
        Optional<Note> queryNote = noteService.getRepository().findById(id);
        checkNoteOwner(queryNote, auth);
        Note noteTobeUpdate = queryNote.get();
        if (StringUtils.isNotBlank(noteDto.getNote())) {
            noteTobeUpdate.setNote(noteDto.getNote());
        }

        if (StringUtils.isNotBlank(noteDto.getTags())) {
            noteTobeUpdate.setTags(noteDto.getTags());
        }

        if (StringUtils.isNotBlank(noteDto.getHighlightColor())) {
            noteTobeUpdate.setHighlightColor(noteDto.getHighlightColor());
        }
        noteTobeUpdate = noteService.getRepository().save(noteTobeUpdate);
        return ResponseEntity.ok(noteTobeUpdate);
    }

    @DeleteMapping("/note/{id}")
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
    public ResponseEntity<ServiceResponse> postDeleteNoteById(
            @PathVariable("id") String id, Authentication auth) {
        return deleteNoteById(id, auth);
    }

    @GetMapping("/notes")
    public List<Note> getNotes(Authentication auth, @RequestParam(required = false) String keyword) {
        JwtUser user = (JwtUser) auth.getPrincipal();
        List<Note> allNotes = noteService.getRepository().findAllByUserId(user.getId());
        if (allNotes == null || allNotes.isEmpty() || StringUtils.isBlank(keyword)) {
            return allNotes;
        }
        Iterable<String> queries = Splitter.on(" ")
                .trimResults()
                .omitEmptyStrings()
                .split(keyword);

        return allNotes.stream().filter(n -> StringUtils.isNotBlank(n.getNote()) && BaseUtils.containsAllIgnoreCase(n.getNote(), queries) ||
                        StringUtils.isNotBlank(n.getText()) && BaseUtils.containsAllIgnoreCase(n.getText(), queries))
                .collect(Collectors.toList());
    }

    @PostMapping("/notes")
    public List<Note> getNotesByUrl(Authentication auth, @Valid @RequestBody NoteQuery noteQuery) {
        JwtUser user = (JwtUser) auth.getPrincipal();
        return noteService.getRepository().findAllByUserIdAndUrl(user.getId(), noteQuery.getUrl());
    }

    @PostMapping("/note")
    public ResponseEntity<Note> createNote(@Valid @RequestBody NoteDto noteDto, Authentication auth) {
        JwtUser user = (JwtUser) auth.getPrincipal();
        noteDto.setUserId(user.getId());
        Note note = noteConverter.toEntity(noteDto);
        note = noteService.getRepository().save(note);
        if (StringUtils.isNotBlank(note.getId())) {
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
