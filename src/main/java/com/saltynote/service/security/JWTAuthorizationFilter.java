package com.saltynote.service.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.saltynote.service.component.JwtInstance;
import com.saltynote.service.domain.transfer.JwtUser;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

public class JWTAuthorizationFilter extends BasicAuthenticationFilter {

    private final JwtInstance jwtInstance;

    public JWTAuthorizationFilter(AuthenticationManager authManager, JwtInstance jwtInstance) {
        super(authManager);
        this.jwtInstance = jwtInstance;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        String header = req.getHeader(SecurityConstants.HEADER_STRING);

        if (header == null || !header.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            chain.doFilter(req, res);
            return;
        }

        UsernamePasswordAuthenticationToken authentication = getAuthentication(req);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(req, res);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(SecurityConstants.HEADER_STRING);
        if (token != null) {
            // parse the token.
            DecodedJWT decodedJWT = jwtInstance.verifyAccessToken(token);

            if (decodedJWT != null) {
                return new UsernamePasswordAuthenticationToken(
                        new JwtUser(
                                decodedJWT.getClaim(SecurityConstants.CLAIM_KEY_USER_ID).asString(),
                                decodedJWT.getSubject()),
                        null,
                        Collections.emptyList());
            }
            return null;
        }
        return null;
    }
}
