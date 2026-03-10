package com.pragma.plazoleta.infrastructure.exceptionhandler;

import com.pragma.plazoleta.domain.exception.DomainException;
import com.pragma.plazoleta.infrastructure.exception.NoDataFoundException;
import com.pragma.plazoleta.infrastructure.exception.RestaurantAlreadyExistsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ControllerExceptionHandler {

    private static final String MESSAGE = "message";

    /** 404 - Recurso no encontrado */
    @ExceptionHandler(NoDataFoundException.class)
    public ResponseEntity<Map<String, String>> handleNoDataFoundException(
            NoDataFoundException ex) {
        log.warn("[EXCEPTION] 404 - Recurso no encontrado: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Collections.singletonMap(MESSAGE,
                        ExceptionResponse.NO_DATA_FOUND.getMessage()));
    }

    /** 409 - El restaurante ya existe (NIT o nombre duplicado) */
    @ExceptionHandler(RestaurantAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleRestaurantAlreadyExistsException(
            RestaurantAlreadyExistsException ex) {
        log.warn("[EXCEPTION] 409 - Restaurante ya existe: NIT o nombre duplicado");
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Collections.singletonMap(MESSAGE,
                        ExceptionResponse.RESTAURANT_ALREADY_EXISTS.getMessage()));
    }

    /** 400 - Reglas de negocio del dominio (edad, formato, etc.) */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<Map<String, String>> handleDomainException(
            DomainException ex) {
        log.warn("[EXCEPTION] 400 - Validación de dominio fallida: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Collections.singletonMap(MESSAGE, ex.getMessage()));
    }

    /** 400 - Validaciones de campos (@Valid / @NotBlank / @Email / @Pattern) */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(field, errorMessage);
        });
        log.warn("[EXCEPTION] 400 - Validación de campos fallida: {}", errors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errors);
    }

    /** 400 - JSON malformado o campo con tipo incorrecto */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex) {
        log.warn("[EXCEPTION] 400 - JSON malformado o ilegible: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Collections.singletonMap(MESSAGE,
                        "El cuerpo de la solicitud es inválido o está mal formado"));
    }

    /** 400 - Parámetro de tipo incorrecto en la URL */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex) {
        log.warn("[EXCEPTION] 400 - Tipo de parámetro inválido: nombre='{}', valor='{}'",
                ex.getName(), ex.getValue());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Collections.singletonMap(MESSAGE,
                        "El parámetro '" + ex.getName() + "' tiene un tipo de dato inválido"));
    }

    /** 500 - Error genérico no controlado */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        log.error("[EXCEPTION] 500 - Error interno no controlado: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap(MESSAGE,
                        "Ha ocurrido un error interno. Por favor intente más tarde"));
    }
}
