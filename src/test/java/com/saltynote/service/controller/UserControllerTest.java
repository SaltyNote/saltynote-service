package com.saltynote.service.controller;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.saltynote.service.domain.transfer.UserCredential;
import com.saltynote.service.entity.SiteUser;
import com.saltynote.service.service.EmailService;
import com.saltynote.service.service.UserService;
import lombok.extern.slf4j.Slf4j;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@EnableAutoConfiguration(exclude = SecurityAutoConfiguration.class)
@AutoConfigureTestDatabase(replace = NONE)
@Slf4j
public class UserControllerTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private UserService userService;
  @Autowired private BCryptPasswordEncoder bCryptPasswordEncoder;
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
  public void loginShouldSuccess() throws Exception {

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
    this.mockMvc
        .perform(
            post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
        .andDo(print())
        .andExpect(status().isOk());

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
        .andDo(print())
        .andExpect(status().isUnauthorized());

    userService.cleanupByUserId(user.getId());
  }
}
