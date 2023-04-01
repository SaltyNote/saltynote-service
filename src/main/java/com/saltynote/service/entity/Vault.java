package com.saltynote.service.entity;

import com.devskiller.friendly_id.FriendlyId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name = "vault")
@Data
@Accessors(chain = true)
public class Vault implements Serializable {

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
}
