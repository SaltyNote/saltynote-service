package net.hzhou.note.service.domain;

import java.util.Collections;

import org.springframework.security.core.userdetails.User;

import lombok.Getter;
import net.hzhou.note.service.entity.SiteUser;

public class LoginUser extends User implements IdentifiableUser {
  @Getter private final Integer id;

  public LoginUser(SiteUser user) {
    super(user.getUsername(), user.getPassword(), Collections.emptyList());
    this.id = user.getId();
  }
}
