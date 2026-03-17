package com.pragma.plazoleta.domain.usecase;

import com.pragma.plazoleta.domain.api.IOrderServicePort;
import com.pragma.plazoleta.domain.exception.DomainException;
import com.pragma.plazoleta.domain.exception.ExceptionConstants;
import com.pragma.plazoleta.domain.model.EmployeeRestaurantModel;
import com.pragma.plazoleta.domain.model.OrderItemModel;
import com.pragma.plazoleta.domain.model.OrderModel;
import com.pragma.plazoleta.domain.model.OrderStatus;
import com.pragma.plazoleta.domain.model.PlateModel;
import com.pragma.plazoleta.domain.spi.IEmployeeRestaurantPersistencePort;
import com.pragma.plazoleta.domain.spi.IOrderPersistencePort;
import com.pragma.plazoleta.domain.spi.IPlatePersistencePort;
import com.pragma.plazoleta.domain.spi.IRestaurantPersistencePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;

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

        return orderPersistencePort.saveOrder(orderModel);
    }

    @Override
    public Page<OrderModel> listOrdersByStatus(OrderStatus status, Long employeeId, Pageable pageable) {
        log.info("[USE CASE] Listando pedidos por estado: {}, empleado: {}", status, employeeId);

        EmployeeRestaurantModel employeeRestaurant = employeeRestaurantPersistencePort
                .findByEmployeeId(employeeId)
                .orElseThrow(() -> {
                    log.warn("[USE CASE] El empleado {} no está asignado a ningún restaurante", employeeId);
                    return new DomainException(ExceptionConstants.EMPLOYEE_NOT_BELONGS_TO_RESTAURANT_MESSAGE);
                });

        Long restaurantId = employeeRestaurant.getIdRestaurante();
        log.debug("[USE CASE] Empleado {} pertenece al restaurante {}", employeeId, restaurantId);

        Page<OrderModel> orders = orderPersistencePort.findByRestaurantIdAndStatus(
                restaurantId, status, pageable);

        log.info("[USE CASE] Encontrados {} pedidos con estado {} en restaurante {}", 
                orders.getTotalElements(), status, restaurantId);

        return orders;
    }

    private void validateRestaurantExists(Long restaurantId) {
        log.debug("[USE CASE] Validando existencia del restaurante: id={}", restaurantId);

        restaurantPersistencePort.findRestaurantById(restaurantId)
                .orElseThrow(() -> {
                    log.warn("[USE CASE] Restaurante no encontrado: id={}", restaurantId);
                    return new DomainException(ExceptionConstants.RESTAURANT_NOT_FOUND_MESSAGE);
                });

        log.debug("[USE CASE] Restaurante validado correctamente");
    }

    private void validateNoActiveOrders(Long clientId) {
        log.debug("[USE CASE] Verificando pedidos activos para cliente: {}", clientId);

        if (orderPersistencePort.existsActiveOrderByClientId(clientId, ACTIVE_ORDER_STATUSES)) {
            log.warn("[USE CASE] Cliente ya tiene un pedido activo: clientId={}", clientId);
            throw new DomainException(ExceptionConstants.CLIENT_HAS_ACTIVE_ORDER_MESSAGE);
        }

        log.debug("[USE CASE] Cliente no tiene pedidos activos");
    }

    private void validateAndEnrichOrderItems(OrderModel orderModel) {
        log.debug("[USE CASE] Validando {} items del pedido", orderModel.getItems().size());

        for (OrderItemModel item : orderModel.getItems()) {
            PlateModel plate = platePersistencePort.findPlateById(item.getIdPlato())
                    .orElseThrow(() -> {
                        log.warn("[USE CASE] Plato no encontrado: id={}", item.getIdPlato());
                        return new DomainException(ExceptionConstants.PLATE_NOT_FOUND_MESSAGE);
                    });

            if (!plate.getIdRestaurante().equals(orderModel.getIdRestaurante())) {
                log.warn("[USE CASE] Plato {} no pertenece al restaurante {}", 
                        item.getIdPlato(), orderModel.getIdRestaurante());
                throw new DomainException(ExceptionConstants.PLATE_NOT_BELONGS_TO_RESTAURANT_MESSAGE);
            }

            if (!Boolean.TRUE.equals(plate.getActiva())) {
                log.warn("[USE CASE] Plato no disponible: id={}", item.getIdPlato());
                throw new DomainException(ExceptionConstants.PLATE_NOT_ACTIVE_MESSAGE);
            }

            item.setNombrePlato(plate.getNombre());
            item.setPrecioPlato(plate.getPrecio());
        }

        log.debug("[USE CASE] Todos los items validados y enriquecidos");
    }
}
