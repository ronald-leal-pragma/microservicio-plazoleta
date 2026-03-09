package com.pragma.plazoleta.infrastructure.out.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "restaurante")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 20)
    private String nit;

    @Column(nullable = false, length = 255)
    private String direccion;

    @Column(nullable = false, length = 13)
    private String telefono;

    @Column(name = "url_logo", nullable = false, length = 500)
    private String urlLogo;

    @Column(name = "id_usuario_propietario", nullable = false)
    private Long idUsuarioPropietario;
}
