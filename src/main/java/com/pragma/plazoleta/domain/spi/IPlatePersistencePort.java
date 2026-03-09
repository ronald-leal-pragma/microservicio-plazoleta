package com.pragma.plazoleta.domain.spi;

import com.pragma.plazoleta.domain.model.PlateModel;

import java.util.Optional;

public interface IPlatePersistencePort {
    void savePlate(PlateModel plateModel);
    Optional<PlateModel> findPlateById(Long id);
    void updatePlate(PlateModel plateModel);
}
