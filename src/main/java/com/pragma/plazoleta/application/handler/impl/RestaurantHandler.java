package com.pragma.plazoleta.application.handler.impl;

import com.pragma.plazoleta.application.dto.request.RestaurantRequestDto;
import com.pragma.plazoleta.application.handler.IRestaurantHandler;
import com.pragma.plazoleta.application.mapper.IRestaurantRequestMapper;
import com.pragma.plazoleta.domain.api.IRestaurantServicePort;
import com.pragma.plazoleta.domain.model.RestaurantModel;
import com.pragma.plazoleta.infrastructure.configuration.jwt.JwtUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantHandler implements IRestaurantHandler {

    private final IRestaurantServicePort restaurantServicePort;
    private final IRestaurantRequestMapper restaurantRequestMapper;

    @Override
    public void saveRestaurant(RestaurantRequestDto restaurantRequestDto) {
        log.info("[HANDLER] Iniciando proceso de creación de restaurante: nombre={}", 
                restaurantRequestDto.getNombre());
        Long idUsuarioPropietario = getAuthenticatedUserId();
        RestaurantModel restaurantModel = restaurantRequestMapper.toRestaurant(restaurantRequestDto);
        restaurantModel.setIdUsuarioPropietario(idUsuarioPropietario);
        restaurantServicePort.saveRestaurant(restaurantModel);
        log.info("[HANDLER] Proceso finalizado correctamente para restaurante: {}", 
                restaurantRequestDto.getNombre());
    }

    private Long getAuthenticatedUserId() {
        JwtUserDetails userDetails = (JwtUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return userDetails.getId();
    }
}
