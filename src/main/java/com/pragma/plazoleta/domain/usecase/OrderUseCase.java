package com.pragma.plazoleta.domain.usecase;

import com.pragma.plazoleta.domain.api.IOrderServicePort;
import com.pragma.plazoleta.domain.exception.DomainException;
import com.pragma.plazoleta.domain.exception.message.EmployeeErrorMessages;
import com.pragma.plazoleta.domain.exception.message.OrderErrorMessages;
import com.pragma.plazoleta.domain.exception.message.PlateErrorMessages;
import com.pragma.plazoleta.domain.exception.message.RestaurantErrorMessages;
import com.pragma.plazoleta.domain.model.*;
import com.pragma.plazoleta.domain.spi.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Slf4j
@RequiredArgsConstructor
public class OrderUseCase implements IOrderServicePort {

    private static final List<OrderStatus> ACTIVE_ORDER_STATUSES = Arrays.asList(
            OrderStatus.PENDIENTE,
            OrderStatus.EN_PREPARACION,
            OrderStatus.LISTO
    );

    private final IOrderPersistencePort orderPersistencePort;
    private final IRestaurantPersistencePort restaurantPersistencePort;
    private final IPlatePersistencePort platePersistencePort;
    private final IEmployeeRestaurantPersistencePort employeeRestaurantPersistencePort;
    private final ISmsNotificationPort smsNotificationPort;
    private final IUserPersistencePort userPersistencePort;
    private final ITraceabilityNotificationPort traceabilityNotificationPort;

    @Override
    public OrderModel createOrder(OrderModel orderModel, Long idCliente) {
        log.info("[USE CASE] Iniciando creación de pedido para cliente: {}, restaurante: {}",
                idCliente, orderModel.getIdRestaurante());

        validateRestaurantExists(orderModel.getIdRestaurante());
        validateNoActiveOrders(idCliente);

        validateAndEnrichOrderItems(orderModel);

        orderModel.setIdCliente(idCliente);
        orderModel.setEstado(OrderStatus.PENDIENTE);

        log.info("[USE CASE] Todas las validaciones OK, persistiendo pedido");

        OrderModel savedOrder = orderPersistencePort.saveOrder(orderModel);

        try {
            traceabilityNotificationPort.sendTraceabilityLog(savedOrder.getId(), savedOrder.getIdCliente(),
                    "CREACION", savedOrder.getEstado().name());
        } catch (Exception e) {
            log.error("[USE CASE] Error enviando log de trazabilidad: {}", e.getMessage());
        }

        return savedOrder;
    }

    @Override
    public Page<OrderModel> listOrdersByStatus(OrderStatus status, Long employeeId, Pageable pageable) {
        log.info("[USE CASE] Listando pedidos por estado: {}, empleado: {}", status, employeeId);

        EmployeeRestaurantModel employeeRestaurant = getEmployeeRestaurant(employeeId);

        Long restaurantId = employeeRestaurant.getIdRestaurante();
        log.debug("[USE CASE] Empleado {} pertenece al restaurante {}", employeeId, restaurantId);

        Page<OrderModel> orders = orderPersistencePort.findByRestaurantIdAndStatus(
                restaurantId, status, pageable);

        log.info("[USE CASE] Encontrados {} pedidos con estado {} en restaurante {}", 
                orders.getTotalElements(), status, restaurantId);

        return orders;
    }

    private EmployeeRestaurantModel getEmployeeRestaurant(Long employeeId) {
        return employeeRestaurantPersistencePort
                .findByEmployeeId(employeeId)
                .orElseThrow(() -> {
                    log.warn("[USE CASE] El empleado {} no está asignado a ningún restaurante", employeeId);
                    return new DomainException(EmployeeErrorMessages.NOT_BELONGS_TO_RESTAURANT);
                });
    }

    private OrderModel getOrderById(Long orderId) {
        return orderPersistencePort.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("[USE CASE] Pedido no encontrado: id={}", orderId);
                    return new DomainException(OrderErrorMessages.NOT_FOUND);
                });
    }

    private void validateRestaurantExists(Long restaurantId) {
        log.debug("[USE CASE] Validando existencia del restaurante: id={}", restaurantId);

        restaurantPersistencePort.findRestaurantById(restaurantId)
                .orElseThrow(() -> {
                    log.warn("[USE CASE] Restaurante no encontrado: id={}", restaurantId);
                    return new DomainException(RestaurantErrorMessages.NOT_FOUND);
                });

        log.debug("[USE CASE] Restaurante validado correctamente");
    }

    private void validateNoActiveOrders(Long clientId) {
        log.debug("[USE CASE] Verificando pedidos activos para cliente: {}", clientId);

        if (orderPersistencePort.existsActiveOrderByClientId(clientId, ACTIVE_ORDER_STATUSES)) {
            log.warn("[USE CASE] Cliente ya tiene un pedido activo: clientId={}", clientId);
            throw new DomainException(OrderErrorMessages.CLIENT_HAS_ACTIVE_ORDER);
        }

        log.debug("[USE CASE] Cliente no tiene pedidos activos");
    }

    private void validateAndEnrichOrderItems(OrderModel orderModel) {
        log.debug("[USE CASE] Validando {} items del pedido", orderModel.getItems().size());

        for (OrderItemModel item : orderModel.getItems()) {
            PlateModel plate = platePersistencePort.findPlateById(item.getIdPlato())
                    .orElseThrow(() -> {
                        log.warn("[USE CASE] Plato no encontrado: id={}", item.getIdPlato());
                        return new DomainException(PlateErrorMessages.NOT_FOUND);
                    });

            if (!plate.getIdRestaurante().equals(orderModel.getIdRestaurante())) {
                log.warn("[USE CASE] Plato {} no pertenece al restaurante {}", 
                        item.getIdPlato(), orderModel.getIdRestaurante());
                throw new DomainException(PlateErrorMessages.NOT_BELONGS_TO_RESTAURANT);
            }

            if (!Boolean.TRUE.equals(plate.getActiva())) {
                log.warn("[USE CASE] Plato no disponible: id={}", item.getIdPlato());
                throw new DomainException(PlateErrorMessages.NOT_ACTIVE);
            }

            item.setNombrePlato(plate.getNombre());
            item.setPrecioPlato(plate.getPrecio());
        }

        log.debug("[USE CASE] Todos los items validados y enriquecidos");
    }

    @Override
    public OrderModel assignEmployeeToOrder(Long orderId, Long employeeId) {
        log.info("[USE CASE] Asignando empleado {} al pedido {}", employeeId, orderId);

        EmployeeRestaurantModel employeeRestaurant = getEmployeeRestaurant(employeeId);
        OrderModel order = getOrderById(orderId);

        validateOrderIsPending(order);
        validateOrderBelongsToEmployeeRestaurant(order, employeeRestaurant);

        OrderModel updatedOrder = OrderModel.builder()
                .id(order.getId())
                .idCliente(order.getIdCliente())
                .idRestaurante(order.getIdRestaurante())
                .idChef(employeeId)
                .estado(OrderStatus.EN_PREPARACION)
                .creadoEn(order.getCreadoEn())
                .actualizadoEn(order.getActualizadoEn())
                .items(order.getItems())
                .build();

        OrderModel savedOrder = orderPersistencePort.saveOrder(updatedOrder);
        log.info("[USE CASE] Pedido {} asignado al empleado {} con estado EN_PREPARACION", orderId, employeeId);

        try {
            java.util.Map<String, String> metadata = java.util.Map.of("restaurantId", String.valueOf(employeeRestaurant.getIdRestaurante()), "employeeId", String.valueOf(employeeId));
            traceabilityNotificationPort.sendTraceabilityLog(orderId, order.getIdCliente(), order.getEstado().name(), savedOrder.getEstado().name(), metadata);
        } catch (Exception e) {
            log.error("[USE CASE] Error enviando log de trazabilidad: {}", e.getMessage());
        }

        return savedOrder;
    }

    private void validateOrderIsPending(OrderModel order) {
        if (!OrderStatus.PENDIENTE.equals(order.getEstado())) {
            log.warn("[USE CASE] El pedido {} no está en estado PENDIENTE, estado actual: {}", 
                    order.getId(), order.getEstado());
            throw new DomainException(OrderErrorMessages.NOT_PENDING);
        }
    }

    private void validateOrderCanBeCanceled(OrderModel order) {
        if (order.getEstado() != OrderStatus.PENDIENTE && order.getEstado() != OrderStatus.CANCELADO) {
            log.warn("[USE CASE] No se puede cancelar el pedido {}. Estado actual: {}",
                    order.getId(), order.getEstado());
            throw new DomainException(OrderErrorMessages.ORDER_ALREADY_IN_PREPARATION);
        }

        if (order.getEstado() == OrderStatus.CANCELADO) {
            log.info("[USE CASE] El pedido {} ya fue cancelado previamente", order.getId());
            throw new DomainException(OrderErrorMessages.ORDER_CANCEL);
        }
    }

    private void validateOrderBelongsToEmployeeRestaurant(OrderModel order, EmployeeRestaurantModel employeeRestaurant) {
        if (!order.getIdRestaurante().equals(employeeRestaurant.getIdRestaurante())) {
            log.warn("[USE CASE] El pedido {} pertenece al restaurante {}, pero el empleado pertenece al restaurante {}", 
                    order.getId(), order.getIdRestaurante(), employeeRestaurant.getIdRestaurante());
            throw new DomainException(OrderErrorMessages.NOT_BELONGS_TO_RESTAURANT);
        }
    }

    @Override
    public OrderModel markOrderAsReady(Long orderId, Long employeeId) {
        log.info("[USE CASE] Marcando pedido {} como LISTO por empleado {}", orderId, employeeId);

        EmployeeRestaurantModel employeeRestaurant = getEmployeeRestaurant(employeeId);
        OrderModel order = getOrderById(orderId);

        validateOrderIsInPreparation(order);
        validateOrderBelongsToEmployeeRestaurant(order, employeeRestaurant);

        String pin = generatePin();
        log.debug("[USE CASE] PIN generado para pedido {}: {}", orderId, pin);

        OrderModel updatedOrder = OrderModel.builder()
                .id(order.getId())
                .idCliente(order.getIdCliente())
                .idRestaurante(order.getIdRestaurante())
                .idChef(order.getIdChef())
                .estado(OrderStatus.LISTO)
                .pin(pin)
                .creadoEn(order.getCreadoEn())
                .actualizadoEn(order.getActualizadoEn())
                .items(order.getItems())
                .build();

        OrderModel savedOrder = orderPersistencePort.saveOrder(updatedOrder);
        log.info("[USE CASE] Pedido {} marcado como LISTO", orderId);

        sendSmsNotification(order, employeeRestaurant.getIdRestaurante(), pin);

        try {
            traceabilityNotificationPort.sendTraceabilityLog(orderId, order.getIdCliente(), order.getEstado().name(), savedOrder.getEstado().name());
        } catch (Exception e) {
            log.error("[USE CASE] Error enviando log de trazabilidad: {}", e.getMessage());
        }

        return savedOrder;
    }

    @Override
    public OrderModel markOrderAsDelivered(Long orderId, Long employeeId, String pin) {
        log.info("[USE CASE] Intentando entregar pedido {}. Empleado: {}", orderId, employeeId);

        EmployeeRestaurantModel employeeRestaurant = getEmployeeRestaurant(employeeId);
        OrderModel order = getOrderById(orderId);

        validateOrderIsReady(order);
        validateOrderBelongsToEmployeeRestaurant(order, employeeRestaurant);

        if (order.getPin() == null || !order.getPin().equals(pin)) {
            log.warn("[USE CASE] PIN inválido para el pedido {}. Esperado: {}, Recibido: {}",
                    orderId, order.getPin(), pin);
            throw new DomainException(OrderErrorMessages.INVALID_PIN);
        }

        OrderModel updatedOrder = OrderModel.builder()
                .id(order.getId())
                .idCliente(order.getIdCliente())
                .idRestaurante(order.getIdRestaurante())
                .idChef(order.getIdChef())
                .estado(OrderStatus.ENTREGADO)
                .pin(order.getPin())
                .items(order.getItems())
                .creadoEn(order.getCreadoEn())
                .actualizadoEn(order.getActualizadoEn())
                .build();

        log.info("[USE CASE] Pedido {} listo para ser persistido como ENTREGADO", orderId);
        OrderModel savedOrder = orderPersistencePort.saveOrder(updatedOrder);

        try {
            traceabilityNotificationPort.sendTraceabilityLog(orderId, order.getIdCliente(), order.getEstado().name(), savedOrder.getEstado().name());
        } catch (Exception e) {
            log.error("[USE CASE] Error enviando log de trazabilidad: {}", e.getMessage());
        }

        return savedOrder;
    }

    @Override
    public OrderModel markOrderAsCanceled(Long orderId, Long employeeId) {
        log.info("[USE CASE] Intentando cancelar pedido {}. Usuario: {}", orderId, employeeId);

        OrderModel order = getOrderById(orderId);

        validateOrderCanBeCanceled(order);

        OrderModel canceledOrder = OrderModel.builder()
                .id(order.getId())
                .idCliente(order.getIdCliente())
                .idRestaurante(order.getIdRestaurante())
                .idChef(order.getIdChef())
                .estado(OrderStatus.CANCELADO)
                .pin(order.getPin())
                .items(order.getItems())
                .creadoEn(order.getCreadoEn())
                .build();

        log.info("[USE CASE] Pedido {} listo para ser persistido como CANCELADO", orderId);

        OrderModel savedOrder = orderPersistencePort.saveOrder(canceledOrder);

        try {
            traceabilityNotificationPort.sendTraceabilityLog(orderId, order.getIdCliente(), order.getEstado().name(), savedOrder.getEstado().name());
        } catch (Exception e) {
            log.error("[USE CASE] Error enviando log de trazabilidad: {}", e.getMessage());
        }

        return savedOrder;
    }

    private void validateOrderIsReady(OrderModel order) {
        if (order.getEstado() != OrderStatus.LISTO) {
            throw new DomainException(OrderErrorMessages.ORDER_NOT_READY_FOR_DELIVERY);
        }
    }

    private void validateOrderIsInPreparation(OrderModel order) {
        if (!OrderStatus.EN_PREPARACION.equals(order.getEstado())) {
            log.warn("[USE CASE] El pedido {} no está en estado EN_PREPARACION, estado actual: {}", 
                    order.getId(), order.getEstado());
            throw new DomainException(OrderErrorMessages.NOT_IN_PREPARATION);
        }
    }

    private String generatePin() {
        Random random = new Random();
        int pin = 100000 + random.nextInt(900000);
        return String.valueOf(pin);
    }

    private void sendSmsNotification(OrderModel order, Long restaurantId, String pin) {
        try {
            UserModel client = userPersistencePort.findUserById(order.getIdCliente())
                    .orElse(null);

            if (client == null || client.getCelular() == null) {
                log.warn("[USE CASE] No se pudo enviar SMS: cliente o teléfono no encontrado");
                return;
            }

            RestaurantModel restaurant = restaurantPersistencePort.findRestaurantById(restaurantId)
                    .orElse(null);

            String restaurantName = restaurant != null ? restaurant.getNombre() : "el restaurante";

            smsNotificationPort.sendOrderReadyNotification(client.getCelular(), pin, restaurantName);
            log.info("[USE CASE] Notificación SMS enviada al cliente {}", order.getIdCliente());
        } catch (Exception e) {
            log.error("[USE CASE] Error al enviar notificación SMS: {}", e.getMessage());
        }
    }
}
