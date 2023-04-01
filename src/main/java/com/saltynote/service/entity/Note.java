package com.saltynote.service.entity;

import com.devskiller.friendly_id.FriendlyId;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.sql.Timestamp;

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
    @NotBlank
    private String text;

    @Column(name = "url", nullable = false)
    @NotBlank
    private String url;

    @Column(name = "note")
    private String note;

    @Column(name = "is_page_only")
    @JsonProperty("is_page_only")
    private Boolean pageOnly;

    @Column(name = "highlight_color")
    private String highlightColor;

    @Column(name = "created_time", nullable = false)
    private Timestamp createdTime;

    @Column(name = "tags")
    private String tags;

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
