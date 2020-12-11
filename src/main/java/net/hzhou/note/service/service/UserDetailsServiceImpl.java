package net.hzhou.note.service.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import net.hzhou.note.service.domain.LoginUser;
import net.hzhou.note.service.entity.SiteUser;
import net.hzhou.note.service.repository.UserRepository;

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
