package com.pragma.plazoleta.application.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderListResponseDto {
    private Long id;
    private Long idCliente;
    private Long idRestaurante;
    private Long idChef;
    private String estado;
    private List<OrderItemResponseDto> items;
    private Integer total;
    private String creadoEn;
    private String actualizadoEn;
}
