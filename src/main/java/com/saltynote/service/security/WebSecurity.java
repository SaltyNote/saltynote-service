package com.saltynote.service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saltynote.service.component.JwtInstance;
import com.saltynote.service.domain.transfer.ServiceResponse;
import com.saltynote.service.service.UserDetailsServiceImpl;
import com.saltynote.service.service.UserService;
import com.saltynote.service.service.VaultService;
import jakarta.annotation.Resource;
import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@EnableWebSecurity
public class WebSecurity {
    private static final String[] PUBLIC_POST_ENDPOINTS = {
            SecurityConstants.SIGN_UP_URL,
            "/refresh_token",
            "/password/forget",
            "/password/reset",
            "/email/verification"
    };

    private static final String[] PUBLIC_GET_ENDPOINTS = {"/"};

    private static final String[] SWAGGER_URLS = {
            "/swagger-resources/**", "/swagger-ui/**", "/v2/api-docs", "/webjars/**"
    };

    @Resource
    private UserDetailsServiceImpl userDetailsService;
    @Resource
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Resource
    private VaultService vaultService;
    @Resource
    private JwtInstance jwtInstance;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private UserService userService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // @formatter:off
        http.cors()
                .and()
                .csrf()
                .disable()
                .authorizeHttpRequests(req -> req.requestMatchers(HttpMethod.POST, PUBLIC_POST_ENDPOINTS)
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_GET_ENDPOINTS)
                        .permitAll()
                        .requestMatchers(SWAGGER_URLS)
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .addFilter(new JWTAuthenticationFilter(authenticationManager(http), vaultService, jwtInstance, userService))
                .addFilter(new JWTAuthorizationFilter(authenticationManager(http), jwtInstance))
                // this disables session creation on Spring Security
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and().exceptionHandling()
                .authenticationEntryPoint((request, response, e) -> {
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.getWriter().write(objectMapper.writeValueAsString(new ServiceResponse(HttpStatus.FORBIDDEN, "Access Denied")));
                });
        // @formatter:on
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(userDetailsService)
                .passwordEncoder(bCryptPasswordEncoder)
                .and()
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        val source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", new CorsConfiguration().applyPermitDefaultValues());
        return source;
    }
}
