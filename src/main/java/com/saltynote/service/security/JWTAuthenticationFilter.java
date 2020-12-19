package com.saltynote.service.security;

import java.io.IOException;
import java.util.Collections;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saltynote.service.component.JwtInstance;
import com.saltynote.service.domain.LoginUser;
import com.saltynote.service.domain.transfer.UserCredential;
import com.saltynote.service.service.VaultService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
  private final AuthenticationManager authenticationManager;
  private final VaultService vaultService;
  private final JwtInstance jwtInstance;

  public JWTAuthenticationFilter(
      AuthenticationManager authenticationManager,
      VaultService vaultService,
      JwtInstance jwtInstance) {
    this.authenticationManager = authenticationManager;
    this.vaultService = vaultService;
    this.jwtInstance = jwtInstance;
  }

  @Override
  public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res)
      throws AuthenticationException {
    try {
      UserCredential user =
          new ObjectMapper().readValue(req.getInputStream(), UserCredential.class);

      return authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              user.getUsername(), user.getPassword(), Collections.emptyList()));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void successfulAuthentication(
      HttpServletRequest req, HttpServletResponse res, FilterChain chain, Authentication auth)
      throws IOException {

    LoginUser user = (LoginUser) auth.getPrincipal();
    String accessToken = jwtInstance.createAccessToken(user);
    String refreshToken = vaultService.createRefreshToken(user);
    res.addHeader(SecurityConstants.HEADER_STRING, SecurityConstants.TOKEN_PREFIX + accessToken);
    res.setContentType(MediaType.APPLICATION_JSON_VALUE);
    res.getWriter().write(jwtInstance.tokenToJson(accessToken, refreshToken));
  }
}
