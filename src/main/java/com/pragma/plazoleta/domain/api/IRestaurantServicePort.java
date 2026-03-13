package com.pragma.plazoleta.domain.api;

import com.pragma.plazoleta.domain.model.RestaurantModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IRestaurantServicePort {
    RestaurantModel saveRestaurant(RestaurantModel restaurantModel);
    Page<RestaurantModel> listRestaurants(Pageable pageable);
}
