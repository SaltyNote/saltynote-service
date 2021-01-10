package com.saltynote.service.entity;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import com.devskiller.friendly_id.FriendlyId;
import lombok.Data;
import lombok.experimental.Accessors;

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
