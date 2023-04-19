package com.saltynote.service.service;

import com.saltynote.service.domain.LoginUser;
import com.saltynote.service.entity.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;

    @Override
    public LoginUser loadUserByUsername(String username) throws UsernameNotFoundException {
        SiteUser user = userService.getByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }

        return new LoginUser(user);
    }

}
