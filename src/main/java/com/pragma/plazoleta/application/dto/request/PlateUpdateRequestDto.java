package com.pragma.plazoleta.application.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class PlateUpdateRequestDto {

    @NotNull(message = "El precio es obligatorio")
    @Min(value = 1, message = "El precio debe ser un número entero positivo mayor a 0")
    private Integer precio;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    @NotNull(message = "El ID del usuario propietario es obligatorio")
    private Long idUsuarioPropietario;
}
