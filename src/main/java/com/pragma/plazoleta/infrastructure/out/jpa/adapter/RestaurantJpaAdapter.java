package com.pragma.plazoleta.infrastructure.out.jpa.adapter;

import com.pragma.plazoleta.domain.model.RestaurantModel;
import com.pragma.plazoleta.domain.spi.IRestaurantPersistencePort;
import com.pragma.plazoleta.infrastructure.out.jpa.entity.RestaurantEntity;
import com.pragma.plazoleta.infrastructure.out.jpa.mapper.IRestaurantEntityMapper;
import com.pragma.plazoleta.infrastructure.out.jpa.repository.IRestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class RestaurantJpaAdapter implements IRestaurantPersistencePort {

    private final IRestaurantRepository restaurantRepository;
    private final IRestaurantEntityMapper restaurantEntityMapper;

    @Override
    public RestaurantModel saveRestaurant(RestaurantModel restaurantModel) {
        log.debug("[JPA ADAPTER] Mapeando modelo a entidad");
        RestaurantEntity restaurantEntity = restaurantEntityMapper.toEntity(restaurantModel);

        log.debug("[JPA ADAPTER] Guardando restaurante en base de datos");
        RestaurantEntity saved = restaurantRepository.save(restaurantEntity);

        log.info("[JPA ADAPTER] Restaurante guardado exitosamente: nombre={}, NIT={}",
                restaurantModel.getNombre(), restaurantModel.getNit());

        return restaurantEntityMapper.toModel(saved);
    }

    @Override
    public boolean existsRestaurantByNit(String nit) {
        log.debug("[JPA ADAPTER] Consultando existencia por NIT: {}", nit);
        return restaurantRepository.existsByNit(nit);
    }

    @Override
    public boolean existsRestaurantByName(String name) {
        log.debug("[JPA ADAPTER] Consultando existencia por nombre: {}", name);
        return restaurantRepository.existsByNombre(name);
    }

    @Override
    public Optional<RestaurantModel> findRestaurantById(Long id) {
        log.debug("[JPA ADAPTER] Buscando restaurante con id={}", id);
        return restaurantRepository.findById(id)
                .map(restaurantEntityMapper::toModel);
    }
}
