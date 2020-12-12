package com.saltynote.service.controller.advice;

import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.saltynote.service.domain.transfer.ServiceResponse;
import com.saltynote.service.exception.WebClientRuntimeException;
import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class ExceptionHandlerControllerAdvice {

  @ExceptionHandler(NoSuchElementException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public String handleNoSuchElementException(NoSuchElementException e) {
    return e.getMessage();
  }

  @ExceptionHandler(WebClientRuntimeException.class)
  public ResponseEntity<ServiceResponse> handleWebClientRuntimeException(
      WebClientRuntimeException e) {
    return ResponseEntity.status(e.getStatus())
        .body(new ServiceResponse(e.getStatus(), e.getMessage()));
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ServiceResponse> handleRuntimeException(RuntimeException e) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ServiceResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
  }
}
