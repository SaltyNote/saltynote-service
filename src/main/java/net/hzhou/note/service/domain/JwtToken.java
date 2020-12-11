package net.hzhou.note.service.domain;

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JwtToken {
  @JsonProperty("access_token")
  private String accessToken;
  @JsonProperty("refresh_token")
  @NotBlank private String refreshToken;
}
