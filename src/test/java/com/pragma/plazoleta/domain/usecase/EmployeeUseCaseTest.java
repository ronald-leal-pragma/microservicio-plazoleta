package com.pragma.plazoleta.domain.usecase;

import com.pragma.plazoleta.domain.exception.DomainException;
import com.pragma.plazoleta.domain.exception.ExceptionConstants;
import com.pragma.plazoleta.domain.model.RolModel;
import com.pragma.plazoleta.domain.model.UserModel;
import com.pragma.plazoleta.domain.spi.IRestaurantPersistencePort;
import com.pragma.plazoleta.domain.spi.IUserPersistencePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeUseCaseTest {

    @Mock
    private IUserPersistencePort userPersistencePort;

    @Mock
    private IRestaurantPersistencePort restaurantPersistencePort;

    @InjectMocks
    private EmployeeUseCase employeeUseCase;

    private static final Long OWNER_ID = 10L;

    private UserModel ownerUser;
    private UserModel employeeModel;

    @BeforeEach
    void setUp() {
        ownerUser = UserModel.builder()
                .id(OWNER_ID)
                .nombre("Carlos")
                .apellido("Gómez")
                .correo("carlos@mail.com")
                .rol(RolModel.builder().id(2L).nombre("PROPIETARIO").build())
                .build();

        employeeModel = UserModel.builder()
                .nombre("Ana")
                .apellido("López")
                .correo("ana@mail.com")
                .documentoDeIdentidad("12345678")
                .celular("3001234567")
                .clave("clave123")
                .build();
    }


    @Test
    @DisplayName("Debe crear empleado cuando el propietario y restaurante son válidos")
    void createEmployee_shouldCreateWhenAllDataIsValid() {
        UserModel savedEmployee = UserModel.builder()
                .id(50L)
                .nombre("Ana")
                .apellido("López")
                .correo("ana@mail.com")
                .rol(RolModel.builder().id(3L).nombre("EMPLEADO").build())
                .build();

        when(userPersistencePort.findUserById(OWNER_ID)).thenReturn(Optional.of(ownerUser));
        when(restaurantPersistencePort.existsRestaurantByOwnerId(OWNER_ID)).thenReturn(true);
        when(userPersistencePort.saveUser(any())).thenReturn(savedEmployee);

        UserModel result = employeeUseCase.createEmployee(employeeModel, OWNER_ID);

        assertNotNull(result);
        assertEquals(50L, result.getId());
        assertEquals("EMPLEADO", result.getRol().getNombre());
        verify(userPersistencePort).saveUser(employeeModel);
    }

    @Test
    @DisplayName("Debe asignar automáticamente el rol EMPLEADO al crear")
    void createEmployee_shouldAssignEmployeeRole() {
        UserModel saved = UserModel.builder().id(1L).rol(RolModel.builder().nombre("EMPLEADO").build()).build();

        when(userPersistencePort.findUserById(OWNER_ID)).thenReturn(Optional.of(ownerUser));
        when(restaurantPersistencePort.existsRestaurantByOwnerId(OWNER_ID)).thenReturn(true);
        when(userPersistencePort.saveUser(any())).thenReturn(saved);

        employeeUseCase.createEmployee(employeeModel, OWNER_ID);

        assertEquals(ExceptionConstants.ROL_EMPLEADO, employeeModel.getRol().getNombre());
        assertEquals(ExceptionConstants.ROL_EMPLEADO_ID, employeeModel.getRol().getId());
    }


    @Test
    @DisplayName("Debe lanzar excepción cuando el propietario no existe")
    void createEmployee_shouldThrowWhenOwnerNotFound() {
        when(userPersistencePort.findUserById(OWNER_ID)).thenReturn(Optional.empty());

        DomainException ex = assertThrows(DomainException.class,
                () -> employeeUseCase.createEmployee(employeeModel, OWNER_ID));

        assertEquals(ExceptionConstants.USER_NOT_FOUND_MESSAGE, ex.getMessage());
        verify(userPersistencePort, never()).saveUser(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el usuario no tiene rol PROPIETARIO")
    void createEmployee_shouldThrowWhenUserIsNotOwner() {
        ownerUser.setRol(RolModel.builder().id(1L).nombre("CLIENTE").build());
        when(userPersistencePort.findUserById(OWNER_ID)).thenReturn(Optional.of(ownerUser));

        DomainException ex = assertThrows(DomainException.class,
                () -> employeeUseCase.createEmployee(employeeModel, OWNER_ID));

        assertEquals(ExceptionConstants.USER_NOT_OWNER_MESSAGE, ex.getMessage());
        verify(userPersistencePort, never()).saveUser(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el usuario no tiene rol asignado")
    void createEmployee_shouldThrowWhenUserHasNoRole() {
        ownerUser.setRol(null);
        when(userPersistencePort.findUserById(OWNER_ID)).thenReturn(Optional.of(ownerUser));

        DomainException ex = assertThrows(DomainException.class,
                () -> employeeUseCase.createEmployee(employeeModel, OWNER_ID));

        assertEquals(ExceptionConstants.USER_NOT_OWNER_MESSAGE, ex.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el propietario no tiene restaurante")
    void createEmployee_shouldThrowWhenOwnerHasNoRestaurant() {
        when(userPersistencePort.findUserById(OWNER_ID)).thenReturn(Optional.of(ownerUser));
        when(restaurantPersistencePort.existsRestaurantByOwnerId(OWNER_ID)).thenReturn(false);

        DomainException ex = assertThrows(DomainException.class,
                () -> employeeUseCase.createEmployee(employeeModel, OWNER_ID));

        assertEquals(ExceptionConstants.OWNER_WITHOUT_RESTAURANT_MESSAGE, ex.getMessage());
        verify(userPersistencePort, never()).saveUser(any());
    }
}

