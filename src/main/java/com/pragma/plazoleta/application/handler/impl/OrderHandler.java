package com.pragma.plazoleta.application.handler.impl;

import com.pragma.plazoleta.application.dto.request.OrderRequestDto;
import com.pragma.plazoleta.application.dto.response.OrderItemResponseDto;
import com.pragma.plazoleta.application.dto.response.OrderListResponseDto;
import com.pragma.plazoleta.application.dto.response.OrderResponseDto;
import com.pragma.plazoleta.application.dto.response.PaginatedResponseDto;
import com.pragma.plazoleta.application.handler.IOrderHandler;
import com.pragma.plazoleta.domain.api.IOrderServicePort;
import com.pragma.plazoleta.domain.model.OrderItemModel;
import com.pragma.plazoleta.domain.model.OrderModel;
import com.pragma.plazoleta.domain.model.OrderStatus;
import com.pragma.plazoleta.domain.model.RestaurantModel;
import com.pragma.plazoleta.domain.spi.IRestaurantPersistencePort;
import com.pragma.plazoleta.infrastructure.configuration.jwt.JwtUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

        OrderModel orderModel = mapToModel(orderRequestDto);

        OrderModel createdOrder = orderServicePort.createOrder(orderModel, idCliente);

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

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDto<OrderListResponseDto> listOrdersByStatus(String status, int page, int size) {
        log.info("[HANDLER] Listando pedidos por estado: {}, page={}, size={}", status, page, size);

        Long employeeId = getAuthenticatedUserId();
        log.debug("[HANDLER] Empleado autenticado: id={}", employeeId);

        OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());

        Page<OrderModel> orderPage = orderServicePort.listOrdersByStatus(
                orderStatus, employeeId, PageRequest.of(page, size));

        List<OrderListResponseDto> content = orderPage.getContent().stream()
                .map(this::mapToListResponse)
                .collect(Collectors.toList());

        log.info("[HANDLER] Pedidos encontrados: {} de {} total", content.size(), orderPage.getTotalElements());

        return PaginatedResponseDto.<OrderListResponseDto>builder()
                .content(content)
                .pageNumber(orderPage.getNumber())
                .pageSize(orderPage.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .last(orderPage.isLast())
                .build();
    }

    private OrderListResponseDto mapToListResponse(OrderModel order) {
        List<OrderItemResponseDto> itemsResponse = order.getItems() != null
                ? order.getItems().stream()
                .map(item -> OrderItemResponseDto.builder()
                        .idPlato(item.getIdPlato())
                        .nombrePlato(item.getNombrePlato())
                        .cantidad(item.getCantidad())
                        .precioUnitario(item.getPrecioPlato())
                        .subtotal(item.getCantidad() * item.getPrecioPlato())
                        .build())
                .collect(Collectors.toList())
                : List.of();

        int total = itemsResponse.stream()
                .mapToInt(OrderItemResponseDto::getSubtotal)
                .sum();

        return OrderListResponseDto.builder()
                .id(order.getId())
                .idCliente(order.getIdCliente())
                .idRestaurante(order.getIdRestaurante())
                .idChef(order.getIdChef())
                .estado(order.getEstado().name())
                .items(itemsResponse)
                .total(total)
                .creadoEn(order.getCreadoEn() != null ? order.getCreadoEn().toString() : null)
                .actualizadoEn(order.getActualizadoEn() != null ? order.getActualizadoEn().toString() : null)
                .build();
    }

    @Override
    public OrderListResponseDto assignOrderToEmployee(Long orderId) {
        log.info("[HANDLER] Asignando pedido {} al empleado autenticado", orderId);

        Long employeeId = getAuthenticatedUserId();
        log.debug("[HANDLER] Empleado autenticado: id={}", employeeId);

        OrderModel assignedOrder = orderServicePort.assignEmployeeToOrder(orderId, employeeId);

        log.info("[HANDLER] Pedido {} asignado exitosamente al empleado {}", orderId, employeeId);

        return mapToListResponse(assignedOrder);
    }

    @Override
    public OrderListResponseDto markOrderAsReady(Long orderId) {
        log.info("[HANDLER] Marcando pedido {} como LISTO", orderId);

        Long employeeId = getAuthenticatedUserId();
        log.debug("[HANDLER] Empleado autenticado: id={}", employeeId);

        OrderModel readyOrder = orderServicePort.markOrderAsReady(orderId, employeeId);

        log.info("[HANDLER] Pedido {} marcado como LISTO. PIN: {}", orderId, readyOrder.getPin());

        return mapToListResponseWithPin(readyOrder);
    }

    @Override
    public OrderListResponseDto markOrderAsDelivered(Long orderId, String pin) {
        log.info("[HANDLER] Marcando pedido {} como ENTREGADO", orderId);

        Long employeeId = getAuthenticatedUserId();
        log.debug("[HANDLER] Empleado autenticado: id={}", employeeId);

        OrderModel updateOrder = orderServicePort.markOrderAsDelivered(orderId, employeeId, pin);

        log.info("[HANDLER] Pedido {} marcado como ENTREGADO.", orderId, updateOrder.getPin());

        return mapToListResponseWithPin(updateOrder);
    }

    private OrderListResponseDto mapToListResponseWithPin(OrderModel order) {
        OrderListResponseDto response = mapToListResponse(order);
        response.setPin(order.getPin());
        return response;
    }

    private Long getAuthenticatedUserId() {
        JwtUserDetails userDetails = (JwtUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return userDetails.id();
    }
}
