package com.pragma.plazoleta.domain.api;

import com.pragma.plazoleta.domain.model.OrderModel;
import com.pragma.plazoleta.domain.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IOrderServicePort {
    OrderModel createOrder(OrderModel orderModel, Long idCliente);
    
    Page<OrderModel> listOrdersByStatus(OrderStatus status, Long employeeId, Pageable pageable);
    
    OrderModel assignEmployeeToOrder(Long orderId, Long employeeId);
    
    OrderModel markOrderAsReady(Long orderId, Long employeeId);
    OrderModel markOrderAsDelivered(Long orderId, Long employeeId, String pin);
}
