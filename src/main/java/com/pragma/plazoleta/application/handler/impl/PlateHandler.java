package com.pragma.plazoleta.application.handler.impl;

import com.pragma.plazoleta.application.dto.request.PlateRequestDto;
import com.pragma.plazoleta.application.dto.request.PlateUpdateRequestDto;
import com.pragma.plazoleta.application.handler.IPlateHandler;
import com.pragma.plazoleta.application.mapper.IPlateRequestMapper;
import com.pragma.plazoleta.domain.api.IPlateServicePort;
import com.pragma.plazoleta.domain.model.PlateModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PlateHandler implements IPlateHandler {

    private final IPlateServicePort plateServicePort;
    private final IPlateRequestMapper plateRequestMapper;

    @Override
    public void savePlate(PlateRequestDto plateRequestDto) {
        log.info("[HANDLER] Iniciando proceso de creación de plato: nombre={}, restaurante={}", 
                plateRequestDto.getNombre(), plateRequestDto.getIdRestaurante());
        PlateModel plateModel = plateRequestMapper.toPlate(plateRequestDto);
        plateServicePort.savePlate(plateModel, plateRequestDto.getIdUsuarioPropietario());
        log.info("[HANDLER] Proceso finalizado correctamente para plato: {}", 
                plateRequestDto.getNombre());
    }

    @Override
    public void updatePlate(Long idPlate, PlateUpdateRequestDto plateUpdateRequestDto) {
        log.info("[HANDLER] Iniciando proceso de actualización de plato: id={}", idPlate);
        plateServicePort.updatePlate(
                idPlate, 
                plateUpdateRequestDto.getPrecio(), 
                plateUpdateRequestDto.getDescripcion(),
                plateUpdateRequestDto.getIdUsuarioPropietario()
        );
        log.info("[HANDLER] Proceso finalizado correctamente para plato: id={}", idPlate);
    }
}
