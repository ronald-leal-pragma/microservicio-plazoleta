package com.pragma.plazoleta.application.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
public class OrderRequestDto {

    @NotNull(message = "El id del restaurante es obligatorio")
    private Long idRestaurante;

    @NotEmpty(message = "Debe incluir al menos un plato en el pedido")
    @Valid
    private List<OrderItemRequestDto> items;
}
