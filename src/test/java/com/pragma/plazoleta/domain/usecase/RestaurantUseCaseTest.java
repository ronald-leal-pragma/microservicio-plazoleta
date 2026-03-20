package com.pragma.plazoleta.domain.usecase;

import com.pragma.plazoleta.domain.exception.DomainException;
import com.pragma.plazoleta.domain.exception.message.RestaurantErrorMessages;
import com.pragma.plazoleta.domain.exception.message.UserErrorMessages;
import com.pragma.plazoleta.domain.model.RestaurantModel;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantUseCaseTest {

    @Mock
    private IRestaurantPersistencePort restaurantPersistencePort;

    @Mock
    private IUserPersistencePort userPersistencePort;

    @InjectMocks
    private RestaurantUseCase restaurantUseCase;

    private RestaurantModel validRestaurantModel;
    private UserModel ownerUser;

    @BeforeEach
    void setUp() {
        validRestaurantModel = new RestaurantModel();
        validRestaurantModel.setNombre("Restaurante El Buen Sabor");
        validRestaurantModel.setNit("123456789");
        validRestaurantModel.setDireccion("Calle 10 # 20-30");
        validRestaurantModel.setTelefono("+573001234567");
        validRestaurantModel.setUrlLogo("http://logo.com/logo.png");
        validRestaurantModel.setIdUsuarioPropietario(1L);

        ownerUser = UserModel.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Pérez")
                .correo("juan@mail.com")
                .rol(RolModel.builder().id(2L).nombre("PROPIETARIO").build())
                .build();
    }


    @Test
    @DisplayName("Debe guardar restaurante cuando todos los datos son válidos")
    void saveRestaurant_shouldSaveWhenAllDataIsValid() {
        when(userPersistencePort.findUserById(1L)).thenReturn(Optional.of(ownerUser));
        when(restaurantPersistencePort.existsRestaurantByNit("123456789")).thenReturn(false);
        when(restaurantPersistencePort.existsRestaurantByName("Restaurante El Buen Sabor")).thenReturn(false);
        when(restaurantPersistencePort.saveRestaurant(any())).thenReturn(validRestaurantModel);

        RestaurantModel result = restaurantUseCase.saveRestaurant(validRestaurantModel);

        assertNotNull(result);
        assertEquals("Restaurante El Buen Sabor", result.getNombre());
        verify(restaurantPersistencePort).saveRestaurant(validRestaurantModel);
    }


    @Test
    @DisplayName("Debe lanzar excepción cuando el nombre es solo números")
    void saveRestaurant_shouldThrowWhenNameIsOnlyNumbers() {
        validRestaurantModel.setNombre("12345");

        DomainException ex = assertThrows(DomainException.class,
                () -> restaurantUseCase.saveRestaurant(validRestaurantModel));

        assertEquals(RestaurantErrorMessages.INVALID_NAME, ex.getMessage());
        verify(restaurantPersistencePort, never()).saveRestaurant(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el nombre es nulo")
    void saveRestaurant_shouldThrowWhenNameIsNull() {
        validRestaurantModel.setNombre(null);

        DomainException ex = assertThrows(DomainException.class,
                () -> restaurantUseCase.saveRestaurant(validRestaurantModel));

        assertEquals(RestaurantErrorMessages.INVALID_NAME, ex.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el NIT contiene letras")
    void saveRestaurant_shouldThrowWhenNitHasLetters() {
        validRestaurantModel.setNit("NIT123ABC");

        DomainException ex = assertThrows(DomainException.class,
                () -> restaurantUseCase.saveRestaurant(validRestaurantModel));

        assertEquals(RestaurantErrorMessages.INVALID_NIT, ex.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el NIT es nulo")
    void saveRestaurant_shouldThrowWhenNitIsNull() {
        validRestaurantModel.setNit(null);

        DomainException ex = assertThrows(DomainException.class,
                () -> restaurantUseCase.saveRestaurant(validRestaurantModel));

        assertEquals(RestaurantErrorMessages.INVALID_NIT, ex.getMessage());
    }


    @Test
    @DisplayName("Debe lanzar excepción cuando el teléfono tiene formato inválido")
    void saveRestaurant_shouldThrowWhenPhoneIsInvalid() {
        validRestaurantModel.setTelefono("telefono-invalido");

        DomainException ex = assertThrows(DomainException.class,
                () -> restaurantUseCase.saveRestaurant(validRestaurantModel));

        assertEquals(RestaurantErrorMessages.INVALID_PHONE, ex.getMessage());
    }

    @Test
    @DisplayName("Debe aceptar teléfono con prefijo +")
    void saveRestaurant_shouldAcceptPhoneWithPlusPrefix() {
        when(userPersistencePort.findUserById(1L)).thenReturn(Optional.of(ownerUser));
        when(restaurantPersistencePort.existsRestaurantByNit(any())).thenReturn(false);
        when(restaurantPersistencePort.existsRestaurantByName(any())).thenReturn(false);
        when(restaurantPersistencePort.saveRestaurant(any())).thenReturn(validRestaurantModel);

        validRestaurantModel.setTelefono("+573001234567");

        RestaurantModel result = restaurantUseCase.saveRestaurant(validRestaurantModel);

        assertNotNull(result);
    }


    @Test
    @DisplayName("Debe lanzar excepción cuando el usuario propietario no existe")
    void saveRestaurant_shouldThrowWhenOwnerNotFound() {
        when(userPersistencePort.findUserById(1L)).thenReturn(Optional.empty());

        DomainException ex = assertThrows(DomainException.class,
                () -> restaurantUseCase.saveRestaurant(validRestaurantModel));

        assertEquals(UserErrorMessages.USER_NOT_FOUND, ex.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el usuario no tiene rol PROPIETARIO")
    void saveRestaurant_shouldThrowWhenUserIsNotOwner() {
        ownerUser.setRol(RolModel.builder().id(1L).nombre("ADMINISTRADOR").build());
        when(userPersistencePort.findUserById(1L)).thenReturn(Optional.of(ownerUser));

        DomainException ex = assertThrows(DomainException.class,
                () -> restaurantUseCase.saveRestaurant(validRestaurantModel));

        assertEquals(UserErrorMessages.USER_NOT_OWNER, ex.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el usuario no tiene rol asignado")
    void saveRestaurant_shouldThrowWhenUserHasNoRole() {
        ownerUser.setRol(null);
        when(userPersistencePort.findUserById(1L)).thenReturn(Optional.of(ownerUser));

        DomainException ex = assertThrows(DomainException.class,
                () -> restaurantUseCase.saveRestaurant(validRestaurantModel));

        assertEquals(UserErrorMessages.USER_NOT_OWNER, ex.getMessage());
    }


    @Test
    @DisplayName("Debe lanzar excepción cuando el NIT ya existe")
    void saveRestaurant_shouldThrowWhenNitAlreadyExists() {
        when(userPersistencePort.findUserById(1L)).thenReturn(Optional.of(ownerUser));
        when(restaurantPersistencePort.existsRestaurantByNit("123456789")).thenReturn(true);

        DomainException ex = assertThrows(DomainException.class,
                () -> restaurantUseCase.saveRestaurant(validRestaurantModel));

        assertEquals(RestaurantErrorMessages.NIT_ALREADY_EXISTS, ex.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el nombre del restaurante ya existe")
    void saveRestaurant_shouldThrowWhenNameAlreadyExists() {
        when(userPersistencePort.findUserById(1L)).thenReturn(Optional.of(ownerUser));
        when(restaurantPersistencePort.existsRestaurantByNit("123456789")).thenReturn(false);
        when(restaurantPersistencePort.existsRestaurantByName("Restaurante El Buen Sabor")).thenReturn(true);

        DomainException ex = assertThrows(DomainException.class,
                () -> restaurantUseCase.saveRestaurant(validRestaurantModel));

        assertEquals(RestaurantErrorMessages.NAME_ALREADY_EXISTS, ex.getMessage());
    }


    @Test
    @DisplayName("Debe retornar página de restaurantes ordenados por nombre")
    void listRestaurants_shouldReturnPagedRestaurants() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<RestaurantModel> expectedPage = new PageImpl<>(List.of(validRestaurantModel));
        when(restaurantPersistencePort.findAllRestaurantsOrderByName(pageable)).thenReturn(expectedPage);

        Page<RestaurantModel> result = restaurantUseCase.listRestaurants(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(restaurantPersistencePort).findAllRestaurantsOrderByName(pageable);
    }

    @Test
    @DisplayName("Debe retornar página vacía cuando no hay restaurantes")
    void listRestaurants_shouldReturnEmptyPageWhenNoRestaurants() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<RestaurantModel> emptyPage = new PageImpl<>(List.of());
        when(restaurantPersistencePort.findAllRestaurantsOrderByName(pageable)).thenReturn(emptyPage);

        Page<RestaurantModel> result = restaurantUseCase.listRestaurants(pageable);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
    }
}

