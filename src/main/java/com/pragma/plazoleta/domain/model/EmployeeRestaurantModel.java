package com.pragma.plazoleta.domain.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeRestaurantModel {
    private Long id;
    private Long idEmpleado;
    private Long idRestaurante;
}
