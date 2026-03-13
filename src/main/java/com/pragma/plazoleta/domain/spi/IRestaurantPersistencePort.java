package com.pragma.plazoleta.domain.spi;

import com.pragma.plazoleta.domain.model.RestaurantModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface IRestaurantPersistencePort {
    RestaurantModel saveRestaurant(RestaurantModel restaurantModel);

    Optional<RestaurantModel> findRestaurantById(Long id);

    boolean existsRestaurantByNit(String nit);

    boolean existsRestaurantByName(String name);

    boolean existsRestaurantByOwnerId(Long ownerId);

    Page<RestaurantModel> findAllRestaurantsOrderByName(Pageable pageable);
}
