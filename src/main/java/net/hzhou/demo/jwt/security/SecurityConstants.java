package net.hzhou.demo.jwt.security;

public class SecurityConstants {
  public static final String SECRET = "Huuu,it is a secret!";
  public static final long EXPIRATION_TIME = 864_000_000; // 10 days
  public static final String TOKEN_PREFIX = "Bearer ";
  public static final String HEADER_STRING = "Authorization";
  public static final String SIGN_UP_URL = "/signup";
  public static final String CLAIM_KEY_USER_ID = "user_id";
}
