package com.pragma.plazoleta.domain.usecase;

import com.pragma.plazoleta.domain.exception.DomainException;
import com.pragma.plazoleta.domain.exception.ExceptionConstants;
import com.pragma.plazoleta.domain.model.*;
import com.pragma.plazoleta.domain.spi.IOrderPersistencePort;
import com.pragma.plazoleta.domain.spi.IPlatePersistencePort;
import com.pragma.plazoleta.domain.spi.IRestaurantPersistencePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderUseCaseTest {

    @Mock
    private IOrderPersistencePort orderPersistencePort;

    @Mock
    private IRestaurantPersistencePort restaurantPersistencePort;

    @Mock
    private IPlatePersistencePort platePersistencePort;

    @InjectMocks
    private OrderUseCase orderUseCase;

    private static final Long CLIENT_ID = 1L;
    private static final Long RESTAURANT_ID = 2L;
    private static final Long PLATE_ID = 3L;

    private OrderModel orderModel;
    private RestaurantModel restaurantModel;
    private PlateModel plateModel;
    private OrderItemModel orderItemModel;

    @BeforeEach
    void setUp() {
        restaurantModel = new RestaurantModel();
        restaurantModel.setId(RESTAURANT_ID);
        restaurantModel.setNombre("Restaurante Test");
        restaurantModel.setIdUsuarioPropietario(99L);

        plateModel = PlateModel.builder()
                .id(PLATE_ID)
                .nombre("Bandeja Paisa")
                .precio(25000)
                .descripcion("Plato típico")
                .activa(true)
                .idRestaurante(RESTAURANT_ID)
                .build();

        orderItemModel = OrderItemModel.builder()
                .idPlato(PLATE_ID)
                .cantidad(2)
                .build();

        orderModel = OrderModel.builder()
                .idRestaurante(RESTAURANT_ID)
                .items(Arrays.asList(orderItemModel))
                .build();
    }


    @Test
    @DisplayName("Debe crear pedido exitosamente cuando todos los datos son válidos")
    void createOrder_shouldCreateOrderWhenAllDataIsValid() {
        OrderModel savedOrder = OrderModel.builder()
                .id(100L)
                .idCliente(CLIENT_ID)
                .idRestaurante(RESTAURANT_ID)
                .estado(OrderStatus.PENDIENTE)
                .items(Arrays.asList(orderItemModel))
                .build();

        when(restaurantPersistencePort.findRestaurantById(RESTAURANT_ID)).thenReturn(Optional.of(restaurantModel));
        when(orderPersistencePort.existsActiveOrderByClientId(eq(CLIENT_ID), any())).thenReturn(false);
        when(platePersistencePort.findPlateById(PLATE_ID)).thenReturn(Optional.of(plateModel));
        when(orderPersistencePort.saveOrder(any())).thenReturn(savedOrder);

        OrderModel result = orderUseCase.createOrder(orderModel, CLIENT_ID);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(OrderStatus.PENDIENTE, result.getEstado());
        assertEquals(CLIENT_ID, orderModel.getIdCliente());
        verify(orderPersistencePort).saveOrder(orderModel);
    }

    @Test
    @DisplayName("Debe enriquecer los items con nombre y precio del plato")
    void createOrder_shouldEnrichItemsWithPlateData() {
        when(restaurantPersistencePort.findRestaurantById(RESTAURANT_ID)).thenReturn(Optional.of(restaurantModel));
        when(orderPersistencePort.existsActiveOrderByClientId(any(), any())).thenReturn(false);
        when(platePersistencePort.findPlateById(PLATE_ID)).thenReturn(Optional.of(plateModel));
        when(orderPersistencePort.saveOrder(any())).thenAnswer(inv -> inv.getArgument(0));

        orderUseCase.createOrder(orderModel, CLIENT_ID);

        assertEquals("Bandeja Paisa", orderItemModel.getNombrePlato());
        assertEquals(25000, orderItemModel.getPrecioPlato());
    }

    @Test
    @DisplayName("Debe establecer estado PENDIENTE al crear pedido")
    void createOrder_shouldSetStatusToPendiente() {
        when(restaurantPersistencePort.findRestaurantById(RESTAURANT_ID)).thenReturn(Optional.of(restaurantModel));
        when(orderPersistencePort.existsActiveOrderByClientId(any(), any())).thenReturn(false);
        when(platePersistencePort.findPlateById(PLATE_ID)).thenReturn(Optional.of(plateModel));
        when(orderPersistencePort.saveOrder(any())).thenAnswer(inv -> inv.getArgument(0));

        orderUseCase.createOrder(orderModel, CLIENT_ID);

        assertEquals(OrderStatus.PENDIENTE, orderModel.getEstado());
    }


    @Test
    @DisplayName("Debe lanzar excepción cuando el restaurante no existe")
    void createOrder_shouldThrowWhenRestaurantNotFound() {
        when(restaurantPersistencePort.findRestaurantById(RESTAURANT_ID)).thenReturn(Optional.empty());

        DomainException ex = assertThrows(DomainException.class,
                () -> orderUseCase.createOrder(orderModel, CLIENT_ID));

        assertEquals(ExceptionConstants.RESTAURANT_NOT_FOUND_MESSAGE, ex.getMessage());
        verify(orderPersistencePort, never()).saveOrder(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el cliente ya tiene pedido activo")
    void createOrder_shouldThrowWhenClientHasActiveOrder() {
        when(restaurantPersistencePort.findRestaurantById(RESTAURANT_ID)).thenReturn(Optional.of(restaurantModel));
        when(orderPersistencePort.existsActiveOrderByClientId(eq(CLIENT_ID), any())).thenReturn(true);

        DomainException ex = assertThrows(DomainException.class,
                () -> orderUseCase.createOrder(orderModel, CLIENT_ID));

        assertEquals(ExceptionConstants.CLIENT_HAS_ACTIVE_ORDER_MESSAGE, ex.getMessage());
        verify(orderPersistencePort, never()).saveOrder(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando un plato del pedido no existe")
    void createOrder_shouldThrowWhenPlateNotFound() {
        when(restaurantPersistencePort.findRestaurantById(RESTAURANT_ID)).thenReturn(Optional.of(restaurantModel));
        when(orderPersistencePort.existsActiveOrderByClientId(any(), any())).thenReturn(false);
        when(platePersistencePort.findPlateById(PLATE_ID)).thenReturn(Optional.empty());

        DomainException ex = assertThrows(DomainException.class,
                () -> orderUseCase.createOrder(orderModel, CLIENT_ID));

        assertEquals(ExceptionConstants.PLATE_NOT_FOUND_MESSAGE, ex.getMessage());
        verify(orderPersistencePort, never()).saveOrder(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando un plato no pertenece al restaurante")
    void createOrder_shouldThrowWhenPlateDoesNotBelongToRestaurant() {
        plateModel.setIdRestaurante(999L); // otro restaurante
        when(restaurantPersistencePort.findRestaurantById(RESTAURANT_ID)).thenReturn(Optional.of(restaurantModel));
        when(orderPersistencePort.existsActiveOrderByClientId(any(), any())).thenReturn(false);
        when(platePersistencePort.findPlateById(PLATE_ID)).thenReturn(Optional.of(plateModel));

        DomainException ex = assertThrows(DomainException.class,
                () -> orderUseCase.createOrder(orderModel, CLIENT_ID));

        assertEquals(ExceptionConstants.PLATE_NOT_BELONGS_TO_RESTAURANT_MESSAGE, ex.getMessage());
        verify(orderPersistencePort, never()).saveOrder(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando un plato está inactivo")
    void createOrder_shouldThrowWhenPlateIsInactive() {
        plateModel.setActiva(false);
        when(restaurantPersistencePort.findRestaurantById(RESTAURANT_ID)).thenReturn(Optional.of(restaurantModel));
        when(orderPersistencePort.existsActiveOrderByClientId(any(), any())).thenReturn(false);
        when(platePersistencePort.findPlateById(PLATE_ID)).thenReturn(Optional.of(plateModel));

        DomainException ex = assertThrows(DomainException.class,
                () -> orderUseCase.createOrder(orderModel, CLIENT_ID));

        assertEquals(ExceptionConstants.PLATE_NOT_ACTIVE_MESSAGE, ex.getMessage());
        verify(orderPersistencePort, never()).saveOrder(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la flag activa del plato es null")
    void createOrder_shouldThrowWhenPlateActivaIsNull() {
        plateModel.setActiva(null);
        when(restaurantPersistencePort.findRestaurantById(RESTAURANT_ID)).thenReturn(Optional.of(restaurantModel));
        when(orderPersistencePort.existsActiveOrderByClientId(any(), any())).thenReturn(false);
        when(platePersistencePort.findPlateById(PLATE_ID)).thenReturn(Optional.of(plateModel));

        DomainException ex = assertThrows(DomainException.class,
                () -> orderUseCase.createOrder(orderModel, CLIENT_ID));

        assertEquals(ExceptionConstants.PLATE_NOT_ACTIVE_MESSAGE, ex.getMessage());
    }

    @Test
    @DisplayName("Debe procesar múltiples items correctamente")
    void createOrder_shouldProcessMultipleItems() {
        PlateModel plate2 = PlateModel.builder()
                .id(4L)
                .nombre("Ajiaco")
                .precio(18000)
                .activa(true)
                .idRestaurante(RESTAURANT_ID)
                .build();

        OrderItemModel item2 = OrderItemModel.builder()
                .idPlato(4L)
                .cantidad(1)
                .build();

        orderModel.setItems(Arrays.asList(orderItemModel, item2));

        when(restaurantPersistencePort.findRestaurantById(RESTAURANT_ID)).thenReturn(Optional.of(restaurantModel));
        when(orderPersistencePort.existsActiveOrderByClientId(any(), any())).thenReturn(false);
        when(platePersistencePort.findPlateById(PLATE_ID)).thenReturn(Optional.of(plateModel));
        when(platePersistencePort.findPlateById(4L)).thenReturn(Optional.of(plate2));
        when(orderPersistencePort.saveOrder(any())).thenAnswer(inv -> inv.getArgument(0));

        OrderModel result = orderUseCase.createOrder(orderModel, CLIENT_ID);

        assertNotNull(result);
        assertEquals(2, result.getItems().size());
    }
}

