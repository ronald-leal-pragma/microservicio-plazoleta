package com.pragma.plazoleta.application.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlateListResponseDto {
    private Long id;
    private String nombre;
    private String descripcion;
    private Integer precio;
    private String urlImagen;
    private String categoria;
}
