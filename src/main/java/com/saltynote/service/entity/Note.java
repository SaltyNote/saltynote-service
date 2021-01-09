package com.saltynote.service.entity;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

import org.springframework.util.StringUtils;

import com.devskiller.friendly_id.FriendlyId;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Entity
@Table(name = "note")
@Data
@Accessors(chain = true)
public class Note implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "id", nullable = false)
  private String id;

  @Column(name = "user_id", nullable = false)
  private String userId;

  @Column(name = "text", nullable = false)
  private String text;

  @Column(name = "url", nullable = false)
  @NotBlank
  private String url;

  @Column(name = "note")
  @NotBlank
  private String note;

  @Column(name = "is_page_only")
  @JsonProperty("is_page_only")
  private Boolean pageOnly;

  @Column(name = "highlight_color")
  private String highlightColor;

  @Column(name = "created_time", nullable = false)
  private Timestamp createdTime;

  @PrePersist
  private void beforeSave() {
    this.id = FriendlyId.createFriendlyId();
    this.createdTime = new Timestamp(System.currentTimeMillis());
    if (this.pageOnly == null) {
      this.pageOnly = false;
    }
    if (!StringUtils.hasText(this.highlightColor)) {
      this.highlightColor = "";
    }
  }
}
