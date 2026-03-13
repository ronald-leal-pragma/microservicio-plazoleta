package com.pragma.plazoleta.infrastructure.exceptionhandler;

import com.pragma.plazoleta.domain.exception.DomainException;
import com.pragma.plazoleta.infrastructure.exception.NoDataFoundException;
import com.pragma.plazoleta.infrastructure.exception.RestaurantAlreadyExistsException;
import com.pragma.plazoleta.infrastructure.exception.UserServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ControllerExceptionHandler {

    private static final String BAD_REQUEST = "Bad Request";
    private static final String VALIDATION_ERROR = "VALIDATION_ERROR";

    @ExceptionHandler(NoDataFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNoDataFoundException(
            NoDataFoundException ex, HttpServletRequest request) {
        log.warn("[EXCEPTION] 404 - Recurso no encontrado: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponseDto.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .error("Not Found")
                        .message(ex.getMessage() != null
                                ? ex.getMessage()
                                : ExceptionResponse.NO_DATA_FOUND.getMessage())
                        .timestamp(Instant.now().toString())
                        .path(request.getRequestURI())
                        .build());
    }

    @ExceptionHandler(RestaurantAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleRestaurantAlreadyExistsException() {
        log.warn("[EXCEPTION] 409 - Restaurante ya existe: NIT o nombre duplicado");
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponseDto.builder()
                        .status(HttpStatus.CONFLICT.value())
                        .error("Conflict")
                        .message(ExceptionResponse.RESTAURANT_ALREADY_EXISTS.getMessage())
                        .code("RESTAURANT_ALREADY_EXISTS")
                        .build());
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponseDto> handleDomainException(
            DomainException ex, HttpServletRequest request) {
        log.warn("[EXCEPTION] 400 - Validación de dominio fallida: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseDto.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error(BAD_REQUEST)
                        .message(ex.getMessage())
                        .code(VALIDATION_ERROR)
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex) {
        String combinedMessage = ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    String field = ((FieldError) error).getField();
                    return "El campo '" + field + "': " + error.getDefaultMessage();
                })
                .collect(Collectors.joining("; "));
        log.warn("[EXCEPTION] 400 - Validación de campos fallida: {}", combinedMessage);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseDto.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error(BAD_REQUEST)
                        .message("Validación fallida: " + combinedMessage)
                        .code(VALIDATION_ERROR)
                        .build());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex) {
        log.warn("[EXCEPTION] 400 - JSON malformado o ilegible: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseDto.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error(BAD_REQUEST)
                        .message("El cuerpo de la solicitud es inválido o está mal formado")
                        .code(VALIDATION_ERROR)
                        .build());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex) {
        log.warn("[EXCEPTION] 400 - Tipo de parámetro inválido: nombre='{}', valor='{}'",
                ex.getName(), ex.getValue());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseDto.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error(BAD_REQUEST)
                        .message("El parámetro '" + ex.getName() + "' tiene un tipo de dato inválido")
                        .code(VALIDATION_ERROR)
                        .build());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDeniedException(
            AccessDeniedException ex) {
        log.warn("[EXCEPTION] 403 - Acceso denegado: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ErrorResponseDto.builder()
                        .status(HttpStatus.FORBIDDEN.value())
                        .error("Forbidden")
                        .message("No tienes permisos suficientes para acceder a este recurso.")
                        .code("PERMISSION_DENIED")
                        .details("El recurso solicitado requiere permisos adicionales.")
                        .build());
    }

    /**
     * Error del servicio de usuarios (integración HTTP)
     */
    @ExceptionHandler(UserServiceException.class)
    public ResponseEntity<ErrorResponseDto> handleUserServiceException(
            UserServiceException ex) {
        log.warn("[EXCEPTION] {} - Error del servicio de usuarios: {}", ex.getStatusCode(), ex.getMessage());
        
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode());
        if (status == null) {
            status = HttpStatus.BAD_REQUEST;
        }
        
        return ResponseEntity
                .status(status)
                .body(ErrorResponseDto.builder()
                        .status(status.value())
                        .error(status.getReasonPhrase())
                        .message(ex.getMessage())
                        .code(ex.getErrorCode())
                        .build());
    }

    /**
     * 500 - Error genérico no controlado
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(
            Exception ex, HttpServletRequest request) {
        log.error("[EXCEPTION] 500 - Error interno no controlado: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponseDto.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .error("Internal Server Error")
                        .message("Ha ocurrido un error interno. Por favor intente más tarde")
                        .build());
    }
}
