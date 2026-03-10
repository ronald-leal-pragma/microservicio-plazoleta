package com.pragma.plazoleta.application.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class PlateRequestDto {

    @NotBlank(message = "El nombre del plato es obligatorio")
    private String nombre;

    @NotNull(message = "El precio es obligatorio")
    @Min(value = 1, message = "El precio debe ser un número entero positivo mayor a 0")
    private Integer precio;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    @NotBlank(message = "La URL de la imagen es obligatoria")
    private String urlImagen;

    @NotBlank(message = "La categoría es obligatoria")
    private String categoria;

    @NotNull(message = "El ID del restaurante es obligatorio")
    private Long idRestaurante;
}
