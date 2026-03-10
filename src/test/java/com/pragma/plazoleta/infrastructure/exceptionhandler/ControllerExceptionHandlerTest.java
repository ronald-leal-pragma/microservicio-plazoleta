package com.pragma.plazoleta.infrastructure.exceptionhandler;

import com.pragma.plazoleta.domain.exception.DomainException;
import com.pragma.plazoleta.infrastructure.exception.NoDataFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ControllerExceptionHandlerTest {

    private ControllerExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ControllerExceptionHandler();
    }

    @Test
    @DisplayName("Debe retornar 404 cuando se lanza NoDataFoundException")
    void handleNoDataFoundException_shouldReturn404() {
        NoDataFoundException ex = new NoDataFoundException();

        ResponseEntity<Map<String, String>> response = handler.handleNoDataFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ExceptionResponse.NO_DATA_FOUND.getMessage(), response.getBody().get("message"));
    }

    @Test
    @DisplayName("Debe retornar 400 con el mensaje del dominio cuando se lanza DomainException")
    void handleDomainException_shouldReturn400WithDomainMessage() {
        DomainException ex = new DomainException("El propietario debe ser mayor de edad");

        ResponseEntity<Map<String, String>> response = handler.handleDomainException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("El propietario debe ser mayor de edad", response.getBody().get("message"));
    }

    @Test
    @DisplayName("Debe retornar 400 cuando se lanza HttpMessageNotReadableException")
    void handleHttpMessageNotReadableException_shouldReturn400() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("JSON parse error");

        ResponseEntity<Map<String, String>> response = handler.handleHttpMessageNotReadableException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().get("message"));
        assertFalse(response.getBody().get("message").isEmpty());
    }

    @Test
    @DisplayName("Debe retornar 400 con nombre del parametro cuando se lanza MethodArgumentTypeMismatchException")
    void handleMethodArgumentTypeMismatchException_shouldReturn400WithParamName() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        doReturn("id").when(ex).getName();
        doReturn("abc").when(ex).getValue();

        ResponseEntity<Map<String, String>> response = handler.handleMethodArgumentTypeMismatchException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("message").contains("id"));
    }

    @Test
    @DisplayName("Debe retornar 500 cuando se lanza Exception generica")
    void handleGenericException_shouldReturn500() {
        Exception ex = new Exception("Error inesperado del sistema");

        ResponseEntity<Map<String, String>> response = handler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().get("message"));
        assertFalse(response.getBody().get("message").isEmpty());
    }
}