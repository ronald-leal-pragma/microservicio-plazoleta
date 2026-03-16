package com.pragma.plazoleta.infrastructure.out.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "empleado_restaurante",
       uniqueConstraints = @UniqueConstraint(columnNames = {"id_empleado", "id_restaurante"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRestaurantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_empleado", nullable = false)
    private Long idEmpleado;

    @Column(name = "id_restaurante", nullable = false)
    private Long idRestaurante;
}
