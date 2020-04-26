package net.hzhou.demo.jwt.controller;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.hzhou.demo.jwt.annotation.TokenRequired;
import net.hzhou.demo.jwt.entity.User;
import net.hzhou.demo.jwt.service.UserService;

@RestController
@RequestMapping("user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


    @PostMapping("/login")
    public Map<String, ? extends Serializable> login(User user) {
        String token = userService.login(user.getUsername(), user.getPassword());
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("token", token);
        return tokenMap;
    }

    @TokenRequired
    @GetMapping("/hello")
    public String getMessage() {
        return "Hello World";
    }
}