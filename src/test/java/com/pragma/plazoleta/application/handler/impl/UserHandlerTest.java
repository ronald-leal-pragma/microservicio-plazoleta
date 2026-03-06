package com.pragma.plazoleta.application.handler.impl;

import com.pragma.plazoleta.application.dto.request.UserRequestDto;
import com.pragma.plazoleta.application.mapper.IUserRequestMapper;
import com.pragma.plazoleta.domain.api.IUserServicePort;
import com.pragma.plazoleta.domain.model.UserModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserHandlerTest {

    @Mock
    private IUserServicePort userServicePort;

    @Mock
    private IUserRequestMapper userRequestMapper;

    @InjectMocks
    private UserHandler userHandler;

    private UserRequestDto userRequestDto;
    private UserModel userModel;

    @BeforeEach
    void setUp() {
        userRequestDto = new UserRequestDto();
        userRequestDto.setNombre("Juan");
        userRequestDto.setApellido("Pérez");
        userRequestDto.setDocumentoDeIdentidad("1234567890");
        userRequestDto.setCelular("+573005698325");
        userRequestDto.setFechaNacimiento(LocalDate.of(1990, 5, 15));
        userRequestDto.setCorreo("juan@email.com");
        userRequestDto.setClave("MiClave123");

        userModel = new UserModel();
    }

    @Test
    @DisplayName("Debe mapear el DTO a modelo y llamar al servicePort una vez")
    void saveUser_shouldMapDtoToModelAndCallServicePort() {
        when(userRequestMapper.toUser(userRequestDto)).thenReturn(userModel);

        userHandler.saveUser(userRequestDto);

        verify(userRequestMapper, times(1)).toUser(userRequestDto);
        verify(userServicePort, times(1)).saveUser(userModel);
    }

    @Test
    @DisplayName("No debe lanzar excepción cuando el flujo es exitoso")
    void saveUser_shouldNotThrowAnyException_whenServiceRunsSuccessfully() {
        when(userRequestMapper.toUser(any())).thenReturn(userModel);
        doNothing().when(userServicePort).saveUser(any());

        assertDoesNotThrow(() -> userHandler.saveUser(userRequestDto));
    }

    @Test
    @DisplayName("Debe propagar excepción cuando el servicePort lanza una excepción")
    void saveUser_shouldPropagate_whenServicePortThrows() {
        when(userRequestMapper.toUser(any())).thenReturn(userModel);
        doThrow(new RuntimeException("Error en servicio")).when(userServicePort).saveUser(any());

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                () -> userHandler.saveUser(userRequestDto));
    }
}
