package com.pragma.plazoleta.application.handler;

import com.pragma.plazoleta.application.dto.request.OrderRequestDto;
import com.pragma.plazoleta.application.dto.response.OrderResponseDto;

public interface IOrderHandler {
    OrderResponseDto createOrder(OrderRequestDto orderRequestDto);
}
