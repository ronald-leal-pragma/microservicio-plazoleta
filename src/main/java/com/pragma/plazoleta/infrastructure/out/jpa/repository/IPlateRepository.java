package com.pragma.plazoleta.infrastructure.out.jpa.repository;

import com.pragma.plazoleta.infrastructure.out.jpa.entity.PlateEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IPlateRepository extends JpaRepository<PlateEntity, Long> {
    boolean existsByNombreAndIdRestaurante(String nombre, Long idRestaurante);
    Page<PlateEntity> findByIdRestauranteAndActivaTrue(Long idRestaurante, Pageable pageable);
    Page<PlateEntity> findByIdRestauranteAndCategoriaAndActivaTrue(Long idRestaurante, String categoria, Pageable pageable);
}
