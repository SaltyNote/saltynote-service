package com.saltynote.service.security;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.saltynote.service.component.JwtInstance;
import com.saltynote.service.service.UserDetailsServiceImpl;
import com.saltynote.service.service.VaultService;

@EnableWebSecurity
public class WebSecurity extends WebSecurityConfigurerAdapter {
  private static final String[] PUBLIC_POST_ENDPOINTS = {
    SecurityConstants.SIGN_UP_URL, "/refresh_token"
  };

  private static final String[] PUBLIC_GET_ENDPOINTS = {"/", "/email/verification/**"};

  private static final String[] SWAGGER_URLS = {
    "/swagger-resources/**", "/swagger-ui/**", "/v2/api-docs", "/webjars/**"
  };

  private final UserDetailsServiceImpl userDetailsService;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final VaultService vaultService;
  private final JwtInstance jwtInstance;

  public WebSecurity(
      UserDetailsServiceImpl userDetailsService,
      BCryptPasswordEncoder bCryptPasswordEncoder,
      VaultService vaultService,
      JwtInstance jwtInstance) {
    this.userDetailsService = userDetailsService;
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    this.vaultService = vaultService;
    this.jwtInstance = jwtInstance;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    // @formatter:off
    http.cors()
        .and()
          .csrf()
            .disable()
          .authorizeRequests()
            .antMatchers(HttpMethod.POST, PUBLIC_POST_ENDPOINTS)
              .permitAll()
            .antMatchers(HttpMethod.GET, PUBLIC_GET_ENDPOINTS)
              .permitAll()
            .antMatchers(SWAGGER_URLS)
              .permitAll()
            .anyRequest()
              .authenticated()
        .and()
          .addFilter(new JWTAuthenticationFilter(authenticationManager(), vaultService, jwtInstance))
          .addFilter(new JWTAuthorizationFilter(authenticationManager(), jwtInstance))
          // this disables session creation on Spring Security
          .sessionManagement()
          .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    // @formatter:on
  }

  @Override
  public void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", new CorsConfiguration().applyPermitDefaultValues());
    return source;
  }
}
