package com.pragma.plazoleta.domain.model;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderModel {
    private Long id;
    private Long idCliente;
    private Long idRestaurante;
    private Long idChef;
    private OrderStatus estado;
    private Instant creadoEn;
    private Instant actualizadoEn;
    private List<OrderItemModel> items;
}
