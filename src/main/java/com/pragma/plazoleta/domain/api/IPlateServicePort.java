package com.pragma.plazoleta.domain.api;

import com.pragma.plazoleta.domain.model.PlateModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IPlateServicePort {
    PlateModel savePlate(PlateModel plateModel, Long idUsuarioPropietario);
    PlateModel updatePlate(Long idPlate, Integer precio, String descripcion, Long idUsuarioPropietario);
    PlateModel togglePlateStatus(Long idPlate, Boolean activa, Long idUsuarioPropietario);
    Page<PlateModel> listPlatesByRestaurant(Long restaurantId, String category, Pageable pageable);
}
