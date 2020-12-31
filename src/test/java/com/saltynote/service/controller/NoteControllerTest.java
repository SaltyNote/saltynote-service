package com.saltynote.service.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.saltynote.service.domain.transfer.JwtToken;
import com.saltynote.service.domain.transfer.UserCredential;
import com.saltynote.service.entity.Note;
import com.saltynote.service.entity.SiteUser;
import com.saltynote.service.security.SecurityConstants;
import com.saltynote.service.service.EmailService;
import com.saltynote.service.service.NoteService;
import com.saltynote.service.service.UserService;
import lombok.extern.slf4j.Slf4j;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@EnableAutoConfiguration(exclude = SecurityAutoConfiguration.class)
@AutoConfigureTestDatabase(replace = NONE)
@Slf4j
public class NoteControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private NoteService noteService;
  @Autowired private UserService userService;
  @MockBean private EmailService emailService;

  private final Faker faker = new Faker();
  private List<Note> notesToCleaned;
  private SiteUser siteUser;
  private String accessToken;
  private Note savedNote;

  private Note createTmpNote(String userId) {
    return new Note()
        .setUserId(userId)
        .setNote(faker.lorem().characters(50, 100))
        .setUrl(faker.internet().url())
        .setText(faker.funnyName().name());
  }

  private Pair<SiteUser, String> signupTestUser() throws Exception {
    doNothing().when(emailService).sendAsHtml(any(), any(), any());
    doNothing().when(emailService).send(any(), any(), any());

    UserCredential user = new UserCredential();
    String username = faker.name().username();
    user.setEmail(username + "@saltynote.com");
    user.setPassword(RandomStringUtils.randomAlphanumeric(12));
    user.setUsername(username);

    this.mockMvc
        .perform(
            post("/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
        .andExpect(status().isOk());

    SiteUser siteUser = userService.getRepository().findByUsername(user.getUsername());
    assertThat(siteUser).extracting(SiteUser::getEmail).isEqualTo(user.getEmail());

    MvcResult mvcLoginResult =
        this.mockMvc
            .perform(
                post("/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(user)))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andReturn();
    String res = mvcLoginResult.getResponse().getContentAsString();
    JwtToken token = objectMapper.readValue(res, JwtToken.class);
    assertNotNull(token.getAccessToken());

    return Pair.of(siteUser, token.getAccessToken());
  }

  @BeforeEach
  public void setUp() throws Exception {
    Pair<SiteUser, String> pair = signupTestUser();
    this.accessToken = pair.getRight();
    this.siteUser = pair.getLeft();

    this.notesToCleaned = new ArrayList<>();
    // Create a temp note for current user.
    Note note = createTmpNote(siteUser.getId());
    this.savedNote = noteService.getRepository().save(note);
    this.notesToCleaned.add(this.savedNote);
  }

  @AfterEach
  public void tearDown() {
    userService.cleanupByUserId(this.siteUser.getId());
    noteService.getRepository().deleteAll(this.notesToCleaned);
  }

  @Test
  public void getNoteById() throws Exception {
    this.mockMvc
        .perform(
            get("/note/" + this.savedNote.getId())
                .header(SecurityConstants.HEADER_STRING, "Bearer " + this.accessToken))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content().string(containsString(this.savedNote.getText())));
  }

  @Test
  public void getNoteByIdNoAccessTokenReturnException() throws Exception {
    this.mockMvc.perform(get("/note/" + this.savedNote.getId())).andExpect(status().isForbidden());
  }

  @Test
  public void updateNoteByIdShouldSuccess() throws Exception {
    String newNoteContent = "I am the new note";
    Note noteToUpdate = SerializationUtils.clone(this.savedNote);
    noteToUpdate.setNote(newNoteContent);
    this.mockMvc
        .perform(
            post("/note/" + this.savedNote.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(noteToUpdate))
                .header(SecurityConstants.HEADER_STRING, "Bearer " + this.accessToken))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content().string(containsString(newNoteContent)));

    Optional<Note> queryNote = noteService.getRepository().findById(this.savedNote.getId());
    assertTrue(queryNote.isPresent());
    assertEquals(queryNote.get().getNote(), newNoteContent);
  }

  @Test
  public void updateNoteByIdFromNonOwnerShouldFail() throws Exception {

    Pair<SiteUser, String> pair = signupTestUser();

    String newNoteContent = "I am the new note";
    Note noteToUpdate = SerializationUtils.clone(this.savedNote);
    noteToUpdate.setNote(newNoteContent);
    this.mockMvc
        .perform(
            post("/note/" + this.savedNote.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(noteToUpdate))
                .header(SecurityConstants.HEADER_STRING, "Bearer " + pair.getRight()))
        .andExpect(status().isForbidden())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

    Optional<Note> queryNote = noteService.getRepository().findById(this.savedNote.getId());
    assertTrue(queryNote.isPresent());
    assertEquals(queryNote.get().getNote(), this.savedNote.getNote());

    userService.cleanupByUserId(pair.getLeft().getId());
  }

  @Test
  public void deleteNoteByIdShouldSuccess() throws Exception {
    Note note = createTmpNote(null);
    MvcResult mvcResult =
        this.mockMvc
            .perform(
                post("/note/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(note))
                    .header(SecurityConstants.HEADER_STRING, "Bearer " + this.accessToken))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().string(containsString(note.getText())))
            .andReturn();
    String res = mvcResult.getResponse().getContentAsString();
    Note returnedNote = objectMapper.readValue(res, Note.class);
    assertTrue(noteService.getRepository().findById(returnedNote.getId()).isPresent());
    this.mockMvc
        .perform(
            delete("/note/" + returnedNote.getId())
                .header(SecurityConstants.HEADER_STRING, "Bearer " + this.accessToken))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    assertFalse(noteService.getRepository().findById(returnedNote.getId()).isPresent());
  }

  @Test
  public void deleteNoteByIdFromNonOwnerShouldFail() throws Exception {
    Note note = createTmpNote(null);
    MvcResult mvcResult =
        this.mockMvc
            .perform(
                post("/note/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(note))
                    .header(SecurityConstants.HEADER_STRING, "Bearer " + this.accessToken))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().string(containsString(note.getText())))
            .andReturn();
    String res = mvcResult.getResponse().getContentAsString();
    Note returnedNote = objectMapper.readValue(res, Note.class);
    assertTrue(noteService.getRepository().findById(returnedNote.getId()).isPresent());

    Pair<SiteUser, String> pair = signupTestUser();

    this.mockMvc
        .perform(
            delete("/note/" + returnedNote.getId())
                .header(SecurityConstants.HEADER_STRING, "Bearer " + pair.getRight()))
        .andExpect(status().isForbidden())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

    assertTrue(noteService.getRepository().findById(returnedNote.getId()).isPresent());
    userService.cleanupByUserId(pair.getLeft().getId());
  }

  @Test
  public void getNotes() throws Exception {
    Note note = createTmpNote(null);
    MvcResult mvcResult =
        this.mockMvc
            .perform(
                post("/note")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(note))
                    .header(SecurityConstants.HEADER_STRING, "Bearer " + this.accessToken))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().string(containsString(note.getText())))
            .andReturn();
    String res = mvcResult.getResponse().getContentAsString();
    Note returnedNote = objectMapper.readValue(res, Note.class);
    assertTrue(noteService.getRepository().findById(returnedNote.getId()).isPresent());
    this.mockMvc
        .perform(
            get("/notes").header(SecurityConstants.HEADER_STRING, "Bearer " + this.accessToken))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content().string(containsString(note.getText())))
        .andExpect(jsonPath("$", hasSize(equalTo(2))))
        .andReturn();
  }

  @Test
  public void createNote() throws Exception {
    Note note = createTmpNote(null);
    MvcResult mvcResult =
        this.mockMvc
            .perform(
                post("/note/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(note))
                    .header(SecurityConstants.HEADER_STRING, "Bearer " + this.accessToken))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().string(containsString(note.getText())))
            .andReturn();
    String res = mvcResult.getResponse().getContentAsString();
    log.info("note resp = {}", res);
    Note returnedNote = objectMapper.readValue(res, Note.class);
    assertEquals(note.getNote(), returnedNote.getNote());
    notesToCleaned.add(returnedNote);
  }
}
