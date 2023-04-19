package com.saltynote.service.security;

import com.saltynote.service.domain.LoginUser;
import com.saltynote.service.domain.transfer.JwtToken;
import com.saltynote.service.domain.transfer.UserCredential;
import com.saltynote.service.entity.SiteUser;
import com.saltynote.service.service.JwtService;
import com.saltynote.service.service.UserService;
import com.saltynote.service.service.VaultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Collections;

@Slf4j
@RequiredArgsConstructor
@Service
public class JWTAuthenticationService {

    private final AuthenticationManager authenticationManager;

    private final VaultService vaultService;

    private final JwtService jwtService;

    private final UserService userService;

    public JwtToken authenticate(UserCredential credential) {

        Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                credential.getUsername(), credential.getPassword(), Collections.emptyList()));

        LoginUser user = (LoginUser) auth.getPrincipal();
        String accessToken = jwtService.createAccessToken(user);
        String refreshToken = vaultService.fetchOrCreateRefreshToken(user);
        // update current user's lastLoginTime, after user logged in successfully
        SiteUser curtUser = userService.getByUsername(user.getUsername());
        curtUser.setLastLoginTime(new Timestamp(System.currentTimeMillis()));
        userService.getRepository().save(curtUser);
        return new JwtToken(accessToken, refreshToken);

    }

}
