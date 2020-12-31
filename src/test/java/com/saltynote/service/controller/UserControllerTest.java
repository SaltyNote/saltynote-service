package com.saltynote.service.controller;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.saltynote.service.component.JwtInstance;
import com.saltynote.service.domain.VaultType;
import com.saltynote.service.domain.transfer.JwtToken;
import com.saltynote.service.domain.transfer.JwtUser;
import com.saltynote.service.domain.transfer.UserCredential;
import com.saltynote.service.entity.SiteUser;
import com.saltynote.service.entity.Vault;
import com.saltynote.service.service.EmailService;
import com.saltynote.service.service.UserService;
import com.saltynote.service.service.VaultService;
import lombok.extern.slf4j.Slf4j;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Overwrite refresh token ttl to 8 seconds
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"jwt.refresh_token.ttl=8000"})
@AutoConfigureMockMvc
@EnableAutoConfiguration(exclude = SecurityAutoConfiguration.class)
@AutoConfigureTestDatabase(replace = NONE)
@Slf4j
public class UserControllerTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private UserService userService;
  @Autowired private BCryptPasswordEncoder bCryptPasswordEncoder;
  @Autowired private JwtInstance jwtInstance;
  @Autowired private VaultService vaultService;
  @MockBean private EmailService emailService;

  private final Faker faker = new Faker();

  @After
  public void cleanup() {
    log.info("clean up...");
  }

  @Test
  public void signupShouldReturnSuccess() throws Exception {

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

    SiteUser queryUser = userService.getRepository().findByUsername(user.getUsername());
    assertThat(queryUser).extracting(SiteUser::getEmail).isEqualTo(user.getEmail());

    userService.cleanupByUserId(queryUser.getId());
  }

  @Test
  public void loginAndRefreshTokenShouldSuccess() throws Exception {

    UserCredential uc =
        new UserCredential()
            .setUsername(faker.name().username())
            .setEmail(faker.internet().emailAddress())
            .setPassword(RandomStringUtils.randomAlphanumeric(12));
    SiteUser user = uc.toSiteUser();
    user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
    user = userService.getRepository().save(user);

    UserCredential userRequest =
        new UserCredential().setUsername(uc.getUsername()).setPassword(uc.getPassword());
    MvcResult mvcResult =
        this.mockMvc
            .perform(
                post("/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userRequest)))
            .andExpect(status().isOk())
            .andReturn();
    String res = mvcResult.getResponse().getContentAsString();
    JwtToken token = objectMapper.readValue(res, JwtToken.class);

    assertNotNull(token);
    assertNotNull(jwtInstance.parseRefreshToken(token.getRefreshToken()));
    assertNotNull(jwtInstance.verifyAccessToken(token.getAccessToken()));

    // Note: have to sleep 1 second to have different expire time for new access token
    TimeUnit.SECONDS.sleep(1);

    // try refresh token
    JwtToken tokenRequest = new JwtToken(null, token.getRefreshToken());
    mvcResult =
        this.mockMvc
            .perform(
                post("/refresh_token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(tokenRequest)))
            .andExpect(status().isOk())
            .andReturn();

    res = mvcResult.getResponse().getContentAsString();
    JwtToken newToken = objectMapper.readValue(res, JwtToken.class);
    assertNotNull(newToken.getAccessToken());
    assertNotNull(jwtInstance.verifyAccessToken(newToken.getAccessToken()));
    log.info("old token = {}", token.getAccessToken());
    log.info("new token = {}", newToken.getAccessToken());
    assertNotEquals(token.getAccessToken(), newToken.getAccessToken());
    assertEquals(newToken.getRefreshToken(), token.getRefreshToken());

    userService.cleanupByUserId(user.getId());
  }

  @Test
  public void loginAndRefreshTokenReUsageShouldSuccess() throws Exception {

    UserCredential uc =
        new UserCredential()
            .setUsername(faker.name().username())
            .setEmail(faker.internet().emailAddress())
            .setPassword(RandomStringUtils.randomAlphanumeric(12));
    SiteUser user = uc.toSiteUser();
    user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
    user = userService.getRepository().save(user);

    UserCredential userRequest =
        new UserCredential().setUsername(uc.getUsername()).setPassword(uc.getPassword());
    MvcResult mvcResult =
        this.mockMvc
            .perform(
                post("/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userRequest)))
            .andExpect(status().isOk())
            .andReturn();
    String res = mvcResult.getResponse().getContentAsString();
    JwtToken token = objectMapper.readValue(res, JwtToken.class);

    assertNotNull(token);
    assertNotNull(jwtInstance.parseRefreshToken(token.getRefreshToken()));
    assertNotNull(jwtInstance.verifyAccessToken(token.getAccessToken()));

    String oldRefreshToken = token.getRefreshToken();

    mvcResult =
        this.mockMvc
            .perform(
                post("/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userRequest)))
            .andExpect(status().isOk())
            .andReturn();
    res = mvcResult.getResponse().getContentAsString();
    token = objectMapper.readValue(res, JwtToken.class);

    assertNotNull(token);
    assertNotNull(jwtInstance.parseRefreshToken(token.getRefreshToken()));
    assertNotNull(jwtInstance.verifyAccessToken(token.getAccessToken()));
    // No new refresh token is generated.
    assertEquals(oldRefreshToken, token.getRefreshToken());

    // Sleep 2 second, so refresh token will age 20%+, then new refresh token should be generated.
    TimeUnit.SECONDS.sleep(2);

    mvcResult =
        this.mockMvc
            .perform(
                post("/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userRequest)))
            .andExpect(status().isOk())
            .andReturn();
    res = mvcResult.getResponse().getContentAsString();
    token = objectMapper.readValue(res, JwtToken.class);

    assertNotNull(token);
    assertNotNull(jwtInstance.parseRefreshToken(token.getRefreshToken()));
    assertNotNull(jwtInstance.verifyAccessToken(token.getAccessToken()));
    // New refresh token is generated.
    assertNotEquals(oldRefreshToken, token.getRefreshToken());

    userService.cleanupByUserId(user.getId());
  }

  @Test
  public void loginShouldFail() throws Exception {

    UserCredential uc =
        new UserCredential()
            .setUsername(faker.name().username())
            .setEmail(faker.internet().emailAddress())
            .setPassword(RandomStringUtils.randomAlphanumeric(12));
    SiteUser user = uc.toSiteUser();
    user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
    user = userService.getRepository().save(user);

    UserCredential userRequest =
        new UserCredential()
            .setUsername(uc.getUsername())
            .setPassword(uc.getPassword() + "not valid");
    this.mockMvc
        .perform(
            post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
        .andExpect(status().isUnauthorized());

    userService.cleanupByUserId(user.getId());
  }

  @Test
  public void emailVerificationShouldSuccess() throws Exception {
    doNothing().when(emailService).sendAsHtml(any(), any(), any());
    doNothing().when(emailService).send(any(), any(), any());

    UserCredential user = new UserCredential();
    String username = faker.name().username();
    user.setEmail(username + "@saltynote.com");
    user.setPassword(RandomStringUtils.randomAlphanumeric(12));
    user.setUsername(username);

    MvcResult mvcResult =
        this.mockMvc
            .perform(
                post("/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(user)))
            .andExpect(status().isOk())
            .andReturn();

    String res = mvcResult.getResponse().getContentAsString();
    JwtUser jwtUser = objectMapper.readValue(res, JwtUser.class);

    assertThat(jwtUser).extracting(JwtUser::getId).isNotNull();
    List<Vault> vaults =
        vaultService
            .getRepository()
            .findByUserIdAndType(jwtUser.getId(), VaultType.NEW_ACCOUNT.getValue());
    assertEquals(vaults.size(), 1);

    assertFalse(userService.getRepository().findById(jwtUser.getId()).get().getEmailVerified());
    String token = vaultService.encode(vaults.get(0));
    this.mockMvc.perform(get("/email/verification/" + token)).andExpect(status().isOk());

    assertTrue(userService.getRepository().findById(jwtUser.getId()).get().getEmailVerified());

    userService.cleanupByUserId(jwtUser.getId());
  }
}
