package net.hzhou.demo.jwt.utils;

import java.util.Date;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import net.hzhou.demo.jwt.domain.IdentifiableUser;
import net.hzhou.demo.jwt.domain.JwtToken;
import net.hzhou.demo.jwt.domain.JwtUser;
import net.hzhou.demo.jwt.security.SecurityConstants;

public final class JwtUtils {
  private static final JWTVerifier accessTokenVerifier =
      JWT.require(Algorithm.HMAC512(SecurityConstants.SECRET.getBytes())).build();
  private static final JWTVerifier refreshTokenVerifier =
      JWT.require(Algorithm.HMAC512(SecurityConstants.REFRESH_TOKEN_SECRET.getBytes())).build();
  private static final ObjectMapper objectMapper =
      new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

  private JwtUtils() {}

  public static String createAccessToken(IdentifiableUser user) {
    return createToken(user, SecurityConstants.JWT_TOKEN_TTL_IN_SEC, SecurityConstants.SECRET);
  }

  public static String createRefreshToken(IdentifiableUser user) {
    return createToken(
        user, SecurityConstants.REFRESH_TOKEN_TTL_IN_SEC, SecurityConstants.REFRESH_TOKEN_SECRET);
  }

  private static String createToken(IdentifiableUser user, Long tokenTTL, String secret) {
    return createToken(user.getUsername(), user.getId(), tokenTTL, secret);
  }

  private static String createToken(String subject, Integer userId, Long tokenTTL, String secret)
      throws JWTCreationException {
    return JWT.create()
        .withSubject(subject)
        .withClaim(SecurityConstants.CLAIM_KEY_USER_ID, userId)
        .withExpiresAt(new Date(System.currentTimeMillis() + tokenTTL))
        .sign(Algorithm.HMAC512(secret.getBytes()));
  }

  public static DecodedJWT verifyAccessToken(String token) throws JWTVerificationException {
    // This will handle token expiration exception
    return accessTokenVerifier.verify(token.replace(SecurityConstants.TOKEN_PREFIX, ""));
  }

  public static JwtUser parseRefreshToken(String token) throws JWTVerificationException {
    DecodedJWT jwt = refreshTokenVerifier.verify(token);
    return new JwtUser(jwt.getClaim(SecurityConstants.CLAIM_KEY_USER_ID).asInt(), jwt.getSubject());
  }

  public static String tokenToJson(String accessToken, String refreshToken)
      throws JsonProcessingException {
    return objectMapper.writeValueAsString(new JwtToken(accessToken, refreshToken));
  }
}
