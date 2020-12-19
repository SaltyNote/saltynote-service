package com.saltynote.service.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.SerializationUtils;
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
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.saltynote.service.domain.transfer.JwtToken;
import com.saltynote.service.domain.transfer.UserCredential;
import com.saltynote.service.entity.Note;
import com.saltynote.service.entity.SiteUser;
import com.saltynote.service.repository.NoteRepository;
import com.saltynote.service.repository.UserRepository;
import com.saltynote.service.security.SecurityConstants;
import com.saltynote.service.service.EmailService;
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

  @LocalServerPort private int port;
  @Autowired private TestRestTemplate restTemplate;
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private NoteRepository noteRepository;
  @Autowired private UserRepository userRepository;
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

  private void signupTestUser() throws Exception {
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
        .andDo(print())
        .andExpect(status().isOk());

    siteUser = userRepository.findByUsername(user.getUsername());
    assertThat(siteUser).extracting(SiteUser::getEmail).isEqualTo(user.getEmail());

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(user), headers);
    String tokenStr =
        this.restTemplate.postForObject(
            "http://localhost:" + port + "/login", entity, String.class);
    log.info("token = {}", tokenStr);
    JwtToken token = objectMapper.readValue(tokenStr, JwtToken.class);
    assertNotNull(token.getAccessToken());

    this.accessToken = token.getAccessToken();
  }

  @BeforeEach
  public void setUp() throws Exception {
    signupTestUser();

    notesToCleaned = new ArrayList<>();
    // Create a temp note for current user.
    Note note = createTmpNote(siteUser.getId());
    this.savedNote = noteRepository.save(note);
    notesToCleaned.add(savedNote);
  }

  @AfterEach
  public void tearDown() {
    userRepository.deleteById(siteUser.getId());
    noteRepository.deleteAll(notesToCleaned);
  }

  @Test
  public void getNoteById() throws Exception {
    log.info("getNoteById");
    this.mockMvc
        .perform(
            get("/note/" + savedNote.getId())
                .header(SecurityConstants.HEADER_STRING, "Bearer " + accessToken))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content().string(containsString(savedNote.getText())));
  }

  @Test
  public void getNoteById_noAccessToken_returnException() throws Exception {
    log.info("getNoteById");
    this.mockMvc
        .perform(get("/note/" + savedNote.getId()))
        .andDo(print())
        .andExpect(status().isForbidden());
  }

  @Test
  public void updateNoteById() throws Exception {
    String newNoteContent = "I am the new note";
    Note noteToUpdate = SerializationUtils.clone(savedNote);
    noteToUpdate.setNote(newNoteContent);
    this.mockMvc
        .perform(
            post("/note/" + savedNote.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(noteToUpdate))
                .header(SecurityConstants.HEADER_STRING, "Bearer " + accessToken))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content().string(containsString(newNoteContent)));

    Optional<Note> queryNote = noteRepository.findById(savedNote.getId());
    assertTrue(queryNote.isPresent());
    assertEquals(queryNote.get().getNote(), newNoteContent);
  }

  @Test
  public void deleteNoteById() throws Exception {
    Note note = createTmpNote(null);
    MvcResult mvcResult =
        this.mockMvc
            .perform(
                post("/note/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(note))
                    .header(SecurityConstants.HEADER_STRING, "Bearer " + accessToken))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().string(containsString(note.getText())))
            .andReturn();
    String res = mvcResult.getResponse().getContentAsString();
    Note returnedNote = objectMapper.readValue(res, Note.class);
    assertTrue(noteRepository.findById(returnedNote.getId()).isPresent());
    this.mockMvc
        .perform(
            delete("/note/" + returnedNote.getId())
                .header(SecurityConstants.HEADER_STRING, "Bearer " + accessToken))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    assertFalse(noteRepository.findById(returnedNote.getId()).isPresent());
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
                    .header(SecurityConstants.HEADER_STRING, "Bearer " + accessToken))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().string(containsString(note.getText())))
            .andReturn();
    String res = mvcResult.getResponse().getContentAsString();
    Note returnedNote = objectMapper.readValue(res, Note.class);
    assertTrue(noteRepository.findById(returnedNote.getId()).isPresent());
    this.mockMvc
        .perform(get("/notes").header(SecurityConstants.HEADER_STRING, "Bearer " + accessToken))
        .andDo(print())
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
                    .header(SecurityConstants.HEADER_STRING, "Bearer " + accessToken))
            .andDo(print())
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
