package com.pragma.plazoleta.infrastructure.input.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pragma.plazoleta.application.dto.request.UserRequestDto;
import com.pragma.plazoleta.application.handler.IUserHandler;
import com.pragma.plazoleta.domain.exception.DomainException;
import com.pragma.plazoleta.infrastructure.exception.UserAlreadyExistsException;
import com.pragma.plazoleta.infrastructure.configuration.SecurityConfig;
import com.pragma.plazoleta.infrastructure.exceptionhandler.ControllerExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        value = UserRestController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class},
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
)
@Import(ControllerExceptionHandler.class)
class UserRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IUserHandler userHandler;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private UserRequestDto buildValidDto() {
        UserRequestDto dto = new UserRequestDto();
        dto.setNombre("Juan");
        dto.setApellido("Pérez");
        dto.setDocumentoDeIdentidad("1234567890");
        dto.setCelular("+573005698325");
        dto.setFechaNacimiento(LocalDate.of(1990, 5, 15));
        dto.setCorreo("juan@email.com");
        dto.setClave("MiClave123");
        return dto;
    }

    @Test
    @DisplayName("Debe retornar 201 cuando el request es válido")
    void saveOwner_shouldReturn201_whenRequestIsValid() throws Exception {
        doNothing().when(userHandler).saveUser(any());

        mockMvc.perform(post("/user/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildValidDto())))
                .andExpect(status().isCreated());

        verify(userHandler, times(1)).saveUser(any());
    }

    @Test
    @DisplayName("Debe retornar 400 cuando el nombre está vacío")
    void saveOwner_shouldReturn400_whenNombreIsBlank() throws Exception {
        UserRequestDto dto = buildValidDto();
        dto.setNombre("");

        mockMvc.perform(post("/user/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(userHandler, never()).saveUser(any());
    }

    @Test
    @DisplayName("Debe retornar 400 cuando el correo tiene formato inválido")
    void saveOwner_shouldReturn400_whenCorreoIsInvalid() throws Exception {
        UserRequestDto dto = buildValidDto();
        dto.setCorreo("correo-invalido");

        mockMvc.perform(post("/user/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(userHandler, never()).saveUser(any());
    }

    @Test
    @DisplayName("Debe retornar 400 cuando el documento tiene caracteres no numéricos")
    void saveOwner_shouldReturn400_whenDocumentoIsNotNumeric() throws Exception {
        UserRequestDto dto = buildValidDto();
        dto.setDocumentoDeIdentidad("ABC123");

        mockMvc.perform(post("/user/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(userHandler, never()).saveUser(any());
    }

    @Test
    @DisplayName("Debe retornar 400 cuando la fechaNacimiento es nula")
    void saveOwner_shouldReturn400_whenFechaNacimientoIsNull() throws Exception {
        UserRequestDto dto = buildValidDto();
        dto.setFechaNacimiento(null);

        mockMvc.perform(post("/user/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(userHandler, never()).saveUser(any());
    }

    @Test
    @DisplayName("Debe retornar 400 cuando el handler lanza DomainException por usuario menor de edad")
    void saveOwner_shouldReturn400_whenHandlerThrowsDomainException() throws Exception {
        doThrow(new DomainException("El propietario debe ser mayor de edad"))
                .when(userHandler).saveUser(any());

        mockMvc.perform(post("/user/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildValidDto())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El propietario debe ser mayor de edad"));
    }

    @Test
    @DisplayName("Debe retornar 409 cuando el handler lanza UserAlreadyExistsException")
    void saveOwner_shouldReturn409_whenHandlerThrowsUserAlreadyExistsException() throws Exception {
        doThrow(new UserAlreadyExistsException())
                .when(userHandler).saveUser(any());

        mockMvc.perform(post("/user/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildValidDto())))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Debe retornar 400 cuando el celular tiene formato inválido")
    void saveOwner_shouldReturn400_whenCelularIsInvalid() throws Exception {
        UserRequestDto dto = buildValidDto();
        dto.setCelular("12");

        mockMvc.perform(post("/user/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(userHandler, never()).saveUser(any());
    }
}
