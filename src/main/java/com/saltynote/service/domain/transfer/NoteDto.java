package com.saltynote.service.domain.transfer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class NoteDto {
    private String userId;

    @NotBlank
    private String text;

    @NotBlank
    private String url;

    private String note;

    @JsonProperty("is_page_only")
    private Boolean pageOnly;

    private String highlightColor;

    private String tags;
}
