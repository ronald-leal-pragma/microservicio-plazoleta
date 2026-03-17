package com.pragma.plazoleta.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlateResponseDto {
    private Long id;
    private String nombre;
    private Integer precio;
    private String descripcion;
    private Boolean activa;
    @JsonProperty("creadoEn")
    private String creadoEn;
}
