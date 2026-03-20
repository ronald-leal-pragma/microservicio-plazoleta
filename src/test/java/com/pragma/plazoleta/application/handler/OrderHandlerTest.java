package com.pragma.plazoleta.application.handler;

import com.pragma.plazoleta.application.dto.request.OrderItemRequestDto;
import com.pragma.plazoleta.application.dto.request.OrderRequestDto;
import com.pragma.plazoleta.application.dto.response.OrderListResponseDto;
import com.pragma.plazoleta.application.dto.response.OrderResponseDto;
import com.pragma.plazoleta.application.handler.impl.OrderHandler;
import com.pragma.plazoleta.domain.api.IOrderServicePort;
import com.pragma.plazoleta.domain.model.OrderItemModel;
import com.pragma.plazoleta.domain.model.OrderModel;
import com.pragma.plazoleta.domain.model.OrderStatus;
import com.pragma.plazoleta.domain.model.RestaurantModel;
import com.pragma.plazoleta.domain.spi.IRestaurantPersistencePort;
import com.pragma.plazoleta.infrastructure.configuration.jwt.JwtUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderHandlerTest {

    @Mock
    private IOrderServicePort orderServicePort;

    @Mock
    private IRestaurantPersistencePort restaurantPersistencePort;

    @InjectMocks
    private OrderHandler orderHandler;

    private static final Long CLIENT_ID = 1L;
    private static final Long EMPLOYEE_ID = 10L;
    private static final Long RESTAURANT_ID = 2L;
    private static final Long ORDER_ID = 100L;

    private OrderRequestDto orderRequestDto;
    private OrderModel savedOrder;
    private RestaurantModel restaurantModel;

    @BeforeEach
    void setUp() {
        JwtUserDetails userDetails = new JwtUserDetails(CLIENT_ID, "client@mail.com", "CLIENTE");
        Authentication authentication = mock(Authentication.class);
        lenient().when(authentication.getPrincipal()).thenReturn(userDetails);
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        OrderItemRequestDto itemDto = new OrderItemRequestDto();
        itemDto.setIdPlato(3L);
        itemDto.setCantidad(2);

        orderRequestDto = new OrderRequestDto();
        orderRequestDto.setIdRestaurante(RESTAURANT_ID);
        orderRequestDto.setItems(List.of(itemDto));

        OrderItemModel orderItem = OrderItemModel.builder()
                .idPlato(3L)
                .cantidad(2)
                .nombrePlato("Bandeja Paisa")
                .precioPlato(25000)
                .build();

        savedOrder = OrderModel.builder()
                .id(100L)
                .idCliente(CLIENT_ID)
                .idRestaurante(RESTAURANT_ID)
                .estado(OrderStatus.PENDIENTE)
                .creadoEn(Instant.now())
                .items(List.of(orderItem))
                .build();

        restaurantModel = new RestaurantModel();
        restaurantModel.setId(RESTAURANT_ID);
        restaurantModel.setNombre("Restaurante Test");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Debe retornar OrderResponseDto cuando el pedido es creado exitosamente")
    void createOrder_shouldReturnResponseDtoWhenCreated() {
        when(orderServicePort.createOrder(any(), eq(CLIENT_ID))).thenReturn(savedOrder);
        when(restaurantPersistencePort.findRestaurantById(RESTAURANT_ID))
                .thenReturn(Optional.of(restaurantModel));

        OrderResponseDto result = orderHandler.createOrder(orderRequestDto);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(RESTAURANT_ID, result.getIdRestaurante());
        assertEquals("PENDIENTE", result.getEstado());
        assertEquals("Restaurante Test", result.getNombreRestaurante());
        assertNotNull(result.getCreadoEn());
    }

    @Test
    @DisplayName("Debe calcular el total correcto sumando subtotales de los items")
    void createOrder_shouldCalculateTotalCorrectly() {
        when(orderServicePort.createOrder(any(), any())).thenReturn(savedOrder);
        when(restaurantPersistencePort.findRestaurantById(any()))
                .thenReturn(Optional.of(restaurantModel));

        OrderResponseDto result = orderHandler.createOrder(orderRequestDto);

        assertEquals(50000, result.getTotal());
    }

    @Test
    @DisplayName("Debe mapear correctamente los items en la respuesta")
    void createOrder_shouldMapItemsCorrectly() {
        when(orderServicePort.createOrder(any(), any())).thenReturn(savedOrder);
        when(restaurantPersistencePort.findRestaurantById(any()))
                .thenReturn(Optional.of(restaurantModel));

        OrderResponseDto result = orderHandler.createOrder(orderRequestDto);

        assertNotNull(result.getItems());
        assertEquals(1, result.getItems().size());
        assertEquals("Bandeja Paisa", result.getItems().get(0).getNombrePlato());
        assertEquals(2, result.getItems().get(0).getCantidad());
        assertEquals(25000, result.getItems().get(0).getPrecioUnitario());
        assertEquals(50000, result.getItems().get(0).getSubtotal());
    }

    @Test
    @DisplayName("Debe usar nombre por defecto 'Restaurante' cuando el restaurante no es encontrado")
    void createOrder_shouldUseDefaultRestaurantNameWhenNotFound() {
        when(orderServicePort.createOrder(any(), any())).thenReturn(savedOrder);
        when(restaurantPersistencePort.findRestaurantById(any())).thenReturn(Optional.empty());

        OrderResponseDto result = orderHandler.createOrder(orderRequestDto);

        assertEquals("Restaurante", result.getNombreRestaurante());
    }

    @Test
    @DisplayName("Debe llamar al servicio con el ID del cliente autenticado")
    void createOrder_shouldCallServiceWithAuthenticatedClientId() {
        when(orderServicePort.createOrder(any(), eq(CLIENT_ID))).thenReturn(savedOrder);
        when(restaurantPersistencePort.findRestaurantById(any()))
                .thenReturn(Optional.of(restaurantModel));

        orderHandler.createOrder(orderRequestDto);

        verify(orderServicePort).createOrder(any(OrderModel.class), eq(CLIENT_ID));
    }

    @Test
    @DisplayName("Debe retornar creadoEn nulo cuando el pedido no tiene fecha")
    void createOrder_shouldReturnNullCreadoEnWhenDateIsNull() {
        savedOrder.setCreadoEn(null);
        when(orderServicePort.createOrder(any(), any())).thenReturn(savedOrder);
        when(restaurantPersistencePort.findRestaurantById(any()))
                .thenReturn(Optional.of(restaurantModel));

        OrderResponseDto result = orderHandler.createOrder(orderRequestDto);

        assertNull(result.getCreadoEn());
    }

    // Tests para assignOrderToEmployee

    @Test
    @DisplayName("Debe asignar pedido al empleado y retornar OrderListResponseDto")
    void assignOrderToEmployee_shouldAssignAndReturnDto() {
        // Reconfigurar contexto de seguridad con empleado
        SecurityContextHolder.clearContext();
        JwtUserDetails employeeDetails = new JwtUserDetails(EMPLOYEE_ID, "employee@mail.com", "EMPLEADO");
        Authentication employeeAuth = mock(Authentication.class);
        lenient().when(employeeAuth.getPrincipal()).thenReturn(employeeDetails);
        SecurityContext employeeSecurityContext = mock(SecurityContext.class);
        lenient().when(employeeSecurityContext.getAuthentication()).thenReturn(employeeAuth);
        SecurityContextHolder.setContext(employeeSecurityContext);

        OrderItemModel orderItem = OrderItemModel.builder()
                .idPlato(3L)
                .cantidad(2)
                .nombrePlato("Bandeja Paisa")
                .precioPlato(25000)
                .build();

        OrderModel assignedOrder = OrderModel.builder()
                .id(ORDER_ID)
                .idCliente(CLIENT_ID)
                .idRestaurante(RESTAURANT_ID)
                .idChef(EMPLOYEE_ID)
                .estado(OrderStatus.EN_PREPARACION)
                .creadoEn(Instant.now())
                .actualizadoEn(Instant.now())
                .items(List.of(orderItem))
                .build();

        when(orderServicePort.assignEmployeeToOrder(ORDER_ID, EMPLOYEE_ID)).thenReturn(assignedOrder);

        OrderListResponseDto result = orderHandler.assignOrderToEmployee(ORDER_ID);

        assertNotNull(result);
        assertEquals(ORDER_ID, result.getId());
        assertEquals(EMPLOYEE_ID, result.getIdChef());
        assertEquals("EN_PREPARACION", result.getEstado());
        assertEquals(CLIENT_ID, result.getIdCliente());
        assertEquals(RESTAURANT_ID, result.getIdRestaurante());
        verify(orderServicePort).assignEmployeeToOrder(ORDER_ID, EMPLOYEE_ID);
    }

    @Test
    @DisplayName("Debe mapear correctamente los items al asignar pedido")
    void assignOrderToEmployee_shouldMapItemsCorrectly() {
        // Reconfigurar contexto de seguridad con empleado
        SecurityContextHolder.clearContext();
        JwtUserDetails employeeDetails = new JwtUserDetails(EMPLOYEE_ID, "employee@mail.com", "EMPLEADO");
        Authentication employeeAuth = mock(Authentication.class);
        lenient().when(employeeAuth.getPrincipal()).thenReturn(employeeDetails);
        SecurityContext employeeSecurityContext = mock(SecurityContext.class);
        lenient().when(employeeSecurityContext.getAuthentication()).thenReturn(employeeAuth);
        SecurityContextHolder.setContext(employeeSecurityContext);

        OrderItemModel orderItem = OrderItemModel.builder()
                .idPlato(3L)
                .cantidad(2)
                .nombrePlato("Bandeja Paisa")
                .precioPlato(25000)
                .build();

        OrderModel assignedOrder = OrderModel.builder()
                .id(ORDER_ID)
                .idCliente(CLIENT_ID)
                .idRestaurante(RESTAURANT_ID)
                .idChef(EMPLOYEE_ID)
                .estado(OrderStatus.EN_PREPARACION)
                .items(List.of(orderItem))
                .build();

        when(orderServicePort.assignEmployeeToOrder(ORDER_ID, EMPLOYEE_ID)).thenReturn(assignedOrder);

        OrderListResponseDto result = orderHandler.assignOrderToEmployee(ORDER_ID);

        assertNotNull(result.getItems());
        assertEquals(1, result.getItems().size());
        assertEquals("Bandeja Paisa", result.getItems().get(0).getNombrePlato());
        assertEquals(50000, result.getTotal());
    }
}

