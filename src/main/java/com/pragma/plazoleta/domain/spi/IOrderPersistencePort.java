package com.pragma.plazoleta.domain.spi;

import com.pragma.plazoleta.domain.model.OrderModel;
import com.pragma.plazoleta.domain.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface IOrderPersistencePort {
    OrderModel saveOrder(OrderModel orderModel);
    
    boolean existsActiveOrderByClientId(Long clientId, List<OrderStatus> activeStatuses);
    
    Page<OrderModel> findByRestaurantIdAndStatus(Long restaurantId, OrderStatus status, Pageable pageable);
    
    Optional<OrderModel> findById(Long orderId);
}
