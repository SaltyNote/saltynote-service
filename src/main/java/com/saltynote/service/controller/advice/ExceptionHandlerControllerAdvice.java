package com.saltynote.service.controller.advice;

import com.saltynote.service.domain.transfer.ServiceResponse;
import com.saltynote.service.exception.WebAppRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ExceptionHandlerControllerAdvice {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ServiceResponse> handleAuthenticationException(
            AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ServiceResponse(HttpStatus.UNAUTHORIZED, e.getMessage()));
    }

    @ExceptionHandler(WebAppRuntimeException.class)
    public ResponseEntity<ServiceResponse> handleWebClientRuntimeException(
            WebAppRuntimeException e) {
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
