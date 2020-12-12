package com.saltynote.service.domain;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class EmailPayload {
  private String username;
  private String message;
  private String link;
  private String linkText;
}
