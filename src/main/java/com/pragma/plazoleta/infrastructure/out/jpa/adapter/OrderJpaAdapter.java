package com.pragma.plazoleta.infrastructure.out.jpa.adapter;

import com.pragma.plazoleta.domain.model.OrderItemModel;
import com.pragma.plazoleta.domain.model.OrderModel;
import com.pragma.plazoleta.domain.model.OrderStatus;
import com.pragma.plazoleta.domain.spi.IOrderPersistencePort;
import com.pragma.plazoleta.infrastructure.out.jpa.entity.OrderEntity;
import com.pragma.plazoleta.infrastructure.out.jpa.entity.OrderItemEntity;
import com.pragma.plazoleta.infrastructure.out.jpa.mapper.IOrderEntityMapper;
import com.pragma.plazoleta.infrastructure.out.jpa.repository.IOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class OrderJpaAdapter implements IOrderPersistencePort {

    private final IOrderRepository orderRepository;
    private final IOrderEntityMapper orderEntityMapper;

    @Override
    public OrderModel saveOrder(OrderModel orderModel) {
        log.debug("[JPA ADAPTER] Guardando pedido para cliente: {}", orderModel.getIdCliente());

        OrderEntity orderEntity = orderEntityMapper.toEntity(orderModel);

        if (orderModel.getItems() != null) {
            for (OrderItemModel itemModel : orderModel.getItems()) {
                OrderItemEntity itemEntity = new OrderItemEntity();
                itemEntity.setIdPlato(itemModel.getIdPlato());
                itemEntity.setCantidad(itemModel.getCantidad());
                itemEntity.setNombrePlato(itemModel.getNombrePlato());
                itemEntity.setPrecioPlato(itemModel.getPrecioPlato());
                orderEntity.addItem(itemEntity);
            }
        }

        OrderEntity saved = orderRepository.save(orderEntity);

        log.info("[JPA ADAPTER] Pedido guardado exitosamente: id={}, estado={}", 
                saved.getId(), saved.getEstado());

        return orderEntityMapper.toModel(saved);
    }

    @Override
    public boolean existsActiveOrderByClientId(Long clientId, List<OrderStatus> activeStatuses) {
        log.debug("[JPA ADAPTER] Verificando pedidos activos para cliente: {}", clientId);
        return orderRepository.existsByIdClienteAndEstadoIn(clientId, activeStatuses);
    }

    @Override
    public Page<OrderModel> findByRestaurantIdAndStatus(Long restaurantId, OrderStatus status, Pageable pageable) {
        log.debug("[JPA ADAPTER] Buscando pedidos: restaurante={}, estado={}, page={}", 
                restaurantId, status, pageable.getPageNumber());
        
        Page<OrderEntity> orderEntities = orderRepository.findByIdRestauranteAndEstado(
                restaurantId, status, pageable);
        
        log.debug("[JPA ADAPTER] Encontrados {} pedidos de {} total", 
                orderEntities.getNumberOfElements(), orderEntities.getTotalElements());
        
        return orderEntities.map(orderEntityMapper::toModel);
    }
}
