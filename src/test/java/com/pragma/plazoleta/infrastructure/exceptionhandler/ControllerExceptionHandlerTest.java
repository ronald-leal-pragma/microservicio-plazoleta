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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ControllerExceptionHandlerTest {

    private ControllerExceptionHandler handler;
    private MockHttpServletRequest mockRequest;

    @BeforeEach
    void setUp() {
        handler = new ControllerExceptionHandler();
        mockRequest = new MockHttpServletRequest();
        mockRequest.setRequestURI("/test/resource");
    }

    @Test
    @DisplayName("Debe retornar 404 cuando se lanza NoDataFoundException")
    void handleNoDataFoundException_shouldReturn404() {
        NoDataFoundException ex = new NoDataFoundException();

        ResponseEntity<ErrorResponseDto> response = handler.handleNoDataFoundException(ex, mockRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Not Found", response.getBody().getError());
        assertNotNull(response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
        assertEquals("/test/resource", response.getBody().getPath());
    }

    @Test
    @DisplayName("Debe retornar 400 con el mensaje del dominio cuando se lanza DomainException")
    void handleDomainException_shouldReturn400WithDomainMessage() {
        DomainException ex = new DomainException("El propietario debe ser mayor de edad");

        ResponseEntity<ErrorResponseDto> response = handler.handleDomainException(ex, mockRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals("El propietario debe ser mayor de edad", response.getBody().getMessage());
        assertEquals("VALIDATION_ERROR", response.getBody().getCode());
    }

    @Test
    @DisplayName("Debe retornar 400 cuando se lanza HttpMessageNotReadableException")
    void handleHttpMessageNotReadableException_shouldReturn400() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("JSON parse error", (Throwable) null, null);

        ResponseEntity<ErrorResponseDto> response = handler.handleHttpMessageNotReadableException(ex, mockRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
        assertNotNull(response.getBody().getMessage());
        assertFalse(response.getBody().getMessage().isEmpty());
        assertEquals("VALIDATION_ERROR", response.getBody().getCode());
    }

    @Test
    @DisplayName("Debe retornar 400 con nombre del parametro cuando se lanza MethodArgumentTypeMismatchException")
    void handleMethodArgumentTypeMismatchException_shouldReturn400WithParamName() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        doReturn("id").when(ex).getName();
        doReturn("abc").when(ex).getValue();

        ResponseEntity<ErrorResponseDto> response = handler.handleMethodArgumentTypeMismatchException(ex, mockRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertTrue(response.getBody().getMessage().contains("id"));
        assertEquals("VALIDATION_ERROR", response.getBody().getCode());
    }

    @Test
    @DisplayName("Debe retornar 500 cuando se lanza Exception generica")
    void handleGenericException_shouldReturn500() {
        Exception ex = new Exception("Error inesperado del sistema");

        ResponseEntity<ErrorResponseDto> response = handler.handleGenericException(ex, mockRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Internal Server Error", response.getBody().getError());
        assertNotNull(response.getBody().getMessage());
        assertFalse(response.getBody().getMessage().isEmpty());
    }
}