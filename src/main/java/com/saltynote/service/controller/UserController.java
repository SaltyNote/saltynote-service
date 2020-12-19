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
import com.saltynote.service.domain.VaultEntity;
import com.saltynote.service.domain.VaultType;
import com.saltynote.service.domain.transfer.JwtToken;
import com.saltynote.service.domain.transfer.JwtUser;
import com.saltynote.service.domain.transfer.ServiceResponse;
import com.saltynote.service.domain.transfer.UserCredential;
import com.saltynote.service.entity.SiteUser;
import com.saltynote.service.entity.Vault;
import com.saltynote.service.event.EmailEvent;
import com.saltynote.service.exception.WebClientRuntimeException;
import com.saltynote.service.service.UserService;
import com.saltynote.service.service.VaultService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@RestController
@Slf4j
public class UserController {

  private final UserService userService;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final JwtInstance jwtInstance;
  private final ApplicationEventPublisher eventPublisher;
  private final VaultService vaultService;

  public UserController(
      BCryptPasswordEncoder bCryptPasswordEncoder,
      JwtInstance jwtInstance,
      ApplicationEventPublisher eventPublisher,
      VaultService vaultService,
      UserService userService) {
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    this.jwtInstance = jwtInstance;
    this.eventPublisher = eventPublisher;
    this.vaultService = vaultService;
    this.userService = userService;
  }

  @PostMapping("/signup")
  public ResponseEntity<JwtUser> signUp(@Valid @RequestBody UserCredential userCredential) {
    SiteUser user = userCredential.toSiteUser();
    user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
    user = userService.getRepository().save(user);
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
    Optional<Vault> token =
        vaultService.findByUserIdAndTypeAndValue(
            user.getId(), VaultType.REFRESH_TOKEN, jwtToken.getRefreshToken());
    if (token.isPresent()) {
      String newToken = jwtInstance.createAccessToken(user);
      return ResponseEntity.ok(new JwtToken(newToken, jwtToken.getRefreshToken()));
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
    vaultService.cleanRefreshTokenByUserId(user.getId());
    return ResponseEntity.ok().build();
  }

  @PostMapping("/email/verification/{token}")
  public ResponseEntity<ServiceResponse> userActivation(@PathVariable("token") String token) {
    val wre = new WebClientRuntimeException(HttpStatus.BAD_REQUEST, "Invalid token provided.");
    Optional<VaultEntity> veo = vaultService.decode(token);
    if (veo.isEmpty()) {
      throw wre;
    }
    VaultEntity ve = veo.get();
    Optional<Vault> vault = vaultService.getRepository().findBySecret(ve.getSecret());
    if (vault.isPresent()) {
      if (!vault.get().getUserId().equals(ve.getUserId())) {
        log.error(
            "User id are not match from decoded token {} and database {}",
            ve.getUserId(),
            vault.get().getUserId());
        throw wre;
      }
    }
    Optional<SiteUser> usero = userService.getRepository().findById(ve.getUserId());
    if (usero.isEmpty()) {
      log.error("User is not found for user id = {}", ve.getUserId());
      throw wre;
    }

    SiteUser user = usero.get();
    if (user.getEmailVerified()) {
      return ResponseEntity.ok(ServiceResponse.ok("Email is already verified"));
    }
    if (vault.isPresent()) {
      user.setEmailVerified(true);
      userService.getRepository().save(user);
      vaultService.getRepository().delete(vault.get());
      return ResponseEntity.ok(ServiceResponse.ok("Email is verified now"));
    } else {
      throw wre;
    }
  }
}
