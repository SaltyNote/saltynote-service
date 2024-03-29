package com.saltynote.service.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saltynote.service.domain.IdentifiableUser;
import com.saltynote.service.domain.transfer.JwtUser;
import com.saltynote.service.domain.transfer.TokenPair;
import com.saltynote.service.security.SecurityConstants;
import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.access_token.secret}")
    private String accessTokenSecret;

    @Value("${jwt.access_token.ttl}")
    private long accessTokenTtl;

    @Value("${jwt.refresh_token.secret}")
    private String refreshTokenSecret;

    @Value("${jwt.refresh_token.ttl}")
    private long refreshTokenTtl;

    private final ObjectMapper objectMapper;

    private JWTVerifier accessTokenVerifier;

    private JWTVerifier refreshTokenVerifier;

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

    private String createToken(@NonNull IdentifiableUser user, @NonNull Long tokenTTL, @NonNull String secret) {
        return createToken(user.getUsername(), user.getId(), tokenTTL, secret);
    }

    private String createToken(final @NonNull String subject, @NonNull String userId, @NonNull Long tokenTTL,
            @NonNull String secret) throws JWTCreationException {
        return JWT.create()
            .withSubject(subject)
            .withClaim(SecurityConstants.CLAIM_KEY_USER_ID, userId)
            .withExpiresAt(new Date(System.currentTimeMillis() + tokenTTL))
            .sign(Algorithm.HMAC512(secret.getBytes()));
    }

    public DecodedJWT verifyAccessToken(@NonNull String token) throws JWTVerificationException {
        // This will also handle token expiration exception
        return accessTokenVerifier.verify(token.replace(SecurityConstants.TOKEN_PREFIX, ""));
    }

    public DecodedJWT verifyRefreshToken(@NonNull String token) throws JWTVerificationException {
        // This will also handle token expiration exception
        return refreshTokenVerifier.verify(token);
    }

    public JwtUser parseRefreshToken(String token) throws JWTVerificationException {
        DecodedJWT jwt = verifyRefreshToken(token);
        return new JwtUser(jwt.getClaim(SecurityConstants.CLAIM_KEY_USER_ID).asString(), jwt.getSubject());
    }

    public String tokenToJson(String accessToken, String refreshToken) throws JsonProcessingException {
        return objectMapper.writeValueAsString(new TokenPair(accessToken, refreshToken));
    }

}
