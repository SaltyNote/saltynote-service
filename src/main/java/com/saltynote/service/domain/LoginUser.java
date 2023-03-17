package com.saltynote.service.domain;

import com.saltynote.service.entity.SiteUser;
import lombok.Getter;
import org.springframework.security.core.userdetails.User;

import java.util.Collections;

public class LoginUser extends User implements IdentifiableUser {
    @Getter
    private final String id;

    public LoginUser(SiteUser user) {
        super(user.getUsername(), user.getPassword(), Collections.emptyList());
        this.id = user.getId();
    }
}
