package com.pragma.plazoleta.domain.api;

import com.pragma.plazoleta.domain.model.PlateModel;

public interface IPlateServicePort {
    PlateModel savePlate(PlateModel plateModel, Long idUsuarioPropietario);
    PlateModel updatePlate(Long idPlate, Integer precio, String descripcion, Long idUsuarioPropietario);
}
