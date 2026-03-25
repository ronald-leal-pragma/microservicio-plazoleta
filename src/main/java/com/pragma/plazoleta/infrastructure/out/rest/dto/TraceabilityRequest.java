package com.pragma.plazoleta.infrastructure.out.rest.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TraceabilityRequest {
    private Long pedidoId;
    private Long clienteId;
    private String estadoAnterior;
    private String estadoNuevo;
    private Map<String, String> metadata;
}
