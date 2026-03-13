package com.pragma.plazoleta.domain.spi;

import com.pragma.plazoleta.domain.model.OrderModel;
import com.pragma.plazoleta.domain.model.OrderStatus;

import java.util.List;

public interface IOrderPersistencePort {
    OrderModel saveOrder(OrderModel orderModel);
    
    boolean existsActiveOrderByClientId(Long clientId, List<OrderStatus> activeStatuses);
}
