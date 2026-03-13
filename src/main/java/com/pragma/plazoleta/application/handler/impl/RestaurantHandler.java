package com.pragma.plazoleta.application.handler.impl;

import com.pragma.plazoleta.application.dto.request.RestaurantRequestDto;
import com.pragma.plazoleta.application.dto.response.PaginatedResponseDto;
import com.pragma.plazoleta.application.dto.response.RestaurantListResponseDto;
import com.pragma.plazoleta.application.dto.response.RestaurantResponseDto;
import com.pragma.plazoleta.application.handler.IRestaurantHandler;
import com.pragma.plazoleta.application.mapper.IRestaurantRequestMapper;
import com.pragma.plazoleta.domain.api.IRestaurantServicePort;
import com.pragma.plazoleta.domain.model.RestaurantModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantHandler implements IRestaurantHandler {

    private final IRestaurantServicePort restaurantServicePort;
    private final IRestaurantRequestMapper restaurantRequestMapper;

    @Override
    public RestaurantResponseDto saveRestaurant(RestaurantRequestDto restaurantRequestDto) {
        log.info("[HANDLER] Iniciando proceso de creación de restaurante: nombre={}, propietario={}",
                restaurantRequestDto.getNombre(), restaurantRequestDto.getIdUsuarioPropietario());

        RestaurantModel restaurantModel = restaurantRequestMapper.toRestaurant(restaurantRequestDto);
        RestaurantModel saved = restaurantServicePort.saveRestaurant(restaurantModel);
        log.info("[HANDLER] Proceso finalizado correctamente para restaurante: {}",
                restaurantRequestDto.getNombre());
        RestaurantResponseDto dto = new RestaurantResponseDto();
        dto.setId(saved.getId());
        dto.setNombre(saved.getNombre());
        dto.setNit(saved.getNit());
        dto.setCreadoEn(saved.getCreadoEn() != null ? saved.getCreadoEn().toString() : null);
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDto<RestaurantListResponseDto> listRestaurants(int page, int size) {
        log.info("[HANDLER] Listando restaurantes: page={}, size={}", page, size);

        Page<RestaurantModel> restaurantPage = restaurantServicePort.listRestaurants(PageRequest.of(page, size));

        List<RestaurantListResponseDto> content = restaurantPage.getContent().stream()
                .map(r -> RestaurantListResponseDto.builder()
                        .nombre(r.getNombre())
                        .urlLogo(r.getUrlLogo())
                        .build())
                .collect(Collectors.toList());

        log.info("[HANDLER] Restaurantes encontrados: {} de {} total", content.size(), restaurantPage.getTotalElements());

        return PaginatedResponseDto.<RestaurantListResponseDto>builder()
                .content(content)
                .pageNumber(restaurantPage.getNumber())
                .pageSize(restaurantPage.getSize())
                .totalElements(restaurantPage.getTotalElements())
                .totalPages(restaurantPage.getTotalPages())
                .last(restaurantPage.isLast())
                .build();
    }
}
