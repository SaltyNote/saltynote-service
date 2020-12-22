package com.saltynote.service.controller.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.saltynote.service.domain.transfer.ServiceResponse;
import com.saltynote.service.exception.WebClientRuntimeException;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class ExceptionHandlerControllerAdvice {

  @ExceptionHandler(WebClientRuntimeException.class)
  public ResponseEntity<ServiceResponse> handleWebClientRuntimeException(
      WebClientRuntimeException e) {
    return ResponseEntity.status(e.getStatus())
        .body(new ServiceResponse(e.getStatus(), e.getMessage()));
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ServiceResponse> handleRuntimeException(RuntimeException e) {
    log.error(e.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            new ServiceResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Something is going wrong with the server, please try again later."));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ServiceResponse> handleRuntimeException(Exception e) {
    log.error(e.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            new ServiceResponse(
                HttpStatus.BAD_REQUEST,
                "Something is going wrong with your request, please try again later."));
  }
}
