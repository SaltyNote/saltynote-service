package com.saltynote.service.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.saltynote.service.domain.LoginUser;
import com.saltynote.service.entity.SiteUser;
import com.saltynote.service.repository.UserRepository;

import java.sql.Timestamp;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
  private final UserRepository userRepository;

  public UserDetailsServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public LoginUser loadUserByUsername(String username) throws UsernameNotFoundException {
    SiteUser user = userRepository.findByUsername(username);
    if (user == null) {
      throw new UsernameNotFoundException(username);
    }

    // issue #39 : update last_login_time when user login
    user.setLastLoginTime(new Timestamp(System.currentTimeMillis()));
    userRepository.save(user);

    return new LoginUser(user);
  }
}
