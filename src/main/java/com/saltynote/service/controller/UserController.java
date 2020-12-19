package com.saltynote.service.controller;

import java.util.Optional;

import javax.validation.Valid;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.saltynote.service.component.JwtInstance;
import com.saltynote.service.domain.transfer.JwtToken;
import com.saltynote.service.domain.transfer.JwtUser;
import com.saltynote.service.domain.transfer.ServiceResponse;
import com.saltynote.service.domain.transfer.UserCredential;
import com.saltynote.service.entity.RefreshToken;
import com.saltynote.service.entity.SiteUser;
import com.saltynote.service.entity.Vault;
import com.saltynote.service.event.EmailEvent;
import com.saltynote.service.exception.WebClientRuntimeException;
import com.saltynote.service.repository.RefreshTokenRepository;
import com.saltynote.service.repository.UserRepository;
import com.saltynote.service.repository.VaultRepository;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class UserController {

  private final UserRepository userRepository;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final RefreshTokenRepository tokenRepository;
  private final JwtInstance jwtInstance;
  private final ApplicationEventPublisher eventPublisher;
  private final VaultRepository vaultRepository;

  public UserController(
      UserRepository userRepository,
      BCryptPasswordEncoder bCryptPasswordEncoder,
      RefreshTokenRepository tokenRepository,
      JwtInstance jwtInstance,
      ApplicationEventPublisher eventPublisher,
      VaultRepository vaultRepository) {
    this.userRepository = userRepository;
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    this.tokenRepository = tokenRepository;
    this.jwtInstance = jwtInstance;
    this.eventPublisher = eventPublisher;
    this.vaultRepository = vaultRepository;
  }

  @PostMapping("/signup")
  public ResponseEntity<JwtUser> signUp(@Valid @RequestBody UserCredential userCredential) {
    SiteUser user = userCredential.toSiteUser();
    user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
    user = userRepository.save(user);
    if (StringUtils.hasText(user.getId())) {
      eventPublisher.publishEvent(new EmailEvent(this, user, EmailEvent.Type.NEW_USER));
      return ResponseEntity.ok(new JwtUser(user.getId(), user.getUsername()));
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

  @PostMapping("/user/activation/{token}")
  public ResponseEntity<ServiceResponse> userActivation(@PathVariable("token") String token) {
    Optional<Vault> vault = vaultRepository.findBySecret(token);
    // TODO: implementation
    if (vault.isPresent()) {
      Optional<SiteUser> user = userRepository.findById(vault.get().getUserId());
    }
    
    return null;
  }
}
