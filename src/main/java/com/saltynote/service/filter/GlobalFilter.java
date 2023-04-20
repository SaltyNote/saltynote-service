package com.saltynote.service.filter;

import com.saltynote.service.security.SecurityConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class GlobalFilter extends OncePerRequestFilter
        implements ApplicationListener<ServletWebServerInitializedEvent> {

    private int port;

    @Override
    public void onApplicationEvent(ServletWebServerInitializedEvent event) {
        this.port = event.getWebServer().getPort();
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        response.addHeader(SecurityConstants.SALTY_PORT_HEADER, String.valueOf(port));
        filterChain.doFilter(request, response);
    }

}
