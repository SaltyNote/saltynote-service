package net.hzhou.demo.jwt.service;

import org.springframework.stereotype.Service;

import net.hzhou.demo.jwt.entity.User;
import net.hzhou.demo.jwt.repository.UserRepository;
import net.hzhou.demo.jwt.utils.JwtUtils;

@Service
public class UserService {

  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public String login(String name, String password) {
    String token = null;
    try {
      // TODO: needs spring security integration
      User user = userRepository.findByUsername(name);
      token = JwtUtils.sign(user.getUsername(), user.getId(), user.getPassword());

    } catch (Exception e) {
      e.printStackTrace();
    }
    return token;
  }
}
