package com.pragma.plazoleta.application.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;

@Getter
@Setter
public class EmployeeRequestDto {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotBlank(message = "El documento de identidad es obligatorio")
    @Pattern(regexp = "^[0-9]+$",
             message = "El documento de identidad debe ser únicamente numérico")
    private String documentoDeIdentidad;

    @NotBlank(message = "El celular es obligatorio")
    @Pattern(regexp = "^\\+?[0-9]{1,13}$",
             message = "El celular debe ser numérico, puede contener el símbolo '+' y tener máximo 13 caracteres")
    private String celular;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo debe tener una estructura válida")
    private String correo;

    @NotNull(message = "El idRol es obligatorio")
    private Long idRol;

    @NotBlank(message = "La clave es obligatoria")
    private String clave;

    private LocalDate fechaNacimiento;

    @NotNull(message = "El idRestaurante es obligatorio")
    private Long idRestaurante;
}