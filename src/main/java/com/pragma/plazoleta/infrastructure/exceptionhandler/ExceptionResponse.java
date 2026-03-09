package com.pragma.plazoleta.infrastructure.exceptionhandler;

import lombok.Getter;

@Getter
public enum ExceptionResponse {
    NO_DATA_FOUND("No se encontraron datos para la petición solicitada"),
    USER_ALREADY_EXISTS("Ya existe un usuario con el correo electrónico o documento indicado"),
    RESTAURANT_ALREADY_EXISTS("Ya existe un restaurante con el NIT o nombre indicado");

    private final String message;

    ExceptionResponse(String message) {
        this.message = message;
    }

}
