package com.pragma.plazoleta.application.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponseDto {
    private Long id;
    private Long idRestaurante;
    private String nombreRestaurante;
    private String estado;
    private List<OrderItemResponseDto> items;
    private Integer total;
    private String creadoEn;
}
