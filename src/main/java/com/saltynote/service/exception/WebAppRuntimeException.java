package com.saltynote.service.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

public class WebAppRuntimeException extends RuntimeException {
    @Getter
    private final HttpStatus status;

    public WebAppRuntimeException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
