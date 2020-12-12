package com.saltynote.service.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

import lombok.Data;
import lombok.experimental.Accessors;

@Entity
@Table(name = "note")
@Data
@Accessors(chain = true)
public class Note implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Integer id;

  @Column(name = "user_id", nullable = false)
  private Integer userId;

  @Column(name = "text", nullable = false)
  private String text;

  @Column(name = "url", nullable = false)
  @NotBlank
  private String url;

  @Column(name = "note")
  @NotBlank
  private String note;

  @Column(name = "is_page_only")
  private Boolean pageOnly;

  @Column(name = "created_time", nullable = false)
  private LocalDateTime createdTime;
}
