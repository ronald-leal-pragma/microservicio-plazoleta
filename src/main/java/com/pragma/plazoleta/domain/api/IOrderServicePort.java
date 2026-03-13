package com.pragma.plazoleta.domain.api;

import com.pragma.plazoleta.domain.model.OrderModel;

public interface IOrderServicePort {
    OrderModel createOrder(OrderModel orderModel, Long idCliente);
}
