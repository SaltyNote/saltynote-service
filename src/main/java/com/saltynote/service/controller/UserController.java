package com.saltynote.service.controller;

import java.util.Optional;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.saltynote.service.component.JwtInstance;
import com.saltynote.service.domain.transfer.JwtToken;
import com.saltynote.service.domain.transfer.JwtUser;
import com.saltynote.service.domain.transfer.UserCredential;
import com.saltynote.service.entity.RefreshToken;
import com.saltynote.service.entity.SiteUser;
import com.saltynote.service.exception.WebClientRuntimeException;
import com.saltynote.service.repository.RefreshTokenRepository;
import com.saltynote.service.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class UserController {

  private final UserRepository userRepository;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final RefreshTokenRepository tokenRepository;
  private final JwtInstance jwtInstance;

  public UserController(
      UserRepository userRepository,
      BCryptPasswordEncoder bCryptPasswordEncoder,
      RefreshTokenRepository tokenRepository,
      JwtInstance jwtInstance) {
    this.userRepository = userRepository;
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    this.tokenRepository = tokenRepository;
    this.jwtInstance = jwtInstance;
  }

  @PostMapping("/signup")
  public ResponseEntity<JwtUser> signUp(@Valid @RequestBody UserCredential userCredential) {
    SiteUser siteUser = userCredential.toSiteUser();
    siteUser.setPassword(bCryptPasswordEncoder.encode(siteUser.getPassword()));
    siteUser = userRepository.save(siteUser);
    if (siteUser.getId() > 0) {
      return ResponseEntity.ok(new JwtUser(siteUser.getId(), siteUser.getUsername()));
    } else {
      throw new RuntimeException("Failed to signup, please try again later.");
    }
  }

  @PostMapping("/refresh_token")
  public ResponseEntity<JwtToken> refreshToken(@Valid @RequestBody JwtToken jwtToken) {
    // 1. No expiry, and valid.
    JwtUser user = jwtInstance.parseRefreshToken(jwtToken.getRefreshToken());
    // 2. Not deleted from database.
    Optional<RefreshToken> token =
        tokenRepository.findByUserIdAndRefreshToken(user.getId(), jwtToken.getRefreshToken());
    if (token.isPresent()) {
      String newToken = jwtInstance.createAccessToken(user);
      return ResponseEntity.ok(new JwtToken(newToken, null));
    } else {
      throw new WebClientRuntimeException(
          HttpStatus.BAD_REQUEST, "Invalid refresh token provided!");
    }
  }

  @Transactional
  @DeleteMapping("/refresh_tokens")
  public ResponseEntity<Void> cleanRefreshTokens(Authentication auth) {
    JwtUser user = (JwtUser) auth.getPrincipal();
    log.info("[cleanRefreshTokens] user = {}", user);
    tokenRepository.deleteAllByUserId(user.getId());
    return ResponseEntity.ok().build();
  }
}
