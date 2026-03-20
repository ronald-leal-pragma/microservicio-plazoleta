package com.pragma.plazoleta.application.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
@Setter
public class RestaurantRequestDto {

    @NotBlank(message = "El nombre del restaurante es obligatorio")
    private String nombre;

    @NotBlank(message = "El NIT es obligatorio")
    @Pattern(regexp = "^[0-9]+$", message = "El NIT debe ser únicamente numérico")
    private String nit;

    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^\\+?[0-9]{1,13}$",
             message = "El teléfono debe ser numérico, puede contener el símbolo '+' y tener máximo 13 caracteres")
    private String telefono;

    @NotBlank(message = "La URL del logo es obligatoria")
    private String urlLogo;

    @NotBlank(message = "El correo del propietario es obligatorio")
    private String correoPropietario;
}
