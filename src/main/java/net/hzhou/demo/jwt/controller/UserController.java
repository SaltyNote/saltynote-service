package net.hzhou.demo.jwt.controller;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import net.hzhou.demo.jwt.domain.JwtToken;
import net.hzhou.demo.jwt.domain.JwtUser;
import net.hzhou.demo.jwt.entity.SiteUser;
import net.hzhou.demo.jwt.repository.UserRepository;
import net.hzhou.demo.jwt.utils.JwtUtils;

@RestController
public class UserController {

  private final UserRepository userRepository;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;

  public UserController(
      UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
    this.userRepository = userRepository;
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
  }

  @PostMapping("/signup")
  public ResponseEntity<JwtUser> signUp(@Valid @RequestBody SiteUser siteUser) {
    siteUser.setPassword(bCryptPasswordEncoder.encode(siteUser.getPassword()));
    siteUser = userRepository.save(siteUser);
    if (siteUser.getId() > 0) {
      return ResponseEntity.ok(new JwtUser(siteUser.getId(), siteUser.getUsername()));
    } else {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @PostMapping("/refresh_token")
  public JwtToken refreshToken(@Valid @RequestBody JwtToken jwtToken) {
    JwtUser user = JwtUtils.parseRefreshToken(jwtToken.getRefreshToken());
    String newToken = JwtUtils.createAccessToken(user);
    return new JwtToken(newToken, null);
  }
}
