package com.pragma.plazoleta.domain.spi;

import com.pragma.plazoleta.domain.model.PlateModel;

import java.util.Optional;

public interface IPlatePersistencePort {
    PlateModel savePlate(PlateModel plateModel);
    Optional<PlateModel> findPlateById(Long id);
    PlateModel updatePlate(PlateModel plateModel);
}
