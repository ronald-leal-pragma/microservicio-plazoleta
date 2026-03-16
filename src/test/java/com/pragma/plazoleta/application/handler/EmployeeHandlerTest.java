package com.pragma.plazoleta.application.handler;

import com.pragma.plazoleta.application.dto.request.EmployeeRequestDto;
import com.pragma.plazoleta.application.dto.response.EmployeeResponseDto;
import com.pragma.plazoleta.application.handler.impl.EmployeeHandler;
import com.pragma.plazoleta.application.mapper.IEmployeeRequestMapper;
import com.pragma.plazoleta.domain.api.IEmployeeServicePort;
import com.pragma.plazoleta.domain.model.RolModel;
import com.pragma.plazoleta.domain.model.UserModel;
import com.pragma.plazoleta.infrastructure.configuration.jwt.JwtUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmployeeHandlerTest {

    @Mock
    private IEmployeeServicePort employeeServicePort;

    @Mock
    private IEmployeeRequestMapper employeeRequestMapper;

    @InjectMocks
    private EmployeeHandler employeeHandler;

    private static final Long OWNER_ID = 10L;

    private EmployeeRequestDto requestDto;
    private UserModel employeeModel;
    private UserModel createdEmployee;

    @BeforeEach
    void setUp() {
        JwtUserDetails userDetails = new JwtUserDetails(OWNER_ID, "owner@mail.com", "PROPIETARIO");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        requestDto = new EmployeeRequestDto();
        requestDto.setNombre("Ana");
        requestDto.setApellido("López");
        requestDto.setCorreo("ana@mail.com");
        requestDto.setDocumentoDeIdentidad("12345678");
        requestDto.setCelular("3001234567");
        requestDto.setClave("clave123");
        requestDto.setIdRol(3L);
        requestDto.setFechaNacimiento(LocalDate.of(1990, 5, 15));

        employeeModel = UserModel.builder()
                .nombre("Ana")
                .apellido("López")
                .correo("ana@mail.com")
                .build();

        createdEmployee = UserModel.builder()
                .id(50L)
                .nombre("Ana")
                .apellido("López")
                .correo("ana@mail.com")
                .rol(RolModel.builder().id(3L).nombre("EMPLEADO").build())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Debe retornar EmployeeResponseDto cuando el empleado es creado")
    void createEmployee_shouldReturnResponseDtoWhenCreated() {
        when(employeeRequestMapper.toUser(requestDto)).thenReturn(employeeModel);
        when(employeeServicePort.createEmployee(employeeModel, OWNER_ID,1L)).thenReturn(createdEmployee);

        EmployeeResponseDto result = employeeHandler.createEmployee(requestDto);

        assertNotNull(result);
        assertEquals(50L, result.getId());
        assertEquals("Ana", result.getNombre());
        assertEquals("López", result.getApellido());
        assertEquals("ana@mail.com", result.getCorreo());
        assertEquals("EMPLEADO", result.getRol());
    }

    @Test
    @DisplayName("Debe retornar rol nulo en la respuesta cuando el empleado no tiene rol asignado")
    void createEmployee_shouldReturnNullRolWhenEmployeeHasNoRole() {
        createdEmployee.setRol(null);
        when(employeeRequestMapper.toUser(requestDto)).thenReturn(employeeModel);
        when(employeeServicePort.createEmployee(employeeModel, OWNER_ID,1L)).thenReturn(createdEmployee);

        EmployeeResponseDto result = employeeHandler.createEmployee(requestDto);

        assertNotNull(result);
        assertNull(result.getRol());
    }

    @Test
    @DisplayName("Debe llamar al servicio con el ID del propietario autenticado")
    void createEmployee_shouldCallServiceWithAuthenticatedOwnerId() {
        when(employeeRequestMapper.toUser(requestDto)).thenReturn(employeeModel);
        when(employeeServicePort.createEmployee(any(), eq(OWNER_ID),1L)).thenReturn(createdEmployee);

        employeeHandler.createEmployee(requestDto);

        verify(employeeServicePort).createEmployee(employeeModel, OWNER_ID,1L);
    }

    @Test
    @DisplayName("Debe llamar al mapper para convertir el requestDto en modelo")
    void createEmployee_shouldCallMapper() {
        when(employeeRequestMapper.toUser(requestDto)).thenReturn(employeeModel);
        when(employeeServicePort.createEmployee(any(), any(),1L)).thenReturn(createdEmployee);

        employeeHandler.createEmployee(requestDto);

        verify(employeeRequestMapper).toUser(requestDto);
    }

    @Test
    @DisplayName("Debe lanzar SecurityException cuando no hay autenticación en el contexto")
    void createEmployee_shouldThrowSecurityExceptionWhenNotAuthenticated() {
        SecurityContext emptyContext = mock(SecurityContext.class);
        when(emptyContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(emptyContext);

        assertThrows(SecurityException.class,
                () -> employeeHandler.createEmployee(requestDto));
    }
}

