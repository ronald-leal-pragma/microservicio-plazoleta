package com.pragma.plazoleta.infrastructure.input.rest;

import com.pragma.plazoleta.application.dto.request.OrderRequestDto;
import com.pragma.plazoleta.application.dto.response.OrderListResponseDto;
import com.pragma.plazoleta.application.dto.response.OrderResponseDto;
import com.pragma.plazoleta.application.dto.response.PaginatedResponseDto;
import com.pragma.plazoleta.application.handler.IOrderHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@Tag(name = "Order", description = "Operaciones relacionadas con pedidos")
public class OrderRestController {

    private final IOrderHandler orderHandler;

    @Operation(summary = "Crear pedido",
               description = "Crea un nuevo pedido. Solo clientes pueden crear pedidos y no deben tener pedidos activos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pedido creado exitosamente",
                         content = @Content(schema = @Schema(implementation = OrderResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o cliente ya tiene pedido activo",
                         content = @Content),
            @ApiResponse(responseCode = "404", description = "Restaurante o plato no encontrado",
                         content = @Content),
            @ApiResponse(responseCode = "401", description = "No autorizado",
                         content = @Content)
    })
    @PostMapping("/")
    public ResponseEntity<OrderResponseDto> createOrder(@Valid @RequestBody OrderRequestDto orderRequestDto) {
        log.info("[REST] POST /order/ - Solicitud para crear pedido en restaurante: {}", 
                orderRequestDto.getIdRestaurante());
        OrderResponseDto created = orderHandler.createOrder(orderRequestDto);
        log.info("[REST] Pedido creado exitosamente: id={}", created.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Listar pedidos por estado",
               description = "Lista pedidos del restaurante del empleado filtrados por estado. Solo empleados pueden acceder.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de pedidos obtenida exitosamente"),
            @ApiResponse(responseCode = "400", description = "Estado inválido",
                         content = @Content),
            @ApiResponse(responseCode = "401", description = "No autorizado",
                         content = @Content),
            @ApiResponse(responseCode = "403", description = "El empleado no pertenece a ningún restaurante",
                         content = @Content)
    })
    @GetMapping("/")
    public ResponseEntity<PaginatedResponseDto<OrderListResponseDto>> listOrdersByStatus(
            @RequestParam String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("[REST] GET /order/ - Listando pedidos por estado: {}, page={}, size={}", status, page, size);
        PaginatedResponseDto<OrderListResponseDto> orders = orderHandler.listOrdersByStatus(status, page, size);
        log.info("[REST] Pedidos obtenidos: {} elementos", orders.getTotalElements());
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Asignar empleado a pedido",
               description = "El empleado autenticado se asigna a un pedido y cambia su estado a EN_PREPARACION. Solo empleados pueden acceder.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido asignado exitosamente",
                         content = @Content(schema = @Schema(implementation = OrderListResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "El pedido no está en estado PENDIENTE",
                         content = @Content),
            @ApiResponse(responseCode = "401", description = "No autorizado",
                         content = @Content),
            @ApiResponse(responseCode = "403", description = "El empleado no pertenece al restaurante del pedido",
                         content = @Content),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado",
                         content = @Content)
    })
    @PatchMapping("/{orderId}/assign")
    public ResponseEntity<OrderListResponseDto> assignOrderToEmployee(@PathVariable Long orderId) {
        log.info("[REST] PATCH /order/{}/assign - Asignando empleado al pedido", orderId);
        OrderListResponseDto assignedOrder = orderHandler.assignOrderToEmployee(orderId);
        log.info("[REST] Pedido {} asignado exitosamente, nuevo estado: {}", orderId, assignedOrder.getEstado());
        return ResponseEntity.ok(assignedOrder);
    }

    @Operation(summary = "Marcar pedido como LISTO",
            description = "El empleado marca un pedido en preparación como listo. Genera un PIN y envía SMS al cliente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido marcado como listo"),
            @ApiResponse(responseCode = "400", description = "El pedido no está en estado EN_PREPARACION",
                         content = @Content),
            @ApiResponse(responseCode = "401", description = "No autorizado",
                         content = @Content),
            @ApiResponse(responseCode = "403", description = "El empleado no pertenece al restaurante del pedido",
                         content = @Content),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado",
                         content = @Content)
    })
    @PatchMapping("/{orderId}/ready")
    public ResponseEntity<OrderListResponseDto> markOrderAsReady(@PathVariable Long orderId) {
        log.info("[REST] PATCH /order/{}/ready - Marcando pedido como LISTO", orderId);
        OrderListResponseDto readyOrder = orderHandler.markOrderAsReady(orderId);
        log.info("[REST] Pedido {} marcado como LISTO, PIN generado", orderId);
        return ResponseEntity.ok(readyOrder);
    }

    @Operation(summary = "Marcar pedido como ENTREGADO",
            description = "El empleado marca un pedido en listo como entregado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido marcado como entregado"),
            @ApiResponse(responseCode = "400", description = "El pedido no está en estado LISTO",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "No autorizado",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "El empleado no pertenece al restaurante del pedido",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado",
                    content = @Content)
    })
    @PatchMapping("/{orderId}/delivered")
    public ResponseEntity<OrderListResponseDto> markOrderAsDelivered(
            @PathVariable Long orderId,
            @RequestParam String pin) {

        log.info("[REST] PATCH /order/{}/entregado - Intento de entrega con PIN", orderId);

        OrderListResponseDto deliveredOrder = orderHandler.markOrderAsDelivered(orderId, pin);

        log.info("[REST] Pedido {} entregado exitosamente con PIN verificado", orderId);
        return ResponseEntity.ok(deliveredOrder);
    }

    @Operation(summary = "Marcar pedido como CANCELADO",
            description = "El empleado marca un pedido cancelado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido marcado como cancelado"),
            @ApiResponse(responseCode = "400", description = "El pedido no está en estado PENDIENTE",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "No autorizado",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "El empleado no pertenece al restaurante del pedido",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado",
                    content = @Content)
    })
    @DeleteMapping("/{orderId}/deleted")
    public ResponseEntity<OrderListResponseDto> markOrderAsDelivered(
            @PathVariable Long orderId) {

        log.info("[REST] DELETE /order/{}/deleted - Intento de eliminar orden", orderId);

        OrderListResponseDto deletedOrder = orderHandler.markOrderAsDeleted(orderId);

        log.info("[REST] Pedido {} cancelado exitosamente.", orderId);
        return ResponseEntity.ok(deletedOrder);
    }

}
