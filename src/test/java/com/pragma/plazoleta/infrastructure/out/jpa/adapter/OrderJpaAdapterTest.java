package com.pragma.plazoleta.infrastructure.out.jpa.adapter;

import com.pragma.plazoleta.domain.model.OrderModel;
import com.pragma.plazoleta.domain.model.OrderStatus;
import com.pragma.plazoleta.infrastructure.out.jpa.entity.OrderEntity;
import com.pragma.plazoleta.infrastructure.out.jpa.mapper.IOrderEntityMapper;
import com.pragma.plazoleta.infrastructure.out.jpa.repository.IOrderRepository;
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

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderJpaAdapterTest {

    @Mock
    private IOrderRepository orderRepository;

    @Mock
    private IOrderEntityMapper orderEntityMapper;

    @InjectMocks
    private OrderJpaAdapter orderJpaAdapter;

    private OrderModel orderModel;
    private OrderEntity orderEntity;

    @BeforeEach
    void setUp() {
        orderModel = OrderModel.builder()
                .idCliente(1L)
                .idRestaurante(2L)
                .estado(OrderStatus.PENDIENTE)
                .build();

        orderEntity = new OrderEntity();
        orderEntity.setId(100L);
        orderEntity.setIdCliente(1L);
        orderEntity.setIdRestaurante(2L);
        orderEntity.setEstado(OrderStatus.PENDIENTE);
    }

    // =========================================================
    // saveOrder
    // =========================================================

    @Test
    @DisplayName("Debe guardar el pedido y retornar el modelo mapeado")
    void saveOrder_shouldSaveAndReturnMappedModel() {
        OrderModel expectedResult = OrderModel.builder()
                .id(100L)
                .idCliente(1L)
                .estado(OrderStatus.PENDIENTE)
                .build();

        when(orderEntityMapper.toEntity(orderModel)).thenReturn(orderEntity);
        when(orderRepository.save(orderEntity)).thenReturn(orderEntity);
        when(orderEntityMapper.toModel(orderEntity)).thenReturn(expectedResult);

        OrderModel result = orderJpaAdapter.saveOrder(orderModel);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(OrderStatus.PENDIENTE, result.getEstado());
        verify(orderRepository).save(orderEntity);
    }

    @Test
    @DisplayName("Debe delegar el mapeo completo al mapper incluyendo items")
    void saveOrder_shouldDelegateFullMappingToMapper() {
        when(orderEntityMapper.toEntity(orderModel)).thenReturn(orderEntity);
        when(orderRepository.save(orderEntity)).thenReturn(orderEntity);
        when(orderEntityMapper.toModel(orderEntity)).thenReturn(orderModel);

        orderJpaAdapter.saveOrder(orderModel);

        verify(orderEntityMapper).toEntity(orderModel);
        verify(orderRepository).save(orderEntity);
        verify(orderEntityMapper).toModel(orderEntity);
    }

    // =========================================================
    // existsActiveOrderByClientId
    // =========================================================

    @Test
    @DisplayName("Debe retornar true cuando el cliente tiene un pedido activo")
    void existsActiveOrderByClientId_shouldReturnTrueWhenActiveOrderExists() {
        List<OrderStatus> activeStatuses = Arrays.asList(
                OrderStatus.PENDIENTE, OrderStatus.EN_PREPARACION, OrderStatus.LISTO);
        when(orderRepository.existsByIdClienteAndEstadoIn(1L, activeStatuses)).thenReturn(true);

        boolean result = orderJpaAdapter.existsActiveOrderByClientId(1L, activeStatuses);

        assertTrue(result);
    }

    @Test
    @DisplayName("Debe retornar false cuando el cliente no tiene pedidos activos")
    void existsActiveOrderByClientId_shouldReturnFalseWhenNoActiveOrders() {
        List<OrderStatus> activeStatuses = Arrays.asList(
                OrderStatus.PENDIENTE, OrderStatus.EN_PREPARACION, OrderStatus.LISTO);
        when(orderRepository.existsByIdClienteAndEstadoIn(99L, activeStatuses)).thenReturn(false);

        boolean result = orderJpaAdapter.existsActiveOrderByClientId(99L, activeStatuses);

        assertFalse(result);
    }

    // =========================================================
    // findByRestaurantIdAndStatus
    // =========================================================

    @Test
    @DisplayName("Debe retornar página de pedidos filtrados por restaurante y estado")
    void findByRestaurantIdAndStatus_shouldReturnFilteredOrders() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderEntity> entityPage = new PageImpl<>(List.of(orderEntity));
        OrderModel mappedOrder = OrderModel.builder().id(100L).estado(OrderStatus.PENDIENTE).build();

        when(orderRepository.findByIdRestauranteAndEstado(2L, OrderStatus.PENDIENTE, pageable))
                .thenReturn(entityPage);
        when(orderEntityMapper.toModel(orderEntity)).thenReturn(mappedOrder);

        Page<OrderModel> result = orderJpaAdapter.findByRestaurantIdAndStatus(2L, OrderStatus.PENDIENTE, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(100L, result.getContent().get(0).getId());
    }

    @Test
    @DisplayName("Debe retornar página vacía cuando no hay pedidos con ese estado")
    void findByRestaurantIdAndStatus_shouldReturnEmptyWhenNoOrders() {
        Pageable pageable = PageRequest.of(0, 10);
        when(orderRepository.findByIdRestauranteAndEstado(2L, OrderStatus.ENTREGADO, pageable))
                .thenReturn(new PageImpl<>(List.of()));

        Page<OrderModel> result = orderJpaAdapter.findByRestaurantIdAndStatus(2L, OrderStatus.ENTREGADO, pageable);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
    }
}
