package com.saltynote.service.domain;

import com.saltynote.service.entity.SiteUser;
import lombok.Getter;
import org.springframework.security.core.userdetails.User;

import java.util.Collections;
import java.util.Objects;

public class LoginUser extends User implements IdentifiableUser {

    @Getter
    private final Long id;

    public LoginUser(SiteUser user) {
        super(user.getUsername(), user.getPassword(), Collections.emptyList());
        this.id = user.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        LoginUser loginUser = (LoginUser) o;
        return Objects.equals(id, loginUser.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id);
    }

}
