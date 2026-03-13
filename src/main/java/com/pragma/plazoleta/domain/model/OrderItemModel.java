package com.pragma.plazoleta.domain.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemModel {
    private Long id;
    private Long idPedido;
    private Long idPlato;
    private Integer cantidad;
    private String nombrePlato;
    private Integer precioPlato;
}
