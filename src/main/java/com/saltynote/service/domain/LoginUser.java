package com.saltynote.service.domain;

import java.util.Collections;

import org.springframework.security.core.userdetails.User;

import com.saltynote.service.entity.SiteUser;
import lombok.Getter;

public class LoginUser extends User implements IdentifiableUser {
  @Getter private final String id;

  public LoginUser(SiteUser user) {
    super(user.getUsername(), user.getPassword(), Collections.emptyList());
    this.id = user.getId();
  }
}
