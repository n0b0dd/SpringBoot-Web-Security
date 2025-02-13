package com.kosign.spring_security.utils;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;
    private final Object[] args;

    public CustomException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.errorCode = "ERROR_" + status.value();
        this.args = new Object[]{};
    }

    public CustomException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.args = new Object[]{};
    }

    public CustomException(String message, HttpStatus status, String errorCode, Object... args) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.args = args;
    }
}

