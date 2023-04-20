package com.saltynote.service.security;

import com.saltynote.service.exception.IllegalInitialException;

public final class SecurityConstants {

    public static final String TOKEN_PREFIX = "Bearer ";

    public static final String AUTH_HEADER = "Authorization";

    public static final String USER_AGENT_HEADER = "User-Agent";

    public static final String REAL_IP_HEADER = "X-Real-IP";

    public static final String SALTY_PORT_HEADER = "X-SaltyNote-Port";

    public static final String SIGN_UP_URL = "/signup";

    public static final String CLAIM_KEY_USER_ID = "user_id";

    private SecurityConstants() {
        throw new IllegalInitialException("Do not instantiate me.");
    }

}
