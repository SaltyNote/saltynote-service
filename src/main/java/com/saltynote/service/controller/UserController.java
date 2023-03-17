package com.saltynote.service.controller;

import com.saltynote.service.component.JwtInstance;
import com.saltynote.service.domain.VaultType;
import com.saltynote.service.domain.transfer.Payload;
import com.saltynote.service.domain.transfer.JwtToken;
import com.saltynote.service.domain.transfer.JwtUser;
import com.saltynote.service.domain.transfer.PasswordReset;
import com.saltynote.service.domain.transfer.PasswordUpdate;
import com.saltynote.service.domain.transfer.ServiceResponse;
import com.saltynote.service.domain.transfer.UserNewRequest;
import com.saltynote.service.entity.SiteUser;
import com.saltynote.service.entity.Vault;
import com.saltynote.service.event.EmailEvent;
import com.saltynote.service.exception.WebAppRuntimeException;
import com.saltynote.service.service.UserService;
import com.saltynote.service.service.VaultService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Objects;
import java.util.Optional;

@RestController
@Slf4j
public class UserController {

    @Value("${password.minimal.length}")
    private int passwordMinimalLength;

    @Resource
    private UserService userService;
    @Resource
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Resource
    private JwtInstance jwtInstance;
    @Resource
    private ApplicationEventPublisher eventPublisher;
    @Resource
    private VaultService vaultService;

    @PostMapping("/email/verification")
    public ResponseEntity<ServiceResponse> getVerificationToken(@Valid @RequestBody Payload payload) {
        // check whether this email is already signed up or not.
        if (userService.getRepository().findByEmail(payload.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ServiceResponse(HttpStatus.BAD_REQUEST, "Email is already signed up."));
        }

        SiteUser user = new SiteUser().setEmail(payload.getEmail()).setUsername("there");
        eventPublisher.publishEvent(new EmailEvent(this, user, EmailEvent.Type.NEW_USER));
        return ResponseEntity.ok(
                ServiceResponse.ok("A verification code for signup is sent to you email now"));
    }

    @PostMapping("/signup")
    public ResponseEntity<JwtUser> signup(@Valid @RequestBody UserNewRequest userNewRequest) {
        if (userNewRequest.getPassword().length() < passwordMinimalLength) {
            throw new WebAppRuntimeException(
                    HttpStatus.BAD_REQUEST,
                    "Password should be at least " + passwordMinimalLength + " characters.");
        }
        // Check token
        Optional<Vault> vaultOp =
                vaultService
                        .getRepository()
                        .findByEmailAndSecretAndType(
                                userNewRequest.getEmail(),
                                userNewRequest.getToken(),
                                VaultType.NEW_ACCOUNT.getValue());

        if (vaultOp.isEmpty()) {
            throw new WebAppRuntimeException(
                    HttpStatus.FORBIDDEN, "A valid verification code is required for signup.");
        }
        SiteUser user = userNewRequest.toSiteUser();
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user = userService.getRepository().save(user);
        if (StringUtils.hasText(user.getId())) {
            vaultService.getRepository().delete(vaultOp.get());
            return ResponseEntity.ok(new JwtUser(user.getId(), user.getUsername()));
        } else {
            throw new WebAppRuntimeException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Failed to signup, please try again later.");
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
            throw new WebAppRuntimeException(HttpStatus.BAD_REQUEST, "Invalid refresh token provided!");
        }
    }

    @Transactional
    @DeleteMapping("/refresh_tokens")
    public ResponseEntity<ServiceResponse> cleanRefreshTokens(Authentication auth) {
        JwtUser user = (JwtUser) auth.getPrincipal();
        log.info("[cleanRefreshTokens] user = {}", user);
        vaultService.cleanRefreshTokenByUserId(user.getId());
        return ResponseEntity.ok(ServiceResponse.ok("All your refresh tokens are cleaned."));
    }

    @PostMapping("/password/forget")
    public ResponseEntity<ServiceResponse> forgetPassword(@Valid @RequestBody Payload payload) {
        Optional<SiteUser> usero = userService.getRepository().findByEmail(payload.getEmail());
        if (usero.isEmpty()) {
            log.warn("User is not found for email = {}", payload.getEmail());
            return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED)
                    .body(new ServiceResponse(HttpStatus.PRECONDITION_FAILED, "Invalid email"));
        }

        eventPublisher.publishEvent(new EmailEvent(this, usero.get(), EmailEvent.Type.PASSWORD_FORGET));
        return ResponseEntity.ok(
                ServiceResponse.ok(
                        "Password reset email will be sent to your email, please reset your email with link there."));
    }

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

    @RequestMapping(
            value = "/password",
            method = {RequestMethod.POST, RequestMethod.PUT})
    public ResponseEntity<ServiceResponse> updatePassword(
            @Valid @RequestBody PasswordUpdate passwordUpdate, Authentication auth) {
        JwtUser jwtUser = (JwtUser) auth.getPrincipal();
        // Validate new password
        if (passwordUpdate.getPassword().length() < passwordMinimalLength) {
            throw new WebAppRuntimeException(
                    HttpStatus.BAD_REQUEST,
                    "New password should be at least " + passwordMinimalLength + " characters.");
        }

        // Validate old password
        Optional<SiteUser> usero = userService.getRepository().findById(jwtUser.getId());
        if (usero.isEmpty()) {
            throw new WebAppRuntimeException(
                    HttpStatus.BAD_REQUEST,
                    "Something goes wrong when fetching your info, please try later again.");
        }
        SiteUser user = usero.get();
        if (!bCryptPasswordEncoder.matches(passwordUpdate.getOldPassword(), user.getPassword())) {
            throw new WebAppRuntimeException(
                    HttpStatus.BAD_REQUEST, "Wrong current password is provided.");
        }

        user.setPassword(bCryptPasswordEncoder.encode(passwordUpdate.getPassword()));
        userService.getRepository().save(user);
        return ResponseEntity.ok(ServiceResponse.ok("Password is updated now."));
    }


    @DeleteMapping("/account/{id}")
    public ResponseEntity<ServiceResponse> accountDeletion(
            @PathVariable("id") String userId, Authentication auth) {
        JwtUser jwtUser = (JwtUser) auth.getPrincipal();
        if (!Objects.equals(userId, jwtUser.getId())) {
            throw new WebAppRuntimeException(HttpStatus.BAD_REQUEST, "User information is not confirmed");
        }
        userService.cleanupByUserId(jwtUser.getId());
        return ResponseEntity.ok(ServiceResponse.ok("Account deletion is successful."));
    }

}
