package com.pragma.plazoleta.domain.api;

import com.pragma.plazoleta.domain.model.PlateModel;

public interface IPlateServicePort {
    void savePlate(PlateModel plateModel, Long idUsuarioPropietario);
    void updatePlate(Long idPlate, Integer precio, String descripcion, Long idUsuarioPropietario);
}
