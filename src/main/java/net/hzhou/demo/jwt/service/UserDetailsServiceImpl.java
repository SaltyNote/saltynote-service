package net.hzhou.demo.jwt.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import net.hzhou.demo.jwt.domain.LoginUser;
import net.hzhou.demo.jwt.entity.SiteUser;
import net.hzhou.demo.jwt.repository.UserRepository;

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
    return new LoginUser(user);
  }
}
