package com.pragma.plazoleta.application.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RestaurantListResponseDto {
    private String nombre;
    private String urlLogo;
}
