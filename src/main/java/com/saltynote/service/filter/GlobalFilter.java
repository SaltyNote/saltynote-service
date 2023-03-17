package com.saltynote.service.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        response.addHeader("X-SaltyNote-Port", String.valueOf(port));
        filterChain.doFilter(request, response);
    }
}
