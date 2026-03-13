package com.pragma.plazoleta.infrastructure.out.http.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserCreateRequestDto {
    private String nombre;
    private String apellido;
    private String documentoDeIdentidad;
    private String celular;
    private String correo;
    private Long idRol;
    private String clave;
}