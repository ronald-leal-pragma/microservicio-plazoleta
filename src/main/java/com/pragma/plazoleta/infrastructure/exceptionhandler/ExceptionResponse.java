package com.pragma.plazoleta.infrastructure.exceptionhandler;

import lombok.Getter;

@Getter
public enum ExceptionResponse {
    NO_DATA_FOUND("No data found for the requested petition"),
    USER_ALREADY_EXISTS("A user with the given email or document already exists");

    private final String message;

    ExceptionResponse(String message) {
        this.message = message;
    }

}
