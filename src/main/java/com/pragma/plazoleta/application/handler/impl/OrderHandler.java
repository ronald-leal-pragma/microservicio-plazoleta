package com.pragma.plazoleta.application.handler.impl;

import com.pragma.plazoleta.application.dto.request.OrderRequestDto;
import com.pragma.plazoleta.application.dto.response.OrderItemResponseDto;
import com.pragma.plazoleta.application.dto.response.OrderResponseDto;
import com.pragma.plazoleta.application.handler.IOrderHandler;
import com.pragma.plazoleta.domain.api.IOrderServicePort;
import com.pragma.plazoleta.domain.model.OrderItemModel;
import com.pragma.plazoleta.domain.model.OrderModel;
import com.pragma.plazoleta.domain.model.RestaurantModel;
import com.pragma.plazoleta.domain.spi.IRestaurantPersistencePort;
import com.pragma.plazoleta.infrastructure.configuration.jwt.JwtUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderHandler implements IOrderHandler {

    private final IOrderServicePort orderServicePort;
    private final IRestaurantPersistencePort restaurantPersistencePort;

    @Override
    public OrderResponseDto createOrder(OrderRequestDto orderRequestDto) {
        log.info("[HANDLER] Iniciando creación de pedido para restaurante: {}", 
                orderRequestDto.getIdRestaurante());

        Long idCliente = getAuthenticatedUserId();
        log.debug("[HANDLER] Cliente autenticado: id={}", idCliente);

        // Mapear request a modelo
        OrderModel orderModel = mapToModel(orderRequestDto);

        // Crear pedido
        OrderModel createdOrder = orderServicePort.createOrder(orderModel, idCliente);

        // Obtener nombre del restaurante para la respuesta
        String nombreRestaurante = restaurantPersistencePort.findRestaurantById(orderRequestDto.getIdRestaurante())
                .map(RestaurantModel::getNombre)
                .orElse("Restaurante");

        log.info("[HANDLER] Pedido creado exitosamente: id={}", createdOrder.getId());

        return mapToResponse(createdOrder, nombreRestaurante);
    }

    private OrderModel mapToModel(OrderRequestDto dto) {
        List<OrderItemModel> items = dto.getItems().stream()
                .map(itemDto -> OrderItemModel.builder()
                        .idPlato(itemDto.getIdPlato())
                        .cantidad(itemDto.getCantidad())
                        .build())
                .collect(Collectors.toList());

        return OrderModel.builder()
                .idRestaurante(dto.getIdRestaurante())
                .items(items)
                .build();
    }

    private OrderResponseDto mapToResponse(OrderModel order, String nombreRestaurante) {
        List<OrderItemResponseDto> itemsResponse = order.getItems().stream()
                .map(item -> OrderItemResponseDto.builder()
                        .idPlato(item.getIdPlato())
                        .nombrePlato(item.getNombrePlato())
                        .cantidad(item.getCantidad())
                        .precioUnitario(item.getPrecioPlato())
                        .subtotal(item.getCantidad() * item.getPrecioPlato())
                        .build())
                .collect(Collectors.toList());

        int total = itemsResponse.stream()
                .mapToInt(OrderItemResponseDto::getSubtotal)
                .sum();

        return OrderResponseDto.builder()
                .id(order.getId())
                .idRestaurante(order.getIdRestaurante())
                .nombreRestaurante(nombreRestaurante)
                .estado(order.getEstado().name())
                .items(itemsResponse)
                .total(total)
                .creadoEn(order.getCreadoEn() != null ? order.getCreadoEn().toString() : null)
                .build();
    }

    private Long getAuthenticatedUserId() {
        JwtUserDetails userDetails = (JwtUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return userDetails.getId();
    }
}
