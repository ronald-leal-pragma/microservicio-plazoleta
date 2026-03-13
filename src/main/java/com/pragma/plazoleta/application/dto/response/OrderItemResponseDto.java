package com.pragma.plazoleta.application.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemResponseDto {
    private Long idPlato;
    private String nombrePlato;
    private Integer cantidad;
    private Integer precioUnitario;
    private Integer subtotal;
}
