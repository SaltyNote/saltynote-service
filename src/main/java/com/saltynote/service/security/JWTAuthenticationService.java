package com.saltynote.service.security;

import com.saltynote.service.domain.LoginUser;
import com.saltynote.service.domain.transfer.TokenPair;
import com.saltynote.service.domain.transfer.UserCredential;
import com.saltynote.service.service.JwtService;
import com.saltynote.service.service.UserService;
import com.saltynote.service.service.VaultService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Slf4j
@RequiredArgsConstructor
@Service
public class JWTAuthenticationService {

    private final AuthenticationManager authenticationManager;

    private final VaultService vaultService;

    private final JwtService jwtService;

    private final UserService userService;

    public TokenPair authenticate(UserCredential credential, HttpServletRequest request) {

        Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                credential.getUsername(), credential.getPassword(), Collections.emptyList()));

        LoginUser user = (LoginUser) auth.getPrincipal();
        String accessToken = jwtService.createAccessToken(user);
        String refreshToken = vaultService.fetchOrCreateRefreshToken(user);
        // update current user's lastLoginTime, after user logged in successfully
        userService.saveLoginHistory(user.getId(), request.getHeader(SecurityConstants.REAL_IP_HEADER),
                request.getHeader(SecurityConstants.USER_AGENT_HEADER));
        return new TokenPair(accessToken, refreshToken);

    }

}
