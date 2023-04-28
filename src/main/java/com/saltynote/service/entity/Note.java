package com.saltynote.service.entity;

import com.devskiller.friendly_id.FriendlyId;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.saltynote.service.domain.Identifiable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.util.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "note")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Accessors(chain = true)
public class Note implements Serializable, Identifiable {

    @Serial
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

    @Column(name = "idx")
    private Long idx;

    @Column(name = "user_idx")
    private Long userIdx;

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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Note note1 = (Note) o;
        return Objects.equals(id, note1.id) && Objects.equals(userId, note1.userId) && Objects.equals(text, note1.text)
                && Objects.equals(url, note1.url) && Objects.equals(note, note1.note)
                && Objects.equals(tags, note1.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, text, url, note, tags);
    }

}
