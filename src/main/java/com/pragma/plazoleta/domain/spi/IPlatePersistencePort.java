package com.pragma.plazoleta.domain.spi;

import com.pragma.plazoleta.domain.model.PlateModel;

import java.util.Optional;

public interface IPlatePersistencePort {
    PlateModel savePlate(PlateModel plateModel);

    boolean existsPlateByNameAndRestaurantId(String name, Long restaurantId);

    Optional<PlateModel> findPlateById(Long id);

    PlateModel updatePlate(PlateModel plateModel);
}
