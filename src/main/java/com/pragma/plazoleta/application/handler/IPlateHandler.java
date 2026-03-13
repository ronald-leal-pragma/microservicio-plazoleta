package com.pragma.plazoleta.application.handler;

import com.pragma.plazoleta.application.dto.request.PlateRequestDto;
import com.pragma.plazoleta.application.dto.request.PlateUpdateRequestDto;
import com.pragma.plazoleta.application.dto.response.PaginatedResponseDto;
import com.pragma.plazoleta.application.dto.response.PlateListResponseDto;
import com.pragma.plazoleta.application.dto.response.PlateResponseDto;

public interface IPlateHandler {
    PlateResponseDto savePlate(PlateRequestDto plateRequestDto);
    PlateResponseDto updatePlate(Long idPlate, PlateUpdateRequestDto plateUpdateRequestDto);
    PlateResponseDto togglePlateStatus(Long idPlate, Boolean activa);
    PaginatedResponseDto<PlateListResponseDto> listPlatesByRestaurant(Long restaurantId, String category, int page, int size);
}
