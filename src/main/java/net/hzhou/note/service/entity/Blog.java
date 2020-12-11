package net.hzhou.note.service.entity;

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
@Data
@Table(name = "blog")
@Accessors(chain = true)
public class Blog implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Integer id;

  @Column(name = "user_id", nullable = false)
  private Integer userId;

  @Column(name = "title", nullable = false)
  @NotBlank
  private String title;

  @Column(name = "content")
  @NotBlank
  private String content;

  @Column(name = "created_time", nullable = false)
  private LocalDateTime createdTime;
}
