package com.pragma.plazoleta.infrastructure.out.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "pedido_plato")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pedido", nullable = false)
    private OrderEntity pedido;

    @Column(name = "id_plato", nullable = false)
    private Long idPlato;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "nombre_plato", nullable = false, length = 100)
    private String nombrePlato;

    @Column(name = "precio_plato", nullable = false)
    private Integer precioPlato;
}
