package com.pragma.plazoleta.domain.usecase;

import com.pragma.plazoleta.domain.api.IPlateServicePort;
import com.pragma.plazoleta.domain.exception.DomainException;
import com.pragma.plazoleta.domain.exception.ExceptionConstants;
import com.pragma.plazoleta.domain.model.PlateModel;
import com.pragma.plazoleta.domain.model.RestaurantModel;
import com.pragma.plazoleta.domain.spi.IPlatePersistencePort;
import com.pragma.plazoleta.domain.spi.IRestaurantPersistencePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlateUseCase implements IPlateServicePort {

    private static final Logger log = LoggerFactory.getLogger(PlateUseCase.class);

    private final IPlatePersistencePort platePersistencePort;
    private final IRestaurantPersistencePort restaurantPersistencePort;

    public PlateUseCase(IPlatePersistencePort platePersistencePort,
                        IRestaurantPersistencePort restaurantPersistencePort) {
        this.platePersistencePort = platePersistencePort;
        this.restaurantPersistencePort = restaurantPersistencePort;
    }

    @Override
    public PlateModel savePlate(PlateModel plateModel, Long idUsuarioPropietario) {
        log.info("[USE CASE] Iniciando validaciones para crear plato: nombre={}, restaurante={}", 
                plateModel.getNombre(), plateModel.getIdRestaurante());

        // Validación 1: El precio debe ser un entero positivo mayor a 0
        validatePrice(plateModel.getPrecio());
        
        // Validación 2: Verificar que el restaurante existe
        RestaurantModel restaurant = restaurantPersistencePort.findRestaurantById(plateModel.getIdRestaurante())
                .orElseThrow(() -> {
                    log.warn("[USE CASE] Restaurante no encontrado: id={}", plateModel.getIdRestaurante());
                    return new DomainException(ExceptionConstants.RESTAURANT_NOT_FOUND_MESSAGE);
                });
        
        // Validación 3: Verificar que el usuario es el propietario del restaurante
        validateRestaurantOwner(idUsuarioPropietario, restaurant);
        
        // Establecer activa = true por defecto
        plateModel.setActiva(true);
        log.debug("[USE CASE] Plato configurado como activo por defecto");

        log.info("[USE CASE] Todas las validaciones OK, persistiendo plato");
        PlateModel saved = platePersistencePort.savePlate(plateModel);
        log.info("[USE CASE] Plato creado exitosamente: {}", plateModel.getNombre());
        return saved;
    }

    @Override
    public PlateModel updatePlate(Long idPlate, Integer precio, String descripcion, Long idUsuarioPropietario) {
        log.info("[USE CASE] Iniciando actualización de plato: id={}", idPlate);

        // Validación 1: El precio debe ser un entero positivo mayor a 0
        validatePrice(precio);
        
        // Validación 2: Verificar que el plato existe
        PlateModel plate = platePersistencePort.findPlateById(idPlate)
                .orElseThrow(() -> {
                    log.warn("[USE CASE] Plato no encontrado: id={}", idPlate);
                    return new DomainException(ExceptionConstants.RESTAURANT_NOT_FOUND_MESSAGE);
                });
        
        // Validación 3: Verificar que el restaurante existe
        RestaurantModel restaurant = restaurantPersistencePort.findRestaurantById(plate.getIdRestaurante())
                .orElseThrow(() -> {
                    log.warn("[USE CASE] Restaurante no encontrado: id={}", plate.getIdRestaurante());
                    return new DomainException(ExceptionConstants.RESTAURANT_NOT_FOUND_MESSAGE);
                });
        
        // Validación 4: Verificar que el usuario es el propietario del restaurante
        validateRestaurantOwner(idUsuarioPropietario, restaurant);
        
        // Actualizar solo precio y descripción
        plate.setPrecio(precio);
        plate.setDescripcion(descripcion);
        
        log.info("[USE CASE] Actualizando plato en base de datos");
        PlateModel updated = platePersistencePort.updatePlate(plate);
        log.info("[USE CASE] Plato actualizado exitosamente: id={}", idPlate);
        return updated;
    }

    private void validatePrice(Integer precio) {
        log.debug("[USE CASE] Validando precio: {}", precio);
        
        if (precio == null || precio <= 0) {
            log.warn("[USE CASE] Precio rechazado: debe ser mayor a 0");
            throw new DomainException(ExceptionConstants.INVALID_PRICE_MESSAGE);
        }
        
        log.debug("[USE CASE] Precio válido");
    }

    private void validateRestaurantOwner(Long idUsuarioPropietario, RestaurantModel restaurant) {
        log.debug("[USE CASE] Validando propietario: usuario={}, propietarioRestaurante={}", 
                idUsuarioPropietario, restaurant.getIdUsuarioPropietario());
        
        if (!restaurant.getIdUsuarioPropietario().equals(idUsuarioPropietario)) {
            log.warn("[USE CASE] Usuario no es propietario del restaurante: usuario={}, propietario={}", 
                    idUsuarioPropietario, restaurant.getIdUsuarioPropietario());
            throw new DomainException(ExceptionConstants.USER_NOT_RESTAURANT_OWNER_MESSAGE);
        }
        
        log.debug("[USE CASE] Usuario propietario validado correctamente");
    }
}
