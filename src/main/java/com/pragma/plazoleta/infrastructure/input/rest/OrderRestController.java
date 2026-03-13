package com.pragma.plazoleta.infrastructure.input.rest;

import com.pragma.plazoleta.application.dto.request.OrderRequestDto;
import com.pragma.plazoleta.application.dto.response.OrderResponseDto;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
