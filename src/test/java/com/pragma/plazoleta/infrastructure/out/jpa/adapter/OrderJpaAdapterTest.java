package com.pragma.plazoleta.infrastructure.out.jpa.adapter;

import com.pragma.plazoleta.domain.model.OrderItemModel;
import com.pragma.plazoleta.domain.model.OrderModel;
import com.pragma.plazoleta.domain.model.OrderStatus;
import com.pragma.plazoleta.infrastructure.out.jpa.entity.OrderEntity;
import com.pragma.plazoleta.infrastructure.out.jpa.entity.OrderItemEntity;
import com.pragma.plazoleta.infrastructure.out.jpa.mapper.IOrderEntityMapper;
import com.pragma.plazoleta.infrastructure.out.jpa.repository.IOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
        OrderItemModel item = OrderItemModel.builder()
                .idPlato(3L)
                .cantidad(2)
                .nombrePlato("Bandeja Paisa")
                .precioPlato(25000)
                .build();

        orderModel = OrderModel.builder()
                .idCliente(1L)
                .idRestaurante(2L)
                .estado(OrderStatus.PENDIENTE)
                .items(Arrays.asList(item))
                .build();

        orderEntity = new OrderEntity();
        orderEntity.setId(100L);
        orderEntity.setIdCliente(1L);
        orderEntity.setIdRestaurante(2L);
        orderEntity.setEstado(OrderStatus.PENDIENTE);
    }

    @Test
    @DisplayName("Debe guardar el pedido con sus items y retornar el modelo mapeado")
    void saveOrder_shouldSaveOrderWithItemsAndReturnMappedModel() {
        OrderModel expectedResult = OrderModel.builder()
                .id(100L)
                .idCliente(1L)
                .estado(OrderStatus.PENDIENTE)
                .build();

        when(orderEntityMapper.toEntity(orderModel)).thenReturn(orderEntity);
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(orderEntity);
        when(orderEntityMapper.toModel(orderEntity)).thenReturn(expectedResult);

        OrderModel result = orderJpaAdapter.saveOrder(orderModel);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(OrderStatus.PENDIENTE, result.getEstado());
        verify(orderRepository).save(orderEntity);
    }

    @Test
    @DisplayName("Debe agregar los items a la entidad antes de guardar")
    void saveOrder_shouldAddItemsToEntityBeforeSaving() {
        when(orderEntityMapper.toEntity(orderModel)).thenReturn(orderEntity);
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(orderEntity);
        when(orderEntityMapper.toModel(orderEntity)).thenReturn(orderModel);

        orderJpaAdapter.saveOrder(orderModel);

        verify(orderRepository).save(argThat(entity ->
                entity.getItems() != null && !entity.getItems().isEmpty()
        ));
    }

    @Test
    @DisplayName("Debe guardar pedido sin items cuando la lista es nula")
    void saveOrder_shouldSaveOrderWithNullItems() {
        orderModel.setItems(null);
        when(orderEntityMapper.toEntity(orderModel)).thenReturn(orderEntity);
        when(orderRepository.save(orderEntity)).thenReturn(orderEntity);
        when(orderEntityMapper.toModel(orderEntity)).thenReturn(orderModel);

        OrderModel result = orderJpaAdapter.saveOrder(orderModel);

        assertNotNull(result);
        verify(orderRepository).save(orderEntity);
    }

    @Test
    @DisplayName("Debe guardar pedido con lista de items vacía")
    void saveOrder_shouldSaveOrderWithEmptyItems() {
        orderModel.setItems(List.of());
        when(orderEntityMapper.toEntity(orderModel)).thenReturn(orderEntity);
        when(orderRepository.save(orderEntity)).thenReturn(orderEntity);
        when(orderEntityMapper.toModel(orderEntity)).thenReturn(orderModel);

        OrderModel result = orderJpaAdapter.saveOrder(orderModel);

        assertNotNull(result);
    }


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
}

