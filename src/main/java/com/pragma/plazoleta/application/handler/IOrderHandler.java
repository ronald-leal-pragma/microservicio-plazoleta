package com.pragma.plazoleta.application.handler;

import com.pragma.plazoleta.application.dto.request.OrderRequestDto;
import com.pragma.plazoleta.application.dto.response.OrderListResponseDto;
import com.pragma.plazoleta.application.dto.response.OrderResponseDto;
import com.pragma.plazoleta.application.dto.response.PaginatedResponseDto;

public interface IOrderHandler {
    OrderResponseDto createOrder(OrderRequestDto orderRequestDto);
    
    PaginatedResponseDto<OrderListResponseDto> listOrdersByStatus(String status, int page, int size);
    
    OrderListResponseDto assignOrderToEmployee(Long orderId);
    
    OrderListResponseDto markOrderAsReady(Long orderId);
}
