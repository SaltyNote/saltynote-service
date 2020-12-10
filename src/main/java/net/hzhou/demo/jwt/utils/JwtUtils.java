package net.hzhou.demo.jwt.utils;

import java.util.Date;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import net.hzhou.demo.jwt.domain.JwtUser;
import net.hzhou.demo.jwt.security.SecurityConstants;

public final class JwtUtils {
  private static final JWTVerifier verifier =
      JWT.require(Algorithm.HMAC512(SecurityConstants.SECRET.getBytes())).build();

  private JwtUtils() {}

  public static String createToken(String subject, Integer userId, Long tokenTTL)
      throws JWTCreationException {
    return JWT.create()
        .withSubject(subject)
        .withClaim(SecurityConstants.CLAIM_KEY_USER_ID, userId)
        .withExpiresAt(new Date(System.currentTimeMillis() + tokenTTL))
        .sign(Algorithm.HMAC512(SecurityConstants.SECRET.getBytes()));
  }

  public static DecodedJWT verifyToken(String token) throws JWTVerificationException {
    // This will handle token expiration exception
    return verifier.verify(token);
  }

  public static JwtUser parseToken(String token) throws JWTVerificationException {
    DecodedJWT jwt = verifier.verify(token);
    return new JwtUser(jwt.getClaim(SecurityConstants.CLAIM_KEY_USER_ID).asInt(), jwt.getSubject());
  }
}
