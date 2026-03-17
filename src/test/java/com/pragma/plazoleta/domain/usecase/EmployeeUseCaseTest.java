package com.pragma.plazoleta.domain.usecase;

import com.pragma.plazoleta.domain.exception.DomainException;
import com.pragma.plazoleta.domain.exception.ExceptionConstants;
import com.pragma.plazoleta.domain.model.EmployeeRestaurantModel;
import com.pragma.plazoleta.domain.model.RestaurantModel;
import com.pragma.plazoleta.domain.model.RolModel;
import com.pragma.plazoleta.domain.model.UserModel;
import com.pragma.plazoleta.domain.spi.IEmployeeRestaurantPersistencePort;
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

    @Mock
    private IEmployeeRestaurantPersistencePort employeeRestaurantPersistencePort;

    @InjectMocks
    private EmployeeUseCase employeeUseCase;

    private static final Long OWNER_ID = 10L;
    private static final Long RESTAURANT_ID = 5L;

    private UserModel ownerUser;
    private UserModel employeeModel;
    private RestaurantModel restaurantModel;

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

        restaurantModel = new RestaurantModel();
        restaurantModel.setId(RESTAURANT_ID);
        restaurantModel.setNombre("Restaurante Test");
        restaurantModel.setIdUsuarioPropietario(OWNER_ID);
    }

    // =========================================================
    // createEmployee - caso feliz
    // =========================================================

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
        when(restaurantPersistencePort.findRestaurantById(RESTAURANT_ID)).thenReturn(Optional.of(restaurantModel));
        when(userPersistencePort.saveUser(any())).thenReturn(savedEmployee);
        when(employeeRestaurantPersistencePort.save(any())).thenReturn(
                EmployeeRestaurantModel.builder().idEmpleado(50L).idRestaurante(RESTAURANT_ID).build());

        UserModel result = employeeUseCase.createEmployee(employeeModel, OWNER_ID, RESTAURANT_ID);

        assertNotNull(result);
        assertEquals(50L, result.getId());
        assertEquals("EMPLEADO", result.getRol().getNombre());
        verify(userPersistencePort).saveUser(employeeModel);
        verify(employeeRestaurantPersistencePort).save(any());
    }

    @Test
    @DisplayName("Debe asignar automáticamente el rol EMPLEADO al crear")
    void createEmployee_shouldAssignEmployeeRole() {
        UserModel saved = UserModel.builder()
                .id(1L)
                .rol(RolModel.builder().nombre("EMPLEADO").build())
                .build();

        when(userPersistencePort.findUserById(OWNER_ID)).thenReturn(Optional.of(ownerUser));
        when(restaurantPersistencePort.findRestaurantById(RESTAURANT_ID)).thenReturn(Optional.of(restaurantModel));
        when(userPersistencePort.saveUser(any())).thenReturn(saved);
        when(employeeRestaurantPersistencePort.save(any())).thenReturn(new EmployeeRestaurantModel());

        employeeUseCase.createEmployee(employeeModel, OWNER_ID, RESTAURANT_ID);

        assertEquals(ExceptionConstants.ROL_EMPLEADO, employeeModel.getRol().getNombre());
        assertEquals(ExceptionConstants.ROL_EMPLEADO_ID, employeeModel.getRol().getId());
    }

    @Test
    @DisplayName("Debe guardar la relación empleado-restaurante después de crear el usuario")
    void createEmployee_shouldSaveEmployeeRestaurantRelation() {
        UserModel saved = UserModel.builder().id(50L).rol(RolModel.builder().nombre("EMPLEADO").build()).build();

        when(userPersistencePort.findUserById(OWNER_ID)).thenReturn(Optional.of(ownerUser));
        when(restaurantPersistencePort.findRestaurantById(RESTAURANT_ID)).thenReturn(Optional.of(restaurantModel));
        when(userPersistencePort.saveUser(any())).thenReturn(saved);
        when(employeeRestaurantPersistencePort.save(any())).thenReturn(new EmployeeRestaurantModel());

        employeeUseCase.createEmployee(employeeModel, OWNER_ID, RESTAURANT_ID);

        verify(employeeRestaurantPersistencePort).save(argThat(rel ->
                rel.getIdEmpleado().equals(50L) && rel.getIdRestaurante().equals(RESTAURANT_ID)));
    }

    // =========================================================
    // createEmployee - validación de propietario
    // =========================================================

    @Test
    @DisplayName("Debe lanzar excepción cuando el propietario no existe")
    void createEmployee_shouldThrowWhenOwnerNotFound() {
        when(userPersistencePort.findUserById(OWNER_ID)).thenReturn(Optional.empty());

        DomainException ex = assertThrows(DomainException.class,
                () -> employeeUseCase.createEmployee(employeeModel, OWNER_ID, RESTAURANT_ID));

        assertEquals(ExceptionConstants.USER_NOT_FOUND_MESSAGE, ex.getMessage());
        verify(userPersistencePort, never()).saveUser(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el usuario no tiene rol PROPIETARIO")
    void createEmployee_shouldThrowWhenUserIsNotOwner() {
        ownerUser.setRol(RolModel.builder().id(1L).nombre("CLIENTE").build());
        when(userPersistencePort.findUserById(OWNER_ID)).thenReturn(Optional.of(ownerUser));

        DomainException ex = assertThrows(DomainException.class,
                () -> employeeUseCase.createEmployee(employeeModel, OWNER_ID, RESTAURANT_ID));

        assertEquals(ExceptionConstants.USER_NOT_OWNER_MESSAGE, ex.getMessage());
        verify(userPersistencePort, never()).saveUser(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el usuario no tiene rol asignado")
    void createEmployee_shouldThrowWhenUserHasNoRole() {
        ownerUser.setRol(null);
        when(userPersistencePort.findUserById(OWNER_ID)).thenReturn(Optional.of(ownerUser));

        DomainException ex = assertThrows(DomainException.class,
                () -> employeeUseCase.createEmployee(employeeModel, OWNER_ID, RESTAURANT_ID));

        assertEquals(ExceptionConstants.USER_NOT_OWNER_MESSAGE, ex.getMessage());
    }

    // =========================================================
    // createEmployee - validación restaurante
    // =========================================================

    @Test
    @DisplayName("Debe lanzar excepción cuando el restaurante no existe")
    void createEmployee_shouldThrowWhenRestaurantNotFound() {
        when(userPersistencePort.findUserById(OWNER_ID)).thenReturn(Optional.of(ownerUser));
        when(restaurantPersistencePort.findRestaurantById(RESTAURANT_ID)).thenReturn(Optional.empty());

        DomainException ex = assertThrows(DomainException.class,
                () -> employeeUseCase.createEmployee(employeeModel, OWNER_ID, RESTAURANT_ID));

        assertEquals(ExceptionConstants.RESTAURANT_NOT_BELONGS_TO_OWNER_MESSAGE, ex.getMessage());
        verify(userPersistencePort, never()).saveUser(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el restaurante no pertenece al propietario")
    void createEmployee_shouldThrowWhenRestaurantDoesNotBelongToOwner() {
        restaurantModel.setIdUsuarioPropietario(99L); // otro propietario
        when(userPersistencePort.findUserById(OWNER_ID)).thenReturn(Optional.of(ownerUser));
        when(restaurantPersistencePort.findRestaurantById(RESTAURANT_ID)).thenReturn(Optional.of(restaurantModel));

        DomainException ex = assertThrows(DomainException.class,
                () -> employeeUseCase.createEmployee(employeeModel, OWNER_ID, RESTAURANT_ID));

        assertEquals(ExceptionConstants.RESTAURANT_NOT_BELONGS_TO_OWNER_MESSAGE, ex.getMessage());
        verify(userPersistencePort, never()).saveUser(any());
    }
}
