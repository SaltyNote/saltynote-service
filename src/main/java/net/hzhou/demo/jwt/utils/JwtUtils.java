package net.hzhou.demo.jwt.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;

public class JwtUtils {

  private static final long EXPIRE_TIME = 15 * 60 * 1000; // 15 minutes

  public static String sign(String username, Integer userId, String password) {

    Date date = new Date(System.currentTimeMillis() + EXPIRE_TIME);
    Algorithm algorithm = Algorithm.HMAC256(password);
    Map<String, Object> header = new HashMap<>(2);
    header.put("typ", "JWT");
    header.put("alg", "HS256");

    return JWT.create()
        .withHeader(header)
        .withClaim("userId", userId)
        .withClaim("username", username)
        .withExpiresAt(date)
        .sign(algorithm);
  }

  public static boolean verity(String token, String password) {
    try {
      Algorithm algorithm = Algorithm.HMAC256(password);
      JWTVerifier verifier = JWT.require(algorithm).build();
      verifier.verify(token);
      return true;
    } catch (IllegalArgumentException | JWTVerificationException e) {
      return false;
    }
  }
}
