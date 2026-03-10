package com.pragma.plazoleta.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PlateResponseDto {
    private Long id;
    private String nombre;
    private Integer precio;
    private String descripcion;
    @JsonProperty("creadoEn")
    private String creadoEn;
}
