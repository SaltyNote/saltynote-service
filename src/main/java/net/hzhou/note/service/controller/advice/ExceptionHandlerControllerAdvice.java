package net.hzhou.note.service.controller.advice;

import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.extern.slf4j.Slf4j;
import net.hzhou.note.service.domain.GenericError;
import net.hzhou.note.service.exception.WebClientRuntimeException;

@ControllerAdvice
@Slf4j
public class ExceptionHandlerControllerAdvice {

  @ExceptionHandler(NoSuchElementException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public String handleNoSuchElementException(NoSuchElementException e) {
    return e.getMessage();
  }

  @ExceptionHandler(WebClientRuntimeException.class)
  public ResponseEntity<GenericError> handleWebClientRuntimeException(WebClientRuntimeException e) {
    return ResponseEntity.status(e.getStatus())
        .body(new GenericError(e.getStatus(), e.getMessage()));
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<GenericError> handleRuntimeException(RuntimeException e) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new GenericError(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
  }
}
