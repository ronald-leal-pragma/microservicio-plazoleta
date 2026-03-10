package com.pragma.plazoleta.application.handler.impl;

import com.pragma.plazoleta.application.dto.request.PlateRequestDto;
import com.pragma.plazoleta.application.dto.request.PlateUpdateRequestDto;
import com.pragma.plazoleta.application.dto.response.PlateResponseDto;
import com.pragma.plazoleta.application.handler.IPlateHandler;
import com.pragma.plazoleta.application.mapper.IPlateRequestMapper;
import com.pragma.plazoleta.domain.api.IPlateServicePort;
import com.pragma.plazoleta.domain.model.PlateModel;
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
public class PlateHandler implements IPlateHandler {

    private final IPlateServicePort plateServicePort;
    private final IPlateRequestMapper plateRequestMapper;

    @Override
    public PlateResponseDto savePlate(PlateRequestDto plateRequestDto) {
        log.info("[HANDLER] Iniciando proceso de creación de plato: nombre={}, restaurante={}", 
                plateRequestDto.getNombre(), plateRequestDto.getIdRestaurante());
        Long idUsuarioPropietario = getAuthenticatedUserId();
        PlateModel plateModel = plateRequestMapper.toPlate(plateRequestDto);
        PlateModel saved = plateServicePort.savePlate(plateModel, idUsuarioPropietario);
        log.info("[HANDLER] Proceso finalizado correctamente para plato: {}", 
                plateRequestDto.getNombre());
        PlateResponseDto dto = new PlateResponseDto();
        dto.setId(saved.getId());
        dto.setNombre(saved.getNombre());
        dto.setPrecio(saved.getPrecio());
        dto.setDescripcion(saved.getDescripcion());
        dto.setCreadoEn(saved.getCreadoEn() != null ? saved.getCreadoEn().toString() : null);
        return dto;
    }

    @Override
    public PlateResponseDto updatePlate(Long idPlate, PlateUpdateRequestDto plateUpdateRequestDto) {
        log.info("[HANDLER] Iniciando proceso de actualización de plato: id={}", idPlate);
        Long idUsuarioPropietario = getAuthenticatedUserId();
        PlateModel updated = plateServicePort.updatePlate(
                idPlate, 
                plateUpdateRequestDto.getPrecio(), 
                plateUpdateRequestDto.getDescripcion(),
                idUsuarioPropietario
        );
        log.info("[HANDLER] Proceso finalizado correctamente para plato: id={}", idPlate);
        PlateResponseDto dto = new PlateResponseDto();
        dto.setId(updated.getId());
        dto.setNombre(updated.getNombre());
        dto.setPrecio(updated.getPrecio());
        dto.setDescripcion(updated.getDescripcion());
        dto.setCreadoEn(updated.getCreadoEn() != null ? updated.getCreadoEn().toString() : null);
        return dto;
    }

    private Long getAuthenticatedUserId() {
        JwtUserDetails userDetails = (JwtUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return userDetails.getId();
    }
}
