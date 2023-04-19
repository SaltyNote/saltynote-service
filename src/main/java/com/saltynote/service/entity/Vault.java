package com.saltynote.service.entity;

import com.devskiller.friendly_id.FriendlyId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
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
@Table(name = "vault")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Accessors(chain = true)
public class Vault implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "secret", nullable = false)
    private String secret;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "email")
    private String email;

    @Column(name = "created_time", nullable = false)
    private Timestamp createdTime;

    @PrePersist
    private void beforeSave() {
        this.id = FriendlyId.createFriendlyId();
        this.createdTime = new Timestamp(System.currentTimeMillis());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Vault vault = (Vault) o;
        return Objects.equals(id, vault.id) && Objects.equals(userId, vault.userId)
                && Objects.equals(secret, vault.secret) && Objects.equals(type, vault.type)
                && Objects.equals(email, vault.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, secret, type, email);
    }

}
