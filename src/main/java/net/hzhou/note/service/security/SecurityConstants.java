package net.hzhou.note.service.security;

public class SecurityConstants {
  public static final String SECRET = "emmm, it is a strong secret!";
  public static final String REFRESH_TOKEN_SECRET = "OK!Ineedtocomeupwithavalidrefreshtokensecret";
  public static final long JWT_TOKEN_TTL_IN_SEC = 60 * 1000; // 1 minute
  public static final long REFRESH_TOKEN_TTL_IN_SEC = 60 * 60 * 1000; // 1 hour
  public static final String TOKEN_PREFIX = "Bearer ";
  public static final String HEADER_STRING = "Authorization";
  public static final String SIGN_UP_URL = "/signup";
  public static final String CLAIM_KEY_USER_ID = "user_id";
}
