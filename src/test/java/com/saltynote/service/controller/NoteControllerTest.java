package com.saltynote.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.saltynote.service.domain.converter.NoteConverter;
import com.saltynote.service.domain.transfer.NoteDto;
import com.saltynote.service.domain.transfer.TokenPair;
import com.saltynote.service.domain.transfer.UserCredential;
import com.saltynote.service.domain.transfer.UserNewRequest;
import com.saltynote.service.entity.Note;
import com.saltynote.service.entity.SiteUser;
import com.saltynote.service.entity.Vault;
import com.saltynote.service.security.SecurityConstants;
import com.saltynote.service.service.EmailService;
import com.saltynote.service.service.NoteService;
import com.saltynote.service.service.UserService;
import com.saltynote.service.service.VaultService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@EnableAutoConfiguration(exclude = SecurityAutoConfiguration.class)
@AutoConfigureTestDatabase(replace = NONE)
@Slf4j
public class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NoteService noteService;

    @Autowired
    private UserService userService;

    @Autowired
    private VaultService vaultService;

    @Resource
    private NoteConverter noteConverter;

    @MockBean
    private EmailService emailService;

    private static final Faker faker = new Faker();

    private List<Note> notesToCleaned;

    private SiteUser siteUser;

    private String accessToken;

    private Note savedNote;

    public static NoteDto createTmpNote(String userId) {
        return new NoteDto().setUserId(userId)
            .setNote(faker.lorem().characters(50, 100))
            .setUrl(faker.internet().url())
            .setText(faker.funnyName().name())
            .setTags(new HashSet<>(faker.lorem().words(3)));
    }

    private Pair<SiteUser, String> signupTestUser() throws Exception {
        doNothing().when(emailService).sendAsHtml(any(), any(), any());
        doNothing().when(emailService).send(any(), any(), any());

        String username = faker.name().username();
        String email = username + "@saltynote.com";
        Vault vault = vaultService.createVerificationCode(email);

        assertNotNull(vault.getId());
        assertEquals(vault.getEmail(), email);

        UserNewRequest userNewRequest = new UserNewRequest();

        userNewRequest.setEmail(email);
        userNewRequest.setPassword(RandomStringUtils.randomAlphanumeric(12));
        userNewRequest.setUsername(username);
        userNewRequest.setToken(vault.getSecret());

        this.mockMvc
            .perform(post("/signup").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userNewRequest)))
            .andExpect(status().isOk());

        SiteUser siteUser = userService.getByUsername(userNewRequest.getUsername());
        assertThat(siteUser).extracting(SiteUser::getEmail).isEqualTo(userNewRequest.getEmail());

        UserCredential user = new UserCredential().setUsername(userNewRequest.getUsername())
            .setEmail(userNewRequest.getEmail())
            .setPassword(userNewRequest.getPassword());

        MvcResult mvcLoginResult = this.mockMvc
            .perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andReturn();
        String res = mvcLoginResult.getResponse().getContentAsString();
        TokenPair token = objectMapper.readValue(res, TokenPair.class);
        assertNotNull(token.getAccessToken());

        return Pair.of(siteUser, token.getAccessToken());
    }

    @BeforeEach
    void setUp() throws Exception {
        Pair<SiteUser, String> pair = signupTestUser();
        this.accessToken = pair.getRight();
        this.siteUser = pair.getLeft();

        this.notesToCleaned = new ArrayList<>();
        // Create a temp note for current user.
        NoteDto note = createTmpNote(siteUser.getId());
        this.savedNote = noteService.create(noteConverter.toEntity(note));
        this.notesToCleaned.add(this.savedNote);
    }

    @AfterEach
    void tearDown() {
        // userService.cleanupByUserId(this.siteUser.getId());
        // noteService.deleteAll(this.notesToCleaned);
    }

    @Test
    void getNoteByIdShouldSuccess() throws Exception {
        // Suppress codacy warning
        assertNotNull(this.savedNote.getId());
        this.mockMvc
            .perform(get("/note/" + this.savedNote.getId()).header(SecurityConstants.AUTH_HEADER,
                    "Bearer " + this.accessToken))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().string(containsString(this.savedNote.getText())));
    }

    @Test
    void getNoteByIdNoAccessTokenReturnException() throws Exception {
        // Suppress codacy warning
        assertNotNull(this.savedNote.getId());
        this.mockMvc.perform(get("/note/" + this.savedNote.getId())).andExpect(status().isForbidden());
    }

    @Test
    void getNoteByIdFromNonOwnerShouldFail() throws Exception {
        Pair<SiteUser, String> pair = signupTestUser();
        assertNotNull(pair.getRight());
        this.mockMvc
            .perform(get("/note/" + this.savedNote.getId()).header(SecurityConstants.AUTH_HEADER,
                    "Bearer " + pair.getRight()))
            .andExpect(status().isForbidden())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        userService.cleanupByUserId(pair.getLeft().getId());
    }

    @Test
    void updateNoteByIdShouldSuccess() throws Exception {
        String newNoteContent = "I am the new note";
        Note noteToUpdate = SerializationUtils.clone(this.savedNote);
        noteToUpdate.setNote(newNoteContent);
        this.mockMvc
            .perform(post("/note/" + this.savedNote.getId()).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(noteToUpdate))
                .header(SecurityConstants.AUTH_HEADER, "Bearer " + this.accessToken))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().string(containsString(newNoteContent)));

        Optional<Note> queryNote = noteService.getById(this.savedNote.getId());
        assertTrue(queryNote.isPresent());
        assertEquals(queryNote.get().getNote(), newNoteContent);
    }

    @Test
    void updateTagsByIdShouldSuccess() throws Exception {
        Set<String> newTagsContent = Set.of("java", "python", "spring-boot");
        Note tagToUpdate = SerializationUtils.clone(this.savedNote);
        tagToUpdate.setTags(newTagsContent);
        this.mockMvc
            .perform(post("/note/" + this.savedNote.getId()).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagToUpdate))
                .header(SecurityConstants.AUTH_HEADER, "Bearer " + this.accessToken))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().string(containsString("python")));

        Optional<Note> queryNote = noteService.getById(this.savedNote.getId());
        assertTrue(queryNote.isPresent());
        assertEquals(queryNote.get().getTags(), newTagsContent);
    }

    @Test
    void updateNoteByIdFromNonOwnerShouldFail() throws Exception {

        Pair<SiteUser, String> pair = signupTestUser();

        String newNoteContent = "I am the new note";
        Note noteToUpdate = SerializationUtils.clone(this.savedNote);
        noteToUpdate.setNote(newNoteContent);
        this.mockMvc
            .perform(post("/note/" + this.savedNote.getId()).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(noteToUpdate))
                .header(SecurityConstants.AUTH_HEADER, "Bearer " + pair.getRight()))
            .andExpect(status().isForbidden())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        Optional<Note> queryNote = noteService.getById(this.savedNote.getId());
        assertTrue(queryNote.isPresent());
        assertEquals(queryNote.get().getNote(), this.savedNote.getNote());

        userService.cleanupByUserId(pair.getLeft().getId());
    }

    @Test
    void deleteNoteByIdShouldSuccess() throws Exception {
        NoteDto note = createTmpNote(null);
        MvcResult mvcResult = this.mockMvc
            .perform(post("/note").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(note))
                .header(SecurityConstants.AUTH_HEADER, "Bearer " + this.accessToken))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().string(containsString(note.getText())))
            .andReturn();
        String res = mvcResult.getResponse().getContentAsString();
        Note returnedNote = objectMapper.readValue(res, Note.class);
        assertTrue(noteService.getById(returnedNote.getId()).isPresent());
        this.mockMvc
            .perform(delete("/note/" + returnedNote.getId()).header(SecurityConstants.AUTH_HEADER,
                    "Bearer " + this.accessToken))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        assertFalse(noteService.getById(returnedNote.getId()).isPresent());
    }

    @Test
    void deleteNoteByIdFromNonOwnerShouldFail() throws Exception {
        NoteDto note = createTmpNote(null);
        MvcResult mvcResult = this.mockMvc
            .perform(post("/note").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(note))
                .header(SecurityConstants.AUTH_HEADER, "Bearer " + this.accessToken))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().string(containsString(note.getText())))
            .andReturn();
        String res = mvcResult.getResponse().getContentAsString();
        Note returnedNote = objectMapper.readValue(res, Note.class);
        assertTrue(noteService.getById(returnedNote.getId()).isPresent());

        Pair<SiteUser, String> pair = signupTestUser();

        this.mockMvc
            .perform(delete("/note/" + returnedNote.getId()).header(SecurityConstants.AUTH_HEADER,
                    "Bearer " + pair.getRight()))
            .andExpect(status().isForbidden())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        assertTrue(noteService.getById(returnedNote.getId()).isPresent());
        userService.cleanupByUserId(pair.getLeft().getId());
    }

    @Test
    void getNotes() throws Exception {
        NoteDto note = createTmpNote(null);
        String randKeyword = RandomStringUtils.randomAlphanumeric(10);
        note.setNote(note.getNote() + " " + randKeyword);
        MvcResult mvcResult = this.mockMvc
            .perform(post("/note").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(note))
                .header(SecurityConstants.AUTH_HEADER, "Bearer " + this.accessToken))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().string(containsString(note.getText())))
            .andReturn();
        String res = mvcResult.getResponse().getContentAsString();
        Note returnedNote = objectMapper.readValue(res, Note.class);
        assertTrue(noteService.getById(returnedNote.getId()).isPresent());
        this.mockMvc.perform(get("/notes").header(SecurityConstants.AUTH_HEADER, "Bearer " + this.accessToken))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().string(containsString(note.getText())))
            .andExpect(jsonPath("$", hasSize(equalTo(2))))
            .andReturn();

        // search has result
        this.mockMvc
            .perform(get("/notes?keyword=" + randKeyword).header(SecurityConstants.AUTH_HEADER,
                    "Bearer " + this.accessToken))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().string(containsString(randKeyword)))
            .andExpect(jsonPath("$", hasSize(equalTo(1))))
            .andReturn();

        // search has no result
        this.mockMvc
            .perform(get("/notes?keyword=" + randKeyword + RandomStringUtils.randomAlphanumeric(6))
                .header(SecurityConstants.AUTH_HEADER, "Bearer " + this.accessToken))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(equalTo(0))))
            .andReturn();
    }

    @Test
    void createNote() throws Exception {
        NoteDto note = createTmpNote(null);
        MvcResult mvcResult = this.mockMvc
            .perform(post("/note").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(note))
                .header(SecurityConstants.AUTH_HEADER, "Bearer " + this.accessToken))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().string(containsString(note.getText())))
            .andReturn();
        String res = mvcResult.getResponse().getContentAsString();
        Note returnedNote = objectMapper.readValue(res, Note.class);
        assertEquals(note.getNote(), returnedNote.getNote());
        notesToCleaned.add(returnedNote);
    }

}
