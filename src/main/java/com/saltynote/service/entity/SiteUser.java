package com.saltynote.service.entity;

import com.saltynote.service.domain.IdentifiableUser;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Document
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Accessors(chain = true)
public class SiteUser implements Serializable, IdentifiableUser {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @NotBlank
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    private Date registerTime = new Date();

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        SiteUser siteUser = (SiteUser) o;
        return Objects.equals(id, siteUser.id) && Objects.equals(username, siteUser.username)
                && Objects.equals(email, siteUser.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, email);
    }

}
