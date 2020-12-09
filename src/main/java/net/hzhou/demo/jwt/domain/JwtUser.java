package net.hzhou.demo.jwt.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtUser {
  private Integer id;
  private String username;
}
