package com.saltynote.service.entity;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import com.devskiller.friendly_id.FriendlyId;
import com.saltynote.service.domain.IdentifiableUser;
import lombok.Data;
import lombok.experimental.Accessors;

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

  @Column(name = "email_verified")
  private Boolean emailVerified;

  @Column(name = "register_time")
  private Timestamp registerTime;

  @PrePersist
  private void beforeSave() {
    this.id = FriendlyId.createFriendlyId();
    this.registerTime = new Timestamp(System.currentTimeMillis());
  }
}
