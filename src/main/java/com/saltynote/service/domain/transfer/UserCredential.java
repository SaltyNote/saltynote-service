package com.saltynote.service.domain.transfer;

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.saltynote.service.entity.SiteUser;
import lombok.Data;

@Data
public class UserCredential {
  @NotBlank private String username;
  @NotBlank private String password;
  @NotBlank private String email;

  @JsonIgnore
  public SiteUser toSiteUser() {
    SiteUser user = new SiteUser();
    user.setEmailVerified(false)
        .setEmail(this.email)
        .setUsername(this.username)
        .setPassword(this.password);
    return user;
  }
}
