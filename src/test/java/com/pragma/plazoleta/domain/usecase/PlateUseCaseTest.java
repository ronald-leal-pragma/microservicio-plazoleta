package com.pragma.plazoleta.domain.usecase;

import com.pragma.plazoleta.domain.exception.DomainException;
import com.pragma.plazoleta.domain.exception.message.PlateErrorMessages;
import com.pragma.plazoleta.domain.exception.message.RestaurantErrorMessages;
import com.pragma.plazoleta.domain.model.PlateModel;
import com.pragma.plazoleta.domain.model.RestaurantModel;
import com.pragma.plazoleta.domain.spi.IPlatePersistencePort;
import com.pragma.plazoleta.domain.spi.IRestaurantPersistencePort;
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
class PlateUseCaseTest {

    @Mock
    private IPlatePersistencePort platePersistencePort;

    @Mock
    private IRestaurantPersistencePort restaurantPersistencePort;

    @InjectMocks
    private PlateUseCase plateUseCase;

    private PlateModel validPlateModel;
    private RestaurantModel restaurantModel;

    private static final Long OWNER_ID = 10L;
    private static final Long RESTAURANT_ID = 1L;
    private static final Long PLATE_ID = 5L;

    @BeforeEach
    void setUp() {
        restaurantModel = new RestaurantModel();
        restaurantModel.setId(RESTAURANT_ID);
        restaurantModel.setNombre("Restaurante Test");
        restaurantModel.setIdUsuarioPropietario(OWNER_ID);

        validPlateModel = PlateModel.builder()
                .id(PLATE_ID)
                .nombre("Bandeja Paisa")
                .precio(25000)
                .descripcion("Plato típico colombiano")
                .urlImagen("http://img.com/bandeja.jpg")
                .categoria("Típico")
                .idRestaurante(RESTAURANT_ID)
                .build();
    }


    @Test
    @DisplayName("Debe guardar plato cuando todos los datos son válidos")
    void savePlate_shouldSaveWhenAllDataIsValid() {
        when(restaurantPersistencePort.findRestaurantById(RESTAURANT_ID)).thenReturn(Optional.of(restaurantModel));
        when(platePersistencePort.existsPlateByNameAndRestaurantId("Bandeja Paisa", RESTAURANT_ID)).thenReturn(false);
        when(platePersistencePort.savePlate(any())).thenReturn(validPlateModel);

        PlateModel result = plateUseCase.savePlate(validPlateModel, OWNER_ID);

        assertNotNull(result);
        assertEquals("Bandeja Paisa", result.getNombre());
        assertTrue(validPlateModel.getActiva());
        verify(platePersistencePort).savePlate(validPlateModel);
    }

    @Test
    @DisplayName("Debe marcar el plato como activo automáticamente al crear")
    void savePlate_shouldSetActivaToTrue() {
        validPlateModel.setActiva(null);
        when(restaurantPersistencePort.findRestaurantById(RESTAURANT_ID)).thenReturn(Optional.of(restaurantModel));
        when(platePersistencePort.existsPlateByNameAndRestaurantId(any(), any())).thenReturn(false);
        when(platePersistencePort.savePlate(any())).thenReturn(validPlateModel);

        plateUseCase.savePlate(validPlateModel, OWNER_ID);

        assertTrue(validPlateModel.getActiva());
    }


    @Test
    @DisplayName("Debe lanzar excepción cuando el precio es 0")
    void savePlate_shouldThrowWhenPriceIsZero() {
        validPlateModel.setPrecio(0);

        DomainException ex = assertThrows(DomainException.class,
                () -> plateUseCase.savePlate(validPlateModel, OWNER_ID));

        assertEquals(PlateErrorMessages.INVALID_PRICE, ex.getMessage());
        verify(platePersistencePort, never()).savePlate(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el precio es negativo")
    void savePlate_shouldThrowWhenPriceIsNegative() {
        validPlateModel.setPrecio(-100);

        DomainException ex = assertThrows(DomainException.class,
                () -> plateUseCase.savePlate(validPlateModel, OWNER_ID));

        assertEquals(PlateErrorMessages.INVALID_PRICE, ex.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el precio es nulo")
    void savePlate_shouldThrowWhenPriceIsNull() {
        validPlateModel.setPrecio(null);

        DomainException ex = assertThrows(DomainException.class,
                () -> plateUseCase.savePlate(validPlateModel, OWNER_ID));

        assertEquals(PlateErrorMessages.INVALID_PRICE, ex.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el restaurante no existe")
    void savePlate_shouldThrowWhenRestaurantNotFound() {
        when(restaurantPersistencePort.findRestaurantById(RESTAURANT_ID)).thenReturn(Optional.empty());

        DomainException ex = assertThrows(DomainException.class,
                () -> plateUseCase.savePlate(validPlateModel, OWNER_ID));

        assertEquals(RestaurantErrorMessages.NOT_FOUND, ex.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el usuario no es propietario del restaurante")
    void savePlate_shouldThrowWhenUserIsNotRestaurantOwner() {
        restaurantModel.setIdUsuarioPropietario(99L); // otro propietario
        when(restaurantPersistencePort.findRestaurantById(RESTAURANT_ID)).thenReturn(Optional.of(restaurantModel));

        DomainException ex = assertThrows(DomainException.class,
                () -> plateUseCase.savePlate(validPlateModel, OWNER_ID));

        assertEquals(RestaurantErrorMessages.USER_NOT_RESTAURANT_OWNER, ex.getMessage());
    }


    @Test
    @DisplayName("Debe lanzar excepción cuando el plato ya existe en el restaurante")
    void savePlate_shouldThrowWhenPlateAlreadyExistsInRestaurant() {
        when(restaurantPersistencePort.findRestaurantById(RESTAURANT_ID)).thenReturn(Optional.of(restaurantModel));
        when(platePersistencePort.existsPlateByNameAndRestaurantId("Bandeja Paisa", RESTAURANT_ID)).thenReturn(true);

        DomainException ex = assertThrows(DomainException.class,
                () -> plateUseCase.savePlate(validPlateModel, OWNER_ID));

        assertEquals(PlateErrorMessages.ALREADY_EXISTS, ex.getMessage());
    }


    @Test
    @DisplayName("Debe actualizar plato cuando todos los datos son válidos")
    void updatePlate_shouldUpdateWhenAllDataIsValid() {
        when(platePersistencePort.findPlateById(PLATE_ID)).thenReturn(Optional.of(validPlateModel));
        when(restaurantPersistencePort.findRestaurantById(RESTAURANT_ID)).thenReturn(Optional.of(restaurantModel));
        when(platePersistencePort.updatePlate(any())).thenReturn(validPlateModel);

        PlateModel result = plateUseCase.updatePlate(PLATE_ID, 30000, "Nueva descripción", OWNER_ID);

        assertNotNull(result);
        verify(platePersistencePort).updatePlate(validPlateModel);
    }

    @Test
    @DisplayName("Debe lanzar excepción al actualizar si el plato no existe")
    void updatePlate_shouldThrowWhenPlateNotFound() {
        when(platePersistencePort.findPlateById(PLATE_ID)).thenReturn(Optional.empty());

        DomainException ex = assertThrows(DomainException.class,
                () -> plateUseCase.updatePlate(PLATE_ID, 30000, "desc", OWNER_ID));

        assertEquals(PlateErrorMessages.NOT_FOUND, ex.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción al actualizar si el precio no es válido")
    void updatePlate_shouldThrowWhenPriceIsInvalidOnUpdate() {
        DomainException ex = assertThrows(DomainException.class,
                () -> plateUseCase.updatePlate(PLATE_ID, 0, "desc", OWNER_ID));

        assertEquals(PlateErrorMessages.INVALID_PRICE, ex.getMessage());
        verify(platePersistencePort, never()).updatePlate(any());
    }

    @Test
    @DisplayName("Debe actualizar precio y descripción del plato")
    void updatePlate_shouldUpdatePriceAndDescription() {
        when(platePersistencePort.findPlateById(PLATE_ID)).thenReturn(Optional.of(validPlateModel));
        when(restaurantPersistencePort.findRestaurantById(RESTAURANT_ID)).thenReturn(Optional.of(restaurantModel));
        when(platePersistencePort.updatePlate(any())).thenAnswer(inv -> inv.getArgument(0));

        PlateModel result = plateUseCase.updatePlate(PLATE_ID, 50000, "Descripción actualizada", OWNER_ID);

        assertEquals(50000, result.getPrecio());
        assertEquals("Descripción actualizada", result.getDescripcion());
    }


    @Test
    @DisplayName("Debe cambiar estado del plato a inactivo")
    void togglePlateStatus_shouldDeactivatePlate() {
        when(platePersistencePort.findPlateById(PLATE_ID)).thenReturn(Optional.of(validPlateModel));
        when(restaurantPersistencePort.findRestaurantById(RESTAURANT_ID)).thenReturn(Optional.of(restaurantModel));
        when(platePersistencePort.updatePlate(any())).thenAnswer(inv -> inv.getArgument(0));

        PlateModel result = plateUseCase.togglePlateStatus(PLATE_ID, false, OWNER_ID);

        assertFalse(result.getActiva());
    }

    @Test
    @DisplayName("Debe cambiar estado del plato a activo")
    void togglePlateStatus_shouldActivatePlate() {
        validPlateModel.setActiva(false);
        when(platePersistencePort.findPlateById(PLATE_ID)).thenReturn(Optional.of(validPlateModel));
        when(restaurantPersistencePort.findRestaurantById(RESTAURANT_ID)).thenReturn(Optional.of(restaurantModel));
        when(platePersistencePort.updatePlate(any())).thenAnswer(inv -> inv.getArgument(0));

        PlateModel result = plateUseCase.togglePlateStatus(PLATE_ID, true, OWNER_ID);

        assertTrue(result.getActiva());
    }

    @Test
    @DisplayName("Debe lanzar excepción al cambiar estado si el plato no existe")
    void togglePlateStatus_shouldThrowWhenPlateNotFound() {
        when(platePersistencePort.findPlateById(PLATE_ID)).thenReturn(Optional.empty());

        DomainException ex = assertThrows(DomainException.class,
                () -> plateUseCase.togglePlateStatus(PLATE_ID, false, OWNER_ID));

        assertEquals(PlateErrorMessages.NOT_FOUND, ex.getMessage());
    }


    @Test
    @DisplayName("Debe listar platos sin filtro de categoría")
    void listPlatesByRestaurant_shouldListWithoutCategoryFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<PlateModel> expectedPage = new PageImpl<>(List.of(validPlateModel));
        when(restaurantPersistencePort.findRestaurantById(RESTAURANT_ID)).thenReturn(Optional.of(restaurantModel));
        when(platePersistencePort.findPlatesByRestaurantId(RESTAURANT_ID, pageable)).thenReturn(expectedPage);

        Page<PlateModel> result = plateUseCase.listPlatesByRestaurant(RESTAURANT_ID, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(platePersistencePort).findPlatesByRestaurantId(RESTAURANT_ID, pageable);
        verify(platePersistencePort, never()).findPlatesByRestaurantIdAndCategory(any(), any(), any());
    }

    @Test
    @DisplayName("Debe listar platos filtrando por categoría")
    void listPlatesByRestaurant_shouldListWithCategoryFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<PlateModel> expectedPage = new PageImpl<>(List.of(validPlateModel));
        when(restaurantPersistencePort.findRestaurantById(RESTAURANT_ID)).thenReturn(Optional.of(restaurantModel));
        when(platePersistencePort.findPlatesByRestaurantIdAndCategory(RESTAURANT_ID, "Típico", pageable))
                .thenReturn(expectedPage);

        Page<PlateModel> result = plateUseCase.listPlatesByRestaurant(RESTAURANT_ID, "Típico", pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(platePersistencePort).findPlatesByRestaurantIdAndCategory(RESTAURANT_ID, "Típico", pageable);
        verify(platePersistencePort, never()).findPlatesByRestaurantId(any(), any());
    }

    @Test
    @DisplayName("Debe lanzar excepción al listar platos si el restaurante no existe")
    void listPlatesByRestaurant_shouldThrowWhenRestaurantNotFound() {
        Pageable pageable = PageRequest.of(0, 10);
        when(restaurantPersistencePort.findRestaurantById(RESTAURANT_ID)).thenReturn(Optional.empty());

        DomainException ex = assertThrows(DomainException.class,
                () -> plateUseCase.listPlatesByRestaurant(RESTAURANT_ID, null, pageable));

        assertEquals(RestaurantErrorMessages.NOT_FOUND, ex.getMessage());
    }

    @Test
    @DisplayName("Debe ignorar categoría vacía y no aplicar filtro")
    void listPlatesByRestaurant_shouldIgnoreBlankCategory() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<PlateModel> expectedPage = new PageImpl<>(List.of(validPlateModel));
        when(restaurantPersistencePort.findRestaurantById(RESTAURANT_ID)).thenReturn(Optional.of(restaurantModel));
        when(platePersistencePort.findPlatesByRestaurantId(RESTAURANT_ID, pageable)).thenReturn(expectedPage);

        Page<PlateModel> result = plateUseCase.listPlatesByRestaurant(RESTAURANT_ID, "  ", pageable);

        assertNotNull(result);
        verify(platePersistencePort).findPlatesByRestaurantId(RESTAURANT_ID, pageable);
    }
}

