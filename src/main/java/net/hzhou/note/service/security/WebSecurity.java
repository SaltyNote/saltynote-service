package net.hzhou.note.service.security;

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

import net.hzhou.note.service.component.JwtInstance;
import net.hzhou.note.service.repository.RefreshTokenRepository;
import net.hzhou.note.service.service.UserDetailsServiceImpl;

@EnableWebSecurity
public class WebSecurity extends WebSecurityConfigurerAdapter {
  private static final String[] PUBLIC_ENDPOINTS = {
    SecurityConstants.SIGN_UP_URL, "/refresh_token"
  };
  private final UserDetailsServiceImpl userDetailsService;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final RefreshTokenRepository tokenRepository;
  private final JwtInstance jwtInstance;

  public WebSecurity(
      UserDetailsServiceImpl userDetailsService,
      BCryptPasswordEncoder bCryptPasswordEncoder,
      RefreshTokenRepository tokenRepository,
      JwtInstance jwtInstance) {
    this.userDetailsService = userDetailsService;
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    this.tokenRepository = tokenRepository;
    this.jwtInstance = jwtInstance;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    //@formatter:off
    http.cors()
        .and()
          .csrf()
            .disable()
          .authorizeRequests()
            .antMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS)
              .permitAll()
            .anyRequest()
              .authenticated()
        .and()
          .addFilter(new JWTAuthenticationFilter(authenticationManager(), tokenRepository, jwtInstance))
          .addFilter(new JWTAuthorizationFilter(authenticationManager(), jwtInstance))
          // this disables session creation on Spring Security
          .sessionManagement()
          .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    //@formatter:on
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
