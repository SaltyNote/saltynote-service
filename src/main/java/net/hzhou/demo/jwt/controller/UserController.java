package net.hzhou.demo.jwt.controller;

import javax.validation.Valid;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.hzhou.demo.jwt.entity.SiteUser;
import net.hzhou.demo.jwt.repository.UserRepository;

@RestController
@RequestMapping("user")
public class UserController {

  private final UserRepository userRepository;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;

  public UserController(
      UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
    this.userRepository = userRepository;
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
  }

  @PostMapping("/sign-up")
  public void signUp(@Valid @RequestBody SiteUser siteUser) {
    siteUser.setPassword(bCryptPasswordEncoder.encode(siteUser.getPassword()));
    userRepository.save(siteUser);
  }
}
