package com.saltynote.service.entity;

import com.devskiller.friendly_id.FriendlyId;
import com.saltynote.service.domain.IdentifiableUser;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name = "user")
@Data
@Accessors(chain = true)
public class SiteUser implements Serializable, IdentifiableUser {

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

    @Column(name = "last_login_time")
    private Timestamp lastLoginTime;

    @PrePersist
    private void beforeSave() {
        this.id = FriendlyId.createFriendlyId();
        this.registerTime = new Timestamp(System.currentTimeMillis());
    }
}
