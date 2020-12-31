package com.saltynote.service.repository;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.javafaker.Faker;
import com.saltynote.service.entity.SiteUser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
public class UserRepositoryIntegrationTest {

  @Autowired private TestEntityManager entityManager;
  @Autowired private UserRepository userRepository;
  @Autowired private BCryptPasswordEncoder bCryptPasswordEncoder;

  private final Faker faker = new Faker();

  @Test
  public void whenFindByNameThenReturnUser() {
    // given
    String username = faker.name().username();
    SiteUser user =
        new SiteUser()
            .setEmail(faker.internet().emailAddress())
            .setUsername(username)
            .setPassword(bCryptPasswordEncoder.encode(RandomStringUtils.randomAlphanumeric(12)))
            .setEmailVerified(false);
    entityManager.persist(user);
    entityManager.flush();

    // when
    SiteUser found = userRepository.findByUsername(username);
    // then
    assertThat(found.getEmail()).isEqualTo(user.getEmail());
  }
}
