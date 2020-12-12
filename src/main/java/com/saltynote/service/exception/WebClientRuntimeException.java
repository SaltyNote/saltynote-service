package com.saltynote.service.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

public class WebClientRuntimeException extends RuntimeException {
  @Getter private HttpStatus status;

  public WebClientRuntimeException(HttpStatus status, String message) {
    super(message);
    this.status = status;
  }
}
