package com.saltynote.service.security;

import com.saltynote.service.exception.IllegalInitialException;

public class SecurityConstants {

    private SecurityConstants() {
        throw new IllegalInitialException("Do not instantiate me.");
    }

    public static final String TOKEN_PREFIX = "Bearer ";

    public static final String HEADER_STRING = "Authorization";

    public static final String SIGN_UP_URL = "/signup";

    public static final String CLAIM_KEY_USER_ID = "user_id";

}
