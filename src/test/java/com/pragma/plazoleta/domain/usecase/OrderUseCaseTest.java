package com.pragma.plazoleta.domain.usecase;

import com.pragma.plazoleta.domain.exception.DomainException;
import com.pragma.plazoleta.domain.exception.message.EmployeeErrorMessages;
import com.pragma.plazoleta.domain.exception.message.OrderErrorMessages;
import com.pragma.plazoleta.domain.exception.message.PlateErrorMessages;
import com.pragma.plazoleta.domain.exception.message.RestaurantErrorMessages;
import com.pragma.plazoleta.domain.model.*;
import com.pragma.plazoleta.domain.spi.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Arrays;
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

    @Mock
    private IEmployeeRestaurantPersistencePort employeeRestaurantPersistencePort;

    @Mock
    private ISmsNotificationPort smsNotificationPort;

    @Mock
    private IUserPersistencePort userPersistencePort;

    @InjectMocks
    private OrderUseCase orderUseCase;

    private static final Long CLIENT_ID = 1L;
    private static final Long RESTAURANT_ID = 2L;
    private static final Long PLATE_ID = 3L;
    private static final Long EMPLOYEE_ID = 10L;
    private static final Long ORDER_ID = 100L;

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

        assertEquals(RestaurantErrorMessages.NOT_FOUND, ex.getMessage());
        verify(orderPersistencePort, never()).saveOrder(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el cliente ya tiene pedido activo")
    void createOrder_shouldThrowWhenClientHasActiveOrder() {
        when(restaurantPersistencePort.findRestaurantById(RESTAURANT_ID)).thenReturn(Optional.of(restaurantModel));
        when(orderPersistencePort.existsActiveOrderByClientId(eq(CLIENT_ID), any())).thenReturn(true);

        DomainException ex = assertThrows(DomainException.class,
                () -> orderUseCase.createOrder(orderModel, CLIENT_ID));

        assertEquals(OrderErrorMessages.CLIENT_HAS_ACTIVE_ORDER, ex.getMessage());
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

        assertEquals(PlateErrorMessages.NOT_FOUND, ex.getMessage());
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

        assertEquals(PlateErrorMessages.NOT_BELONGS_TO_RESTAURANT, ex.getMessage());
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

        assertEquals(PlateErrorMessages.NOT_ACTIVE, ex.getMessage());
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

        assertEquals(PlateErrorMessages.NOT_ACTIVE, ex.getMessage());
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

    // Tests para assignEmployeeToOrder

    @Test
    @DisplayName("Debe asignar empleado al pedido exitosamente cuando datos son válidos")
    void assignEmployeeToOrder_shouldAssignWhenDataIsValid() {
        EmployeeRestaurantModel employeeRestaurant = EmployeeRestaurantModel.builder()
                .id(1L)
                .idEmpleado(EMPLOYEE_ID)
                .idRestaurante(RESTAURANT_ID)
                .build();

        OrderModel pendingOrder = OrderModel.builder()
                .id(ORDER_ID)
                .idCliente(CLIENT_ID)
                .idRestaurante(RESTAURANT_ID)
                .estado(OrderStatus.PENDIENTE)
                .creadoEn(Instant.now())
                .items(Arrays.asList(orderItemModel))
                .build();

        OrderModel expectedSavedOrder = OrderModel.builder()
                .id(ORDER_ID)
                .idCliente(CLIENT_ID)
                .idRestaurante(RESTAURANT_ID)
                .idChef(EMPLOYEE_ID)
                .estado(OrderStatus.EN_PREPARACION)
                .creadoEn(pendingOrder.getCreadoEn())
                .items(Arrays.asList(orderItemModel))
                .build();

        when(employeeRestaurantPersistencePort.findByEmployeeId(EMPLOYEE_ID))
                .thenReturn(Optional.of(employeeRestaurant));
        when(orderPersistencePort.findById(ORDER_ID)).thenReturn(Optional.of(pendingOrder));
        when(orderPersistencePort.saveOrder(any())).thenReturn(expectedSavedOrder);

        OrderModel result = orderUseCase.assignEmployeeToOrder(ORDER_ID, EMPLOYEE_ID);

        assertNotNull(result);
        assertEquals(ORDER_ID, result.getId());
        assertEquals(EMPLOYEE_ID, result.getIdChef());
        assertEquals(OrderStatus.EN_PREPARACION, result.getEstado());
        verify(orderPersistencePort).saveOrder(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando empleado no pertenece a ningún restaurante")
    void assignEmployeeToOrder_shouldThrowWhenEmployeeNotBelongsToRestaurant() {
        when(employeeRestaurantPersistencePort.findByEmployeeId(EMPLOYEE_ID))
                .thenReturn(Optional.empty());

        DomainException ex = assertThrows(DomainException.class,
                () -> orderUseCase.assignEmployeeToOrder(ORDER_ID, EMPLOYEE_ID));

        assertEquals(EmployeeErrorMessages.NOT_BELONGS_TO_RESTAURANT, ex.getMessage());
        verify(orderPersistencePort, never()).saveOrder(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el pedido no existe")
    void assignEmployeeToOrder_shouldThrowWhenOrderNotFound() {
        EmployeeRestaurantModel employeeRestaurant = EmployeeRestaurantModel.builder()
                .id(1L)
                .idEmpleado(EMPLOYEE_ID)
                .idRestaurante(RESTAURANT_ID)
                .build();

        when(employeeRestaurantPersistencePort.findByEmployeeId(EMPLOYEE_ID))
                .thenReturn(Optional.of(employeeRestaurant));
        when(orderPersistencePort.findById(ORDER_ID)).thenReturn(Optional.empty());

        DomainException ex = assertThrows(DomainException.class,
                () -> orderUseCase.assignEmployeeToOrder(ORDER_ID, EMPLOYEE_ID));

        assertEquals(OrderErrorMessages.NOT_FOUND, ex.getMessage());
        verify(orderPersistencePort, never()).saveOrder(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el pedido no está en estado PENDIENTE")
    void assignEmployeeToOrder_shouldThrowWhenOrderNotPending() {
        EmployeeRestaurantModel employeeRestaurant = EmployeeRestaurantModel.builder()
                .id(1L)
                .idEmpleado(EMPLOYEE_ID)
                .idRestaurante(RESTAURANT_ID)
                .build();

        OrderModel inPreparationOrder = OrderModel.builder()
                .id(ORDER_ID)
                .idCliente(CLIENT_ID)
                .idRestaurante(RESTAURANT_ID)
                .estado(OrderStatus.EN_PREPARACION)
                .build();

        when(employeeRestaurantPersistencePort.findByEmployeeId(EMPLOYEE_ID))
                .thenReturn(Optional.of(employeeRestaurant));
        when(orderPersistencePort.findById(ORDER_ID)).thenReturn(Optional.of(inPreparationOrder));

        DomainException ex = assertThrows(DomainException.class,
                () -> orderUseCase.assignEmployeeToOrder(ORDER_ID, EMPLOYEE_ID));

        assertEquals(OrderErrorMessages.NOT_PENDING, ex.getMessage());
        verify(orderPersistencePort, never()).saveOrder(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el pedido no pertenece al restaurante del empleado")
    void assignEmployeeToOrder_shouldThrowWhenOrderNotBelongsToEmployeeRestaurant() {
        EmployeeRestaurantModel employeeRestaurant = EmployeeRestaurantModel.builder()
                .id(1L)
                .idEmpleado(EMPLOYEE_ID)
                .idRestaurante(RESTAURANT_ID)
                .build();

        OrderModel orderFromOtherRestaurant = OrderModel.builder()
                .id(ORDER_ID)
                .idCliente(CLIENT_ID)
                .idRestaurante(999L) // Otro restaurante
                .estado(OrderStatus.PENDIENTE)
                .build();

        when(employeeRestaurantPersistencePort.findByEmployeeId(EMPLOYEE_ID))
                .thenReturn(Optional.of(employeeRestaurant));
        when(orderPersistencePort.findById(ORDER_ID)).thenReturn(Optional.of(orderFromOtherRestaurant));

        DomainException ex = assertThrows(DomainException.class,
                () -> orderUseCase.assignEmployeeToOrder(ORDER_ID, EMPLOYEE_ID));

        assertEquals(OrderErrorMessages.NOT_BELONGS_TO_RESTAURANT, ex.getMessage());
        verify(orderPersistencePort, never()).saveOrder(any());
    }

    @Test
    @DisplayName("Debe preservar datos del pedido original al asignar empleado")
    void assignEmployeeToOrder_shouldPreserveOriginalOrderData() {
        Instant createdAt = Instant.now().minusSeconds(3600);
        
        EmployeeRestaurantModel employeeRestaurant = EmployeeRestaurantModel.builder()
                .id(1L)
                .idEmpleado(EMPLOYEE_ID)
                .idRestaurante(RESTAURANT_ID)
                .build();

        OrderModel pendingOrder = OrderModel.builder()
                .id(ORDER_ID)
                .idCliente(CLIENT_ID)
                .idRestaurante(RESTAURANT_ID)
                .estado(OrderStatus.PENDIENTE)
                .creadoEn(createdAt)
                .items(Arrays.asList(orderItemModel))
                .build();

        when(employeeRestaurantPersistencePort.findByEmployeeId(EMPLOYEE_ID))
                .thenReturn(Optional.of(employeeRestaurant));
        when(orderPersistencePort.findById(ORDER_ID)).thenReturn(Optional.of(pendingOrder));
        when(orderPersistencePort.saveOrder(any())).thenAnswer(inv -> inv.getArgument(0));

        OrderModel result = orderUseCase.assignEmployeeToOrder(ORDER_ID, EMPLOYEE_ID);

        assertEquals(CLIENT_ID, result.getIdCliente());
        assertEquals(RESTAURANT_ID, result.getIdRestaurante());
        assertEquals(createdAt, result.getCreadoEn());
        assertNotNull(result.getItems());
    }

    // =========================================================
    // Tests para markOrderAsReady
    // =========================================================

    @Test
    @DisplayName("Debe marcar pedido como LISTO cuando está en preparación")
    void markOrderAsReady_shouldMarkWhenInPreparation() {
        EmployeeRestaurantModel employeeRestaurant = EmployeeRestaurantModel.builder()
                .id(1L)
                .idEmpleado(EMPLOYEE_ID)
                .idRestaurante(RESTAURANT_ID)
                .build();

        OrderModel inPreparationOrder = OrderModel.builder()
                .id(ORDER_ID)
                .idCliente(CLIENT_ID)
                .idRestaurante(RESTAURANT_ID)
                .idChef(EMPLOYEE_ID)
                .estado(OrderStatus.EN_PREPARACION)
                .build();

        UserModel client = UserModel.builder()
                .id(CLIENT_ID)
                .celular("+573001234567")
                .build();

        when(employeeRestaurantPersistencePort.findByEmployeeId(EMPLOYEE_ID))
                .thenReturn(Optional.of(employeeRestaurant));
        when(orderPersistencePort.findById(ORDER_ID)).thenReturn(Optional.of(inPreparationOrder));
        when(orderPersistencePort.saveOrder(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userPersistencePort.findUserById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(restaurantPersistencePort.findRestaurantById(RESTAURANT_ID)).thenReturn(Optional.of(restaurantModel));

        OrderModel result = orderUseCase.markOrderAsReady(ORDER_ID, EMPLOYEE_ID);

        assertEquals(OrderStatus.LISTO, result.getEstado());
        assertNotNull(result.getPin());
        assertEquals(6, result.getPin().length());
        verify(smsNotificationPort).sendOrderReadyNotification(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el pedido no está EN_PREPARACION")
    void markOrderAsReady_shouldThrowWhenNotInPreparation() {
        EmployeeRestaurantModel employeeRestaurant = EmployeeRestaurantModel.builder()
                .id(1L)
                .idEmpleado(EMPLOYEE_ID)
                .idRestaurante(RESTAURANT_ID)
                .build();

        OrderModel pendingOrder = OrderModel.builder()
                .id(ORDER_ID)
                .idCliente(CLIENT_ID)
                .idRestaurante(RESTAURANT_ID)
                .estado(OrderStatus.PENDIENTE)
                .build();

        when(employeeRestaurantPersistencePort.findByEmployeeId(EMPLOYEE_ID))
                .thenReturn(Optional.of(employeeRestaurant));
        when(orderPersistencePort.findById(ORDER_ID)).thenReturn(Optional.of(pendingOrder));

        DomainException ex = assertThrows(DomainException.class,
                () -> orderUseCase.markOrderAsReady(ORDER_ID, EMPLOYEE_ID));

        assertEquals(OrderErrorMessages.NOT_IN_PREPARATION, ex.getMessage());
        verify(orderPersistencePort, never()).saveOrder(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el pedido no pertenece al restaurante del empleado")
    void markOrderAsReady_shouldThrowWhenOrderNotBelongsToEmployeeRestaurant() {
        EmployeeRestaurantModel employeeRestaurant = EmployeeRestaurantModel.builder()
                .id(1L)
                .idEmpleado(EMPLOYEE_ID)
                .idRestaurante(RESTAURANT_ID)
                .build();

        OrderModel orderFromOtherRestaurant = OrderModel.builder()
                .id(ORDER_ID)
                .idCliente(CLIENT_ID)
                .idRestaurante(999L)
                .estado(OrderStatus.EN_PREPARACION)
                .build();

        when(employeeRestaurantPersistencePort.findByEmployeeId(EMPLOYEE_ID))
                .thenReturn(Optional.of(employeeRestaurant));
        when(orderPersistencePort.findById(ORDER_ID)).thenReturn(Optional.of(orderFromOtherRestaurant));

        DomainException ex = assertThrows(DomainException.class,
                () -> orderUseCase.markOrderAsReady(ORDER_ID, EMPLOYEE_ID));

        assertEquals(OrderErrorMessages.NOT_BELONGS_TO_RESTAURANT, ex.getMessage());
        verify(orderPersistencePort, never()).saveOrder(any());
    }

    // =========================================================
    // Tests para listOrdersByStatus
    // =========================================================

    @Test
    @DisplayName("Debe listar pedidos por estado exitosamente")
    void listOrdersByStatus_shouldListOrdersSuccessfully() {
        // Arrange
        OrderStatus statusToSearch = OrderStatus.PENDIENTE;
        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);

        EmployeeRestaurantModel employeeRestaurant = EmployeeRestaurantModel.builder()
                .id(1L)
                .idEmpleado(EMPLOYEE_ID)
                .idRestaurante(RESTAURANT_ID)
                .build();

        // Simulamos una página vacía o con datos, lo importante es que se llame al puerto correcto
        org.springframework.data.domain.Page<OrderModel> expectedPage = org.springframework.data.domain.Page.empty();

        when(employeeRestaurantPersistencePort.findByEmployeeId(EMPLOYEE_ID))
                .thenReturn(Optional.of(employeeRestaurant));
        when(orderPersistencePort.findByRestaurantIdAndStatus(RESTAURANT_ID, statusToSearch, pageable))
                .thenReturn(expectedPage);

        // Act
        org.springframework.data.domain.Page<OrderModel> result =
                orderUseCase.listOrdersByStatus(statusToSearch, EMPLOYEE_ID, pageable);

        // Assert
        assertNotNull(result);
        verify(employeeRestaurantPersistencePort).findByEmployeeId(EMPLOYEE_ID);
        verify(orderPersistencePort).findByRestaurantIdAndStatus(RESTAURANT_ID, statusToSearch, pageable);
    }

    @Test
    @DisplayName("Debe lanzar excepción al listar si el empleado no pertenece a un restaurante")
    void listOrdersByStatus_shouldThrowWhenEmployeeNotBelongsToRestaurant() {
        // Arrange
        OrderStatus statusToSearch = OrderStatus.PENDIENTE;
        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);

        when(employeeRestaurantPersistencePort.findByEmployeeId(EMPLOYEE_ID))
                .thenReturn(Optional.empty());

        // Act & Assert
        DomainException ex = assertThrows(DomainException.class,
                () -> orderUseCase.listOrdersByStatus(statusToSearch, EMPLOYEE_ID, pageable));

        assertEquals(EmployeeErrorMessages.NOT_BELONGS_TO_RESTAURANT, ex.getMessage());
        verify(orderPersistencePort, never()).findByRestaurantIdAndStatus(any(), any(), any());
    }

    // =========================================================
    // Tests para markOrderAsEntregado
    // =========================================================

    @Test
    @DisplayName("Debe marcar pedido como ENTREGADO exitosamente cuando el PIN es correcto")
    void markOrderAsEntregado_shouldMarkAsDeliveredWhenPinIsCorrect() {
        // Arrange
        String validPin = "123456";
        EmployeeRestaurantModel employeeRestaurant = EmployeeRestaurantModel.builder()
                .id(1L)
                .idEmpleado(EMPLOYEE_ID)
                .idRestaurante(RESTAURANT_ID)
                .build();

        OrderModel readyOrder = OrderModel.builder()
                .id(ORDER_ID)
                .idCliente(CLIENT_ID)
                .idRestaurante(RESTAURANT_ID)
                .idChef(EMPLOYEE_ID)
                .estado(OrderStatus.LISTO)
                .pin(validPin)
                .build();

        when(employeeRestaurantPersistencePort.findByEmployeeId(EMPLOYEE_ID))
                .thenReturn(Optional.of(employeeRestaurant));
        when(orderPersistencePort.findById(ORDER_ID)).thenReturn(Optional.of(readyOrder));
        when(orderPersistencePort.saveOrder(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        OrderModel result = orderUseCase.markOrderAsDelivered(ORDER_ID, EMPLOYEE_ID, validPin);

        // Assert
        assertNotNull(result);
        assertEquals(OrderStatus.ENTREGADO, result.getEstado());
        assertEquals(validPin, result.getPin()); // Verifica que el PIN se mantuvo
        verify(orderPersistencePort).saveOrder(any(OrderModel.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al entregar si el PIN es incorrecto")
    void markOrderAsDelivered_shouldThrowWhenPinIsIncorrect() {
        // Arrange
        String correctPin = "123456";
        String wrongPin = "999999";
        EmployeeRestaurantModel employeeRestaurant = EmployeeRestaurantModel.builder()
                .id(1L)
                .idEmpleado(EMPLOYEE_ID)
                .idRestaurante(RESTAURANT_ID)
                .build();

        OrderModel readyOrder = OrderModel.builder()
                .id(ORDER_ID)
                .idCliente(CLIENT_ID)
                .idRestaurante(RESTAURANT_ID)
                .estado(OrderStatus.LISTO)
                .pin(correctPin)
                .build();

        when(employeeRestaurantPersistencePort.findByEmployeeId(EMPLOYEE_ID))
                .thenReturn(Optional.of(employeeRestaurant));
        when(orderPersistencePort.findById(ORDER_ID)).thenReturn(Optional.of(readyOrder));

        // Act & Assert
        DomainException ex = assertThrows(DomainException.class,
                () -> orderUseCase.markOrderAsDelivered(ORDER_ID, EMPLOYEE_ID, wrongPin));

        assertEquals(OrderErrorMessages.INVALID_PIN, ex.getMessage());
        verify(orderPersistencePort, never()).saveOrder(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción al entregar si el PIN de la orden es nulo")
    void markOrderAsEntregado_shouldThrowWhenOrderPinIsNull() {
        // Arrange
        String providedPin = "123456";
        EmployeeRestaurantModel employeeRestaurant = EmployeeRestaurantModel.builder()
                .id(1L)
                .idEmpleado(EMPLOYEE_ID)
                .idRestaurante(RESTAURANT_ID)
                .build();

        OrderModel readyOrder = OrderModel.builder()
                .id(ORDER_ID)
                .idCliente(CLIENT_ID)
                .idRestaurante(RESTAURANT_ID)
                .estado(OrderStatus.LISTO)
                .pin(null) // PIN nulo en base de datos
                .build();

        when(employeeRestaurantPersistencePort.findByEmployeeId(EMPLOYEE_ID))
                .thenReturn(Optional.of(employeeRestaurant));
        when(orderPersistencePort.findById(ORDER_ID)).thenReturn(Optional.of(readyOrder));

        // Act & Assert
        DomainException ex = assertThrows(DomainException.class,
                () -> orderUseCase.markOrderAsDelivered(ORDER_ID, EMPLOYEE_ID, providedPin));

        assertEquals(OrderErrorMessages.INVALID_PIN, ex.getMessage());
        verify(orderPersistencePort, never()).saveOrder(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción al entregar si la orden no está en estado LISTO")
    void markOrderAsEntregado_shouldThrowWhenOrderIsNotReady() {
        // Arrange
        String pin = "123456";
        EmployeeRestaurantModel employeeRestaurant = EmployeeRestaurantModel.builder()
                .id(1L)
                .idEmpleado(EMPLOYEE_ID)
                .idRestaurante(RESTAURANT_ID)
                .build();

        OrderModel pendingOrder = OrderModel.builder()
                .id(ORDER_ID)
                .idCliente(CLIENT_ID)
                .idRestaurante(RESTAURANT_ID)
                .estado(OrderStatus.PENDIENTE) // Estado incorrecto
                .pin(pin)
                .build();

        when(employeeRestaurantPersistencePort.findByEmployeeId(EMPLOYEE_ID))
                .thenReturn(Optional.of(employeeRestaurant));
        when(orderPersistencePort.findById(ORDER_ID)).thenReturn(Optional.of(pendingOrder));

        // Act & Assert
        DomainException ex = assertThrows(DomainException.class,
                () -> orderUseCase.markOrderAsDelivered(ORDER_ID, EMPLOYEE_ID, pin));

        assertEquals(OrderErrorMessages.ORDER_NOT_READY_FOR_DELIVERY, ex.getMessage());
        verify(orderPersistencePort, never()).saveOrder(any());
    }
}

