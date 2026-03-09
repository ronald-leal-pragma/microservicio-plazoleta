package com.pragma.plazoleta.infrastructure.out.jpa.adapter;

import com.pragma.plazoleta.domain.model.RestaurantModel;
import com.pragma.plazoleta.domain.spi.IRestaurantPersistencePort;
import com.pragma.plazoleta.infrastructure.exception.RestaurantAlreadyExistsException;
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
    public void saveRestaurant(RestaurantModel restaurantModel) {
        log.info("[JPA ADAPTER] Verificando duplicados para NIT={} y nombre={}",
                restaurantModel.getNit(), restaurantModel.getNombre());

        if (restaurantRepository.existsByNit(restaurantModel.getNit())) {
            log.warn("[JPA ADAPTER] NIT ya registrado: {}", restaurantModel.getNit());
            throw new RestaurantAlreadyExistsException();
        }

        if (restaurantRepository.existsByNombre(restaurantModel.getNombre())) {
            log.warn("[JPA ADAPTER] Nombre ya registrado: {}", restaurantModel.getNombre());
            throw new RestaurantAlreadyExistsException();
        }

        log.debug("[JPA ADAPTER] Mapeando modelo a entidad");
        RestaurantEntity restaurantEntity = restaurantEntityMapper.toEntity(restaurantModel);
        
        log.debug("[JPA ADAPTER] Guardando restaurante en base de datos");
        restaurantRepository.save(restaurantEntity);
        
        log.info("[JPA ADAPTER] Restaurante guardado exitosamente: nombre={}, NIT={}",
                restaurantModel.getNombre(), restaurantModel.getNit());
    }

    @Override
    public Optional<RestaurantModel> findRestaurantById(Long id) {
        log.debug("[JPA ADAPTER] Buscando restaurante con id={}", id);
        return restaurantRepository.findById(id)
                .map(restaurantEntityMapper::toModel);
    }
}
