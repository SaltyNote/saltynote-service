package com.saltynote.service.controller;

import java.util.Optional;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.saltynote.service.component.JwtInstance;
import com.saltynote.service.domain.VaultEntity;
import com.saltynote.service.domain.VaultType;
import com.saltynote.service.domain.transfer.Email;
import com.saltynote.service.domain.transfer.JwtToken;
import com.saltynote.service.domain.transfer.JwtUser;
import com.saltynote.service.domain.transfer.PasswordReset;
import com.saltynote.service.domain.transfer.ServiceResponse;
import com.saltynote.service.domain.transfer.UserCredential;
import com.saltynote.service.entity.SiteUser;
import com.saltynote.service.entity.Vault;
import com.saltynote.service.event.EmailEvent;
import com.saltynote.service.exception.WebAppRuntimeException;
import com.saltynote.service.service.UserService;
import com.saltynote.service.service.VaultService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@RestController
@Slf4j
@Api(
    value = "User Endpoint",
    description = "Everything about User operation, e.g. login, signup, etc")
public class UserController {

  @Value("${password.minimal.length}")
  private int passwordMinimalLength;

  @Resource private UserService userService;
  @Resource private BCryptPasswordEncoder bCryptPasswordEncoder;
  @Resource private JwtInstance jwtInstance;
  @Resource private ApplicationEventPublisher eventPublisher;
  @Resource private VaultService vaultService;

  @ApiOperation(value = "Create a new user with email, username and password")
  @PostMapping("/signup")
  public ResponseEntity<JwtUser> signup(@Valid @RequestBody UserCredential userCredential) {
    if (userCredential.getPassword().length() < passwordMinimalLength) {
      throw new WebAppRuntimeException(
          HttpStatus.BAD_REQUEST,
          "Password should be at least " + passwordMinimalLength + " characters.");
    }
    SiteUser user = userCredential.toSiteUser();
    user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
    user = userService.getRepository().save(user);
    if (StringUtils.hasText(user.getId())) {
      eventPublisher.publishEvent(new EmailEvent(this, user, EmailEvent.Type.NEW_USER));
      return ResponseEntity.ok(new JwtUser(user.getId(), user.getUsername()));
    } else {
      throw new WebAppRuntimeException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Failed to signup, please try again later.");
    }
  }

  @PostMapping("/refresh_token")
  @ApiOperation(value = "Get a new access token with refresh_token.")
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
      throw new WebAppRuntimeException(HttpStatus.BAD_REQUEST, "Invalid refresh token provided!");
    }
  }

  @Transactional
  @ApiOperation(
      value =
          "Clean all your refresh tokens, so no one can use any of them to refresh and obtain access token")
  @DeleteMapping("/refresh_tokens")
  public ResponseEntity<Void> cleanRefreshTokens(Authentication auth) {
    JwtUser user = (JwtUser) auth.getPrincipal();
    log.info("[cleanRefreshTokens] user = {}", user);
    vaultService.cleanRefreshTokenByUserId(user.getId());
    return ResponseEntity.ok().build();
  }

  @ApiOperation(
      value =
          "Email verification. Once signup, your email will receive a verification message, there you can find this link to verify your email")
  @GetMapping("/email/verification/{token}")
  public ResponseEntity<ServiceResponse> userActivation(@PathVariable("token") String token) {
    val wre = new WebAppRuntimeException(HttpStatus.BAD_REQUEST, "Invalid token provided.");
    Optional<VaultEntity> veo = vaultService.decode(token);
    if (veo.isEmpty()) {
      throw wre;
    }
    VaultEntity ve = veo.get();
    Optional<Vault> vault = vaultService.getRepository().findBySecret(ve.getSecret());
    if (vault.isPresent() && !vault.get().getUserId().equals(ve.getUserId())) {
      log.error(
          "User id are not match from decoded token {} and database {}",
          ve.getUserId(),
          vault.get().getUserId());
      throw wre;
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

  @ApiOperation(value = "Request password reset email")
  @PostMapping("/password/forget")
  public ResponseEntity<ServiceResponse> forgetPassword(@Valid @RequestBody Email email) {
    Optional<SiteUser> usero = userService.getRepository().findByEmail(email.getEmail());
    if (usero.isEmpty()) {
      log.warn("User is not found for email = {}", email.getEmail());
      return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED)
          .body(new ServiceResponse(HttpStatus.PRECONDITION_FAILED, "Invalid email"));
    }

    eventPublisher.publishEvent(new EmailEvent(this, usero.get(), EmailEvent.Type.PASSWORD_FORGET));
    return ResponseEntity.ok(
        ServiceResponse.ok(
            "Password reset email will be sent to your email, please reset your email with link there."));
  }

  @ApiOperation(value = "Reset Password")
  @PostMapping("/password/reset")
  public ResponseEntity<ServiceResponse> resetPassword(
      @Valid @RequestBody PasswordReset passwordReset) {
    val wre = new WebAppRuntimeException(HttpStatus.BAD_REQUEST, "Invalid payload provided.");
    if (passwordReset.getPassword().length() < passwordMinimalLength) {
      throw new WebAppRuntimeException(
          HttpStatus.BAD_REQUEST,
          "Password should be at least " + passwordMinimalLength + " characters.");
    }
    Optional<Vault> vo = vaultService.findByToken(passwordReset.getToken());
    if (vo.isEmpty()) {
      throw wre;
    }

    Optional<SiteUser> usero = userService.getRepository().findById(vo.get().getUserId());
    if (usero.isPresent()) {
      SiteUser user = usero.get();
      user.setPassword(bCryptPasswordEncoder.encode(passwordReset.getPassword()));
      userService.getRepository().save(user);
      vaultService.getRepository().delete(vo.get());
      return ResponseEntity.ok(ServiceResponse.ok("Password has been reset!"));
    } else {
      throw wre;
    }
  }

  // Note: this is not a valid endpoint, it is only used for swagger doc.
  @ApiOperation(
      value =
          "Please user '/login' instead, as login is managed by spring security. Here is just a placeholder for swagger doc")
  @PostMapping("/login-placeholder-for-swagger-doc")
  public ResponseEntity<JwtUser> login(@Valid @RequestBody UserCredential userCredential) {
    return ResponseEntity.ok(new JwtUser("swagger-ui-user-id", "swagger-ui-username"));
  }
}
