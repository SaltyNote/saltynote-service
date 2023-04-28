package com.saltynote.service.entity;

import com.devskiller.friendly_id.FriendlyId;
import com.saltynote.service.domain.IdentifiableUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "user")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Accessors(chain = true)
public class SiteUser implements Serializable, IdentifiableUser {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "username", nullable = false)
    @NotBlank
    private String username;

    @Column(name = "email", nullable = false)
    @NotBlank
    @Email
    private String email;

    @Column(name = "password", nullable = false)
    @NotBlank
    private String password;

    @Column(name = "register_time")
    private Timestamp registerTime;

    @Column(name = "idx")
    private Long idx;

    @PrePersist
    private void beforeSave() {
        this.id = FriendlyId.createFriendlyId();
        this.registerTime = new Timestamp(System.currentTimeMillis());
    }

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
