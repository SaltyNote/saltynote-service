package com.saltynote.service.service;

import java.sql.Timestamp;

import javax.annotation.Resource;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.saltynote.service.domain.LoginUser;
import com.saltynote.service.entity.SiteUser;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

  @Resource private UserService userService;

  @Override
  public LoginUser loadUserByUsername(String username) throws UsernameNotFoundException {
    SiteUser user = userService.getRepository().findByUsername(username);
    if (user == null) {
      throw new UsernameNotFoundException(username);
    }

    return new LoginUser(user);
  }
}
