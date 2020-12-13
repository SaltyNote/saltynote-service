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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.saltynote.service.domain.transfer.UserCredential;
import com.saltynote.service.entity.SiteUser;
import com.saltynote.service.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

import static org.assertj.core.api.Assertions.assertThat;
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
  @Autowired private UserRepository userRepository;

  private final Faker faker = new Faker();

  @After
  public void cleanup() {
    log.info("clean up...");
  }

  @Test
  public void signupShouldReturnSuccess() throws Exception {
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

    SiteUser queryUser = userRepository.findByUsername(user.getUsername());
    assertThat(queryUser).extracting(SiteUser::getEmail).isEqualTo(user.getEmail());

    userRepository.deleteById(queryUser.getId());
  }
}
