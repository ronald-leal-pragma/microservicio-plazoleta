package com.pragma.plazoleta.domain.spi;

import com.pragma.plazoleta.domain.model.PlateModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface IPlatePersistencePort {
    PlateModel savePlate(PlateModel plateModel);

    boolean existsPlateByNameAndRestaurantId(String name, Long restaurantId);

    Optional<PlateModel> findPlateById(Long id);

    PlateModel updatePlate(PlateModel plateModel);

    Page<PlateModel> findPlatesByRestaurantId(Long restaurantId, Pageable pageable);

    Page<PlateModel> findPlatesByRestaurantIdAndCategory(Long restaurantId, String category, Pageable pageable);
}
