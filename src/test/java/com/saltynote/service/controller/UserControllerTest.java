package com.saltynote.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.saltynote.service.domain.VaultType;
import com.saltynote.service.domain.transfer.JwtToken;
import com.saltynote.service.domain.transfer.JwtUser;
import com.saltynote.service.domain.transfer.NoteDto;
import com.saltynote.service.domain.transfer.PasswordReset;
import com.saltynote.service.domain.transfer.PasswordUpdate;
import com.saltynote.service.domain.transfer.Payload;
import com.saltynote.service.domain.transfer.UserCredential;
import com.saltynote.service.domain.transfer.UserNewRequest;
import com.saltynote.service.entity.Note;
import com.saltynote.service.entity.SiteUser;
import com.saltynote.service.entity.Vault;
import com.saltynote.service.security.SecurityConstants;
import com.saltynote.service.service.EmailService;
import com.saltynote.service.service.JwtService;
import com.saltynote.service.service.NoteService;
import com.saltynote.service.service.UserService;
import com.saltynote.service.service.VaultService;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Overwrite refresh token ttl to 8 seconds
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = { "jwt.refresh_token.ttl=8000" })
@AutoConfigureMockMvc
@EnableAutoConfiguration(exclude = SecurityAutoConfiguration.class)
@AutoConfigureTestDatabase(replace = NONE)
@Slf4j
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private VaultService vaultService;

    @Autowired
    private NoteService noteService;

    @MockBean
    private EmailService emailService;

    private final Faker faker = new Faker();

    @BeforeEach
    public void setup() throws MessagingException, IOException, TemplateException {
        doNothing().when(emailService).sendAsHtml(any(), any(), any());
        doNothing().when(emailService).send(any(), any(), any());
    }

    @Test
    void emailVerifyTest() throws Exception {
        String username = faker.name().username();
        String emailStr = getEmail(username);
        String alreadyUsedEmail = "example@exmaple.com";

        SiteUser user = new SiteUser().setUsername(faker.name().username()).setEmail(alreadyUsedEmail);
        user.setPassword(bCryptPasswordEncoder.encode(RandomStringUtils.randomAlphanumeric(12)));
        user = userService.save(user);
        assertNotNull(user.getId());

        this.mockMvc
            .perform(post("/email/verification").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new Payload(alreadyUsedEmail))))
            .andExpect(status().isBadRequest());

        this.mockMvc
            .perform(post("/email/verification").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new Payload(emailStr))))
            .andExpect(status().isOk());

        List<Vault> vaults = vaultService.getByEmail(emailStr);
        assertEquals(1, vaults.size());
        vaultService.deleteById(vaults.get(0).getId());
        userService.cleanupByUserId(user.getId());
    }

    @Test
    void signupShouldFailIfNoToken() throws Exception {
        String username = faker.name().username();
        String email = getEmail(username);

        UserNewRequest userNewRequest = new UserNewRequest();

        userNewRequest.setEmail(email);
        userNewRequest.setPassword(RandomStringUtils.randomAlphanumeric(12));
        userNewRequest.setUsername(username);
        assertNull(userNewRequest.getToken());

        this.mockMvc
            .perform(post("/signup").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userNewRequest)))
            .andExpect(status().isInternalServerError());
    }

    @Test
    void signupShouldReturnSuccess() throws Exception {
        String username = faker.name().username();
        String email = getEmail(username);
        Vault vault = vaultService.createForEmail(email, VaultType.NEW_ACCOUNT);

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
            .andDo(print())
            .andExpect(status().isOk());

        SiteUser queryUser = userService.getByUsername(userNewRequest.getUsername());
        assertThat(queryUser).extracting(SiteUser::getEmail).isEqualTo(userNewRequest.getEmail());

        userService.cleanupByUserId(queryUser.getId());
    }

    @Test
    void loginAndRefreshTokenShouldSuccess() throws Exception {

        var uc = new UserCredential().setUsername(faker.name().username())
            .setEmail(faker.internet().emailAddress())
            .setPassword(RandomStringUtils.randomAlphanumeric(12));
        SiteUser user = uc.toSiteUser();
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user = userService.save(user);

        UserCredential userRequest = new UserCredential().setUsername(uc.getUsername()).setPassword(uc.getPassword());
        MvcResult mvcResult = this.mockMvc
            .perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
            .andExpect(status().isOk())
            .andReturn();
        String res = mvcResult.getResponse().getContentAsString();
        var token = objectMapper.readValue(res, JwtToken.class);

        assertNotNull(token);
        assertNotNull(jwtService.parseRefreshToken(token.getRefreshToken()));
        assertNotNull(jwtService.verifyAccessToken(token.getAccessToken()));

        // Note: have to sleep 1 second to have different expire time for new access token
        TimeUnit.SECONDS.sleep(1);

        // try refresh token
        JwtToken tokenRequest = new JwtToken(null, token.getRefreshToken());
        mvcResult = this.mockMvc
            .perform(post("/refresh_token").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tokenRequest)))
            .andExpect(status().isOk())
            .andReturn();

        res = mvcResult.getResponse().getContentAsString();
        JwtToken newToken = objectMapper.readValue(res, JwtToken.class);
        assertNotNull(newToken.getAccessToken());
        assertNotNull(jwtService.verifyAccessToken(newToken.getAccessToken()));
        log.info("old token = {}", token.getAccessToken());
        log.info("new token = {}", newToken.getAccessToken());
        assertNotEquals(token.getAccessToken(), newToken.getAccessToken());
        assertEquals(newToken.getRefreshToken(), token.getRefreshToken());

        userService.cleanupByUserId(user.getId());
    }

    @Test
    void loginAndRefreshTokenReUsageShouldSuccess() throws Exception {

        UserCredential uc = new UserCredential().setUsername(faker.name().username())
            .setEmail(faker.internet().emailAddress())
            .setPassword(RandomStringUtils.randomAlphanumeric(12));
        SiteUser user = uc.toSiteUser();
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user = userService.save(user);

        UserCredential userRequest = new UserCredential().setUsername(uc.getUsername()).setPassword(uc.getPassword());
        MvcResult mvcResult = this.mockMvc
            .perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
            .andExpect(status().isOk())
            .andReturn();
        String res = mvcResult.getResponse().getContentAsString();
        JwtToken token = objectMapper.readValue(res, JwtToken.class);

        assertNotNull(token);
        assertNotNull(jwtService.parseRefreshToken(token.getRefreshToken()));
        assertNotNull(jwtService.verifyAccessToken(token.getAccessToken()));

        String oldRefreshToken = token.getRefreshToken();

        mvcResult = this.mockMvc
            .perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
            .andExpect(status().isOk())
            .andReturn();
        res = mvcResult.getResponse().getContentAsString();
        token = objectMapper.readValue(res, JwtToken.class);

        assertNotNull(token);
        assertNotNull(jwtService.parseRefreshToken(token.getRefreshToken()));
        assertNotNull(jwtService.verifyAccessToken(token.getAccessToken()));
        // No new refresh token is generated.
        assertEquals(oldRefreshToken, token.getRefreshToken());

        // Sleep 2 second, so refresh token will age 20%+, then new refresh token should
        // be generated.
        TimeUnit.SECONDS.sleep(2);

        mvcResult = this.mockMvc
            .perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
            .andExpect(status().isOk())
            .andReturn();
        res = mvcResult.getResponse().getContentAsString();
        token = objectMapper.readValue(res, JwtToken.class);

        assertNotNull(token);
        assertNotNull(jwtService.parseRefreshToken(token.getRefreshToken()));
        assertNotNull(jwtService.verifyAccessToken(token.getAccessToken()));
        // New refresh token is generated.
        assertNotEquals(oldRefreshToken, token.getRefreshToken());

        userService.cleanupByUserId(user.getId());
    }

    @Test
    void loginShouldFail() throws Exception {

        UserCredential uc = new UserCredential().setUsername(faker.name().username())
            .setEmail(faker.internet().emailAddress())
            .setPassword(RandomStringUtils.randomAlphanumeric(12));
        SiteUser user = uc.toSiteUser();
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user = userService.save(user);

        assertNotNull(user.getId());

        UserCredential userRequest = new UserCredential().setUsername(uc.getUsername())
            .setPassword(uc.getPassword() + "not valid");
        this.mockMvc
            .perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
            .andExpect(status().isUnauthorized());

        userService.cleanupByUserId(user.getId());
    }

    @Test
    void passwordResetTest() throws Exception {
        // Create a new User
        UserCredential uc = new UserCredential().setUsername(faker.name().username())
            .setEmail(faker.internet().emailAddress())
            .setPassword(RandomStringUtils.randomAlphanumeric(12));
        SiteUser user = uc.toSiteUser();
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user = userService.save(user);

        // request password change
        Payload payload = new Payload(user.getEmail());
        this.mockMvc
            .perform(post("/password/forget").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isOk());

        List<Vault> vaults = vaultService.getByUserIdAndType(user.getId(), VaultType.PASSWORD);

        assertEquals(1, vaults.size());
        Vault vault = vaults.get(0);

        // Can log in without problem
        UserCredential userRequest = new UserCredential().setUsername(uc.getUsername()).setPassword(uc.getPassword());
        this.mockMvc
            .perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
            .andExpect(status().isOk());

        PasswordReset pr = new PasswordReset();
        pr.setToken(vaultService.encode(vault));
        String newPassword = RandomStringUtils.randomAlphanumeric(10);
        pr.setPassword(newPassword);
        this.mockMvc
            .perform(post("/password/reset").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pr)))
            .andExpect(status().isOk());

        // login with old password should fail
        this.mockMvc
            .perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
            .andExpect(status().isUnauthorized());

        // login with new password should success
        UserCredential ur = new UserCredential().setUsername(uc.getUsername()).setPassword(newPassword);
        this.mockMvc
            .perform(
                    post("/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(ur)))
            .andExpect(status().isOk());

        userService.cleanupByUserId(user.getId());
    }

    @Test
    void passwordUpdateTest() throws Exception {
        String oldPassword = RandomStringUtils.randomAlphanumeric(12);
        String newPassword = RandomStringUtils.randomAlphanumeric(12);

        // Create a new User
        String username = faker.name().username();
        String email = getEmail(username);
        Vault vault = vaultService.createForEmail(email, VaultType.NEW_ACCOUNT);

        assertNotNull(vault.getId());
        assertEquals(vault.getEmail(), email);

        UserNewRequest userNewRequest = new UserNewRequest();

        userNewRequest.setEmail(email);
        userNewRequest.setPassword(oldPassword);
        userNewRequest.setUsername(username);
        userNewRequest.setToken(vault.getSecret());

        MvcResult mvcResult = this.mockMvc
            .perform(post("/signup").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userNewRequest)))
            .andExpect(status().isOk())
            .andReturn();
        String res = mvcResult.getResponse().getContentAsString();
        log.info(res);
        JwtUser jwtUser = objectMapper.readValue(res, JwtUser.class);
        assertNotNull(jwtUser.getId());

        // Can login without problem
        mvcResult = this.mockMvc
            .perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userNewRequest)))
            .andExpect(status().isOk())
            .andReturn();
        res = mvcResult.getResponse().getContentAsString();
        JwtToken token = objectMapper.readValue(res, JwtToken.class);

        assertNotNull(token);
        assertNotNull(jwtService.parseRefreshToken(token.getRefreshToken()));
        assertNotNull(jwtService.verifyAccessToken(token.getAccessToken()));

        PasswordUpdate pu = new PasswordUpdate().setOldPassword(oldPassword).setPassword(newPassword);

        this.mockMvc
            .perform(post("/password").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pu))
                .header(SecurityConstants.HEADER_STRING, "Bearer " + token.getAccessToken()))
            .andExpect(status().isOk());

        // login with old password should fail
        this.mockMvc
            .perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userNewRequest)))
            .andExpect(status().isUnauthorized());

        // login with new password should success
        UserCredential ur = new UserCredential().setUsername(userNewRequest.getUsername()).setPassword(newPassword);
        this.mockMvc
            .perform(
                    post("/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(ur)))
            .andExpect(status().isOk());

        userService.cleanupByUserId(jwtUser.getId());
    }

    @Test
    void accountDeletionTest() throws Exception {
        // Create a new User
        String username = faker.name().username();
        String email = getEmail(username);
        Vault vault = vaultService.createForEmail(email, VaultType.NEW_ACCOUNT);

        assertNotNull(vault.getId());
        assertEquals(vault.getEmail(), email);

        UserNewRequest user = new UserNewRequest();

        user.setEmail(email);
        user.setPassword(RandomStringUtils.randomAlphanumeric(12));
        user.setUsername(username);
        user.setToken(vault.getSecret());

        MvcResult mvcResult = this.mockMvc
            .perform(post("/signup").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
            .andExpect(status().isOk())
            .andReturn();
        String res = mvcResult.getResponse().getContentAsString();
        JwtUser jwtUser = objectMapper.readValue(res, JwtUser.class);
        assertNotNull(jwtUser.getId());

        // Can log in without problem
        mvcResult = this.mockMvc
            .perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
            .andExpect(status().isOk())
            .andReturn();
        res = mvcResult.getResponse().getContentAsString();
        JwtToken token = objectMapper.readValue(res, JwtToken.class);

        assertNotNull(token);
        assertNotNull(jwtService.parseRefreshToken(token.getRefreshToken()));
        assertNotNull(jwtService.verifyAccessToken(token.getAccessToken()));

        NoteDto note = NoteControllerTest.createTmpNote(jwtUser.getId());
        mvcResult = this.mockMvc
            .perform(post("/note").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(note))
                .header(SecurityConstants.HEADER_STRING, "Bearer " + token.getAccessToken()))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().string(containsString(note.getText())))
            .andReturn();
        res = mvcResult.getResponse().getContentAsString();
        Note returnedNote = objectMapper.readValue(res, Note.class);
        assertEquals(note.getNote(), returnedNote.getNote());

        // deletion should fail due to invalid user id
        this.mockMvc
            .perform(delete("/account/invalid-id").header(SecurityConstants.HEADER_STRING,
                    "Bearer " + token.getAccessToken()))
            .andExpect(status().isBadRequest());
        // deletion should fail due to missing user id
        this.mockMvc
            .perform(delete("/account").header(SecurityConstants.HEADER_STRING, "Bearer " + token.getAccessToken()))
            .andExpect(status().isNotFound());

        // deletion should fail due to no access token
        this.mockMvc.perform(delete("/account/" + jwtUser.getId())).andExpect(status().isForbidden());

        // deletion should succeed
        this.mockMvc.perform(delete("/account/" + jwtUser.getId()).header(SecurityConstants.HEADER_STRING,
                "Bearer " + token.getAccessToken()))
            .andExpect(status().isOk());

        assertFalse(userService.getById(jwtUser.getId()).isPresent());
        assertTrue(noteService.getAllByUserId(jwtUser.getId()).isEmpty());
        assertTrue(vaultService.getByUserId(jwtUser.getId()).isEmpty());
    }

    private String getEmail(String username) {
        return username + "@saltynote.com";
    }

}
