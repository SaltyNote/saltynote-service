package net.hzhou.note.service.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtUser implements IdentifiableUser {
  private Integer id;
  private String username;
}
