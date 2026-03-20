package com.pragma.plazoleta.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RestaurantResponseDto {
    private Long id;
    private String nombre;
    private String nit;
    @JsonProperty("creadoEn")
    private String creadoEn;
}
