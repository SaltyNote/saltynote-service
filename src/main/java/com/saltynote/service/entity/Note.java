package com.saltynote.service.entity;

import com.saltynote.service.domain.Identifiable;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

@Document
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Accessors(chain = true)
public class Note implements Serializable, Identifiable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    private String userId;

    @NotBlank
    private String text;

    @NotBlank
    private String url;

    private String note;

    private Boolean isPageOnly = false;

    private String highlightColor = "";

    private Long createdTime = System.currentTimeMillis();

    private Set<String> tags;

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
