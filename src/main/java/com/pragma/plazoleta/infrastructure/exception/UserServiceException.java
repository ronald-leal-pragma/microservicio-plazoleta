package com.pragma.plazoleta.infrastructure.exception;

import lombok.Getter;

@Getter
public class UserServiceException extends RuntimeException {
    private final int statusCode;
    private final String errorCode;

    public UserServiceException(String message, int statusCode, String errorCode) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }

    public UserServiceException(String message, int statusCode) {
        this(message, statusCode, "USER_SERVICE_ERROR");
    }
}
