package net.hzhou.demo.jwt.service;


import org.springframework.stereotype.Service;

import net.hzhou.demo.jwt.entity.User;
import net.hzhou.demo.jwt.utils.JwtUtils;

@Service
public class UserService {
    public User TEST_USER = new User("1d", "username1", "password1");

    public String login(String name, String password) {
        String token = null;
        try {
            User user = TEST_USER;
            token = JwtUtils.sign(user.getUsername(), user.getId(), user.getPassword());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return token;
    }

}
