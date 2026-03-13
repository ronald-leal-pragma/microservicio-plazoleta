package com.pragma.plazoleta.infrastructure.out.http.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private Long id;
    private String nombre;
    private String apellido;
    @JsonProperty("email")
    private String correo;
    private String rol;
}
