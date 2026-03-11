package com.pragma.plazoleta.domain.model;

import lombok.*;

@Getter
@Setter
@Builder
public class RolModel {
    private Long id;
    private String nombre;
    private String descripcion;
}
