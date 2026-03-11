package com.pragma.plazoleta.domain.spi;

import com.pragma.plazoleta.domain.model.RestaurantModel;

import java.util.Optional;

public interface IRestaurantPersistencePort {
    RestaurantModel saveRestaurant(RestaurantModel restaurantModel);

    Optional<RestaurantModel> findRestaurantById(Long id);

    boolean existsRestaurantByNit(String nit);

    boolean existsRestaurantByName(String name);
}
