package com.pragma.plazoleta.application.handler;

import com.pragma.plazoleta.application.dto.request.RestaurantRequestDto;
import com.pragma.plazoleta.application.dto.response.PaginatedResponseDto;
import com.pragma.plazoleta.application.dto.response.RestaurantListResponseDto;
import com.pragma.plazoleta.application.dto.response.RestaurantResponseDto;

public interface IRestaurantHandler {
    RestaurantResponseDto saveRestaurant(RestaurantRequestDto restaurantRequestDto);
    PaginatedResponseDto<RestaurantListResponseDto> listRestaurants(int page, int size);
}
