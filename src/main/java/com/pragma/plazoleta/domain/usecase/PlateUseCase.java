package com.pragma.plazoleta.domain.usecase;

import com.pragma.plazoleta.domain.api.IPlateServicePort;
import com.pragma.plazoleta.domain.exception.DomainException;
import com.pragma.plazoleta.domain.exception.ExceptionConstants;
import com.pragma.plazoleta.domain.model.PlateModel;
import com.pragma.plazoleta.domain.model.RestaurantModel;
import com.pragma.plazoleta.domain.spi.IPlatePersistencePort;
import com.pragma.plazoleta.domain.spi.IRestaurantPersistencePort;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@RequiredArgsConstructor
public class PlateUseCase implements IPlateServicePort {

    private static final Logger log = LoggerFactory.getLogger(PlateUseCase.class);
    private static final int MINIMUM_VALID_PRICE = 0;
    private final IPlatePersistencePort platePersistencePort;
    private final IRestaurantPersistencePort restaurantPersistencePort;

    @Override
    public PlateModel savePlate(PlateModel plateModel, Long idUsuarioPropietario) {
        log.info("[USE CASE] Iniciando validaciones para crear plato: nombre={}, restaurante={}",
                plateModel.getNombre(), plateModel.getIdRestaurante());

        validatePrice(plateModel.getPrecio());
        RestaurantModel restaurant = getRestaurantOrThrow(plateModel.getIdRestaurante());
        validateRestaurantOwner(idUsuarioPropietario, restaurant);

        if (platePersistencePort.existsPlateByNameAndRestaurantId(plateModel.getNombre(), plateModel.getIdRestaurante())) {
            log.warn("[USE CASE] El plato ya existe en este restaurante: nombre={}", plateModel.getNombre());
            throw new DomainException(ExceptionConstants.PLATE_ALREADY_EXISTS_MESSAGE);
        }

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

        validatePrice(precio);

        PlateModel plate = platePersistencePort.findPlateById(idPlate)
                .orElseThrow(() -> {
                    log.warn("[USE CASE] Plato no encontrado: id={}", idPlate);
                    return new DomainException(ExceptionConstants.PLATE_NOT_FOUND_MESSAGE);
                });

        RestaurantModel restaurant = getRestaurantOrThrow(plate.getIdRestaurante());
        validateRestaurantOwner(idUsuarioPropietario, restaurant);

        plate.setPrecio(precio);
        plate.setDescripcion(descripcion);

        log.debug("[USE CASE] Mapeo de actualización listo, enviando a persistencia");
        PlateModel updated = platePersistencePort.updatePlate(plate);

        log.info("[USE CASE] Plato actualizado exitosamente: id={}", idPlate);
        return updated;
    }

    @Override
    public PlateModel togglePlateStatus(Long idPlate, Boolean activa, Long idUsuarioPropietario) {
        log.info("[USE CASE] Iniciando cambio de estado de plato: id={}, nuevoEstado={}", idPlate, activa);

        PlateModel plate = platePersistencePort.findPlateById(idPlate)
                .orElseThrow(() -> {
                    log.warn("[USE CASE] Plato no encontrado: id={}", idPlate);
                    return new DomainException(ExceptionConstants.PLATE_NOT_FOUND_MESSAGE);
                });

        RestaurantModel restaurant = getRestaurantOrThrow(plate.getIdRestaurante());
        validateRestaurantOwner(idUsuarioPropietario, restaurant);

        plate.setActiva(activa);

        log.debug("[USE CASE] Actualizando estado del plato en persistencia");
        PlateModel updated = platePersistencePort.updatePlate(plate);

        log.info("[USE CASE] Estado del plato actualizado exitosamente: id={}, activa={}", idPlate, activa);
        return updated;
    }

    private void validatePrice(Integer precio) {
        log.debug("[USE CASE] Validando precio: {}", precio);

        Optional.ofNullable(precio)
                .filter(p -> p > MINIMUM_VALID_PRICE)
                .orElseThrow(() -> {
                    log.warn("[USE CASE] Precio rechazado: debe ser mayor a {}", MINIMUM_VALID_PRICE);
                    return new DomainException(ExceptionConstants.INVALID_PRICE_MESSAGE);
                });

        log.debug("[USE CASE] Precio válido");
    }

    private void validateRestaurantOwner(Long idUsuarioPropietario, RestaurantModel restaurant) {
        log.debug("[USE CASE] Validando propietario: usuario={}, propietarioRestaurante={}",
                idUsuarioPropietario, restaurant.getIdUsuarioPropietario());

        Optional.ofNullable(restaurant.getIdUsuarioPropietario())
                .filter(ownerId -> ownerId.equals(idUsuarioPropietario))
                .orElseThrow(() -> {
                    log.warn("[USE CASE] Usuario no es propietario del restaurante: usuario={}, propietario={}",
                            idUsuarioPropietario, restaurant.getIdUsuarioPropietario());
                    return new DomainException(ExceptionConstants.USER_NOT_RESTAURANT_OWNER_MESSAGE);
                });

        log.debug("[USE CASE] Usuario propietario validado correctamente");
    }

    private RestaurantModel getRestaurantOrThrow(Long idRestaurante) {
        log.debug("[USE CASE] Buscando restaurante: id={}", idRestaurante);

        return restaurantPersistencePort.findRestaurantById(idRestaurante)
                .orElseThrow(() -> {
                    log.warn("[USE CASE] Restaurante no encontrado: id={}", idRestaurante);
                    return new DomainException(ExceptionConstants.RESTAURANT_NOT_FOUND_MESSAGE);
                });
    }

    @Override
    public Page<PlateModel> listPlatesByRestaurant(Long restaurantId, String category, Pageable pageable) {
        log.info("[USE CASE] Listando platos del restaurante: id={}, categoria={}, page={}, size={}",
                restaurantId, category, pageable.getPageNumber(), pageable.getPageSize());

        // Validar que el restaurante existe
        getRestaurantOrThrow(restaurantId);

        if (category != null && !category.isBlank()) {
            log.debug("[USE CASE] Filtrando por categoría: {}", category);
            return platePersistencePort.findPlatesByRestaurantIdAndCategory(restaurantId, category, pageable);
        }

        return platePersistencePort.findPlatesByRestaurantId(restaurantId, pageable);
    }
}
