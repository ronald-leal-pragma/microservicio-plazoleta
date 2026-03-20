package com.pragma.plazoleta.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RolModel {
    private Long id;
    private String nombre;
    private String descripcion;
}
