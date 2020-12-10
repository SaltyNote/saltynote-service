package net.hzhou.demo.jwt.controller.advice;

import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.extern.slf4j.Slf4j;
import net.hzhou.demo.jwt.domain.GenericError;

@ControllerAdvice
@Slf4j
public class ExceptionHandlerControllerAdvice {

  @ExceptionHandler(NoSuchElementException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public String handleNoSuchElementException(NoSuchElementException e) {
    return e.getMessage();
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<GenericError> handleRuntimeException(RuntimeException e) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new GenericError(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
  }
}
