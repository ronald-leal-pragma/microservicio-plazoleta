package com.pragma.plazoleta.application.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeResponseDto {
    private Long id;
    private String nombre;
    private String apellido;
    private String correo;
    private String rol;
}