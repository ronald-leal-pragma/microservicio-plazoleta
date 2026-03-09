package com.pragma.plazoleta.application.handler;

import com.pragma.plazoleta.application.dto.request.PlateRequestDto;
import com.pragma.plazoleta.application.dto.request.PlateUpdateRequestDto;

public interface IPlateHandler {
    void savePlate(PlateRequestDto plateRequestDto);
    void updatePlate(Long idPlate, PlateUpdateRequestDto plateUpdateRequestDto);
}
