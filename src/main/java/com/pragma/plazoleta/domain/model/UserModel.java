package com.pragma.plazoleta.domain.model;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class UserModel {
    private Long id;
    private String nombre;
    private String apellido;
    private String documentoDeIdentidad;
    private String celular;
    private LocalDate fechaNacimiento;
    private String correo;
    private String clave;
    private RolModel rol;
}
