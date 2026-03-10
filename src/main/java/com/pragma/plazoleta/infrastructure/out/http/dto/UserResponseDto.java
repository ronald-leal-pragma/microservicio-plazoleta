package com.pragma.plazoleta.infrastructure.out.http.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserResponseDto {
    private Long id;
    private String nombre;
    private String apellido;
    private String correo;
    private String rol;
}
