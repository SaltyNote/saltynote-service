package net.hzhou.note.service.component;

import java.util.Date;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.hzhou.note.service.domain.IdentifiableUser;
import net.hzhou.note.service.domain.JwtToken;
import net.hzhou.note.service.domain.JwtUser;
import net.hzhou.note.service.security.SecurityConstants;

@Component
@Slf4j
public class JwtInstance {

  @Value("${jwt.access_token.secret}")
  private String accessTokenSecret;

  @Value("${jwt.access_token.ttl}")
  private long accessTokenTtl;

  @Value("${jwt.refresh_token.secret}")
  private String refreshTokenSecret;

  @Value("${jwt.refresh_token.ttl}")
  private long refreshTokenTtl;

  private final ObjectMapper objectMapper;

  private JWTVerifier accessTokenVerifier, refreshTokenVerifier;

  public JwtInstance(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @PostConstruct
  public void init() {
    accessTokenVerifier = JWT.require(Algorithm.HMAC512(accessTokenSecret.getBytes())).build();
    refreshTokenVerifier = JWT.require(Algorithm.HMAC512(refreshTokenSecret.getBytes())).build();
  }

  public String createAccessToken(IdentifiableUser user) {
    return createToken(user, accessTokenTtl, accessTokenSecret);
  }

  public String createRefreshToken(IdentifiableUser user) {
    return createToken(user, refreshTokenTtl, refreshTokenSecret);
  }

  private String createToken(IdentifiableUser user, Long tokenTTL, String secret) {
    return createToken(user.getUsername(), user.getId(), tokenTTL, secret);
  }

  private String createToken(String subject, Integer userId, Long tokenTTL, String secret)
      throws JWTCreationException {
    return JWT.create()
        .withSubject(subject)
        .withClaim(SecurityConstants.CLAIM_KEY_USER_ID, userId)
        .withExpiresAt(new Date(System.currentTimeMillis() + tokenTTL))
        .sign(Algorithm.HMAC512(secret.getBytes()));
  }

  public DecodedJWT verifyAccessToken(String token) throws JWTVerificationException {
    // This will handle token expiration exception
    return accessTokenVerifier.verify(token.replace(SecurityConstants.TOKEN_PREFIX, ""));
  }

  public JwtUser parseRefreshToken(String token) throws JWTVerificationException {
    DecodedJWT jwt = refreshTokenVerifier.verify(token);
    return new JwtUser(jwt.getClaim(SecurityConstants.CLAIM_KEY_USER_ID).asInt(), jwt.getSubject());
  }

  public String tokenToJson(String accessToken, String refreshToken)
      throws JsonProcessingException {
    return objectMapper.writeValueAsString(new JwtToken(accessToken, refreshToken));
  }
}
