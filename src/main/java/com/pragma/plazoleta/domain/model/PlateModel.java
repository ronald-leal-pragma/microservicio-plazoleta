package com.pragma.plazoleta.domain.model;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlateModel {
    private Long id;
    private String nombre;
    private Integer precio;
    private String descripcion;
    private String urlImagen;
    private String categoria;
    private Boolean activa;
    private Long idRestaurante;
    private Instant creadoEn;
}
