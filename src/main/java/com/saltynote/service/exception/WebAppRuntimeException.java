package com.saltynote.service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class WebAppRuntimeException extends RuntimeException {

    @Getter
    private final HttpStatus status;

    public WebAppRuntimeException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

}
