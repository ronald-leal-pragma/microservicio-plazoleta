package com.pragma.plazoleta.domain.usecase;

import com.pragma.plazoleta.domain.api.IRestaurantServicePort;
import com.pragma.plazoleta.domain.exception.DomainException;
import com.pragma.plazoleta.domain.exception.ExceptionConstants;
import com.pragma.plazoleta.domain.model.RestaurantModel;
import com.pragma.plazoleta.domain.model.RolModel;
import com.pragma.plazoleta.domain.model.UserModel;
import com.pragma.plazoleta.domain.spi.IRestaurantPersistencePort;
import com.pragma.plazoleta.domain.spi.IUserPersistencePort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class RestaurantUseCase implements IRestaurantServicePort {

    private static final Logger log = LoggerFactory.getLogger(RestaurantUseCase.class);
    private static final Pattern ONLY_NUMBERS_PATTERN = Pattern.compile("^[0-9]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{1,13}$");


    private final IRestaurantPersistencePort restaurantPersistencePort;
    private final IUserPersistencePort userPersistencePort;

    @Override
    public RestaurantModel saveRestaurant(RestaurantModel restaurantModel) {
        log.info("[USE CASE] Iniciando validaciones para crear restaurante: nombre={}",
                restaurantModel.getNombre());

        validateRestaurantName(restaurantModel.getNombre());

        validateNit(restaurantModel.getNit());

        validatePhone(restaurantModel.getTelefono());

        validateOwnerUser(restaurantModel.getIdUsuarioPropietario());

        validateRestaurantUniqueness(restaurantModel.getNit(), restaurantModel.getNombre());

        log.info("[USE CASE] Todas las validaciones OK, persistiendo restaurante");
        RestaurantModel saved = restaurantPersistencePort.saveRestaurant(restaurantModel);
        log.info("[USE CASE] Restaurante creado exitosamente: {}", restaurantModel.getNombre());
        return saved;
    }

    private void validateRestaurantUniqueness(String nit, String nombre) {
        log.debug("[USE CASE] Verificando duplicidad de restaurante: NIT={}, nombre={}", nit, nombre);

        Optional.ofNullable(nit)
                .filter(n -> !restaurantPersistencePort.existsRestaurantByNit(n))
                .orElseThrow(() -> {
                    log.warn("[USE CASE] NIT ya registrado: {}", nit);
                    return new DomainException(ExceptionConstants.RESTAURANT_NIT_ALREADY_EXISTS_MESSAGE);
                });

        Optional.ofNullable(nombre)
                .filter(n -> !restaurantPersistencePort.existsRestaurantByName(n))
                .orElseThrow(() -> {
                    log.warn("[USE CASE] Nombre ya registrado: {}", nombre);
                    return new DomainException(ExceptionConstants.RESTAURANT_NAME_ALREADY_EXISTS_MESSAGE);
                });

        log.debug("[USE CASE] Validación de duplicidad superada");
    }

    private void validateRestaurantName(String nombre) {
        log.debug("[USE CASE] Validando nombre del restaurante: {}", nombre);

        Optional.ofNullable(nombre)
                .filter(n -> !ONLY_NUMBERS_PATTERN.matcher(n).matches())
                .orElseThrow(() -> {
                    log.warn("[USE CASE] Nombre rechazado: es nulo o contiene solo números");
                    return new DomainException(ExceptionConstants.INVALID_RESTAURANT_NAME_MESSAGE);
                });

        log.debug("[USE CASE] Nombre del restaurante válido");
    }

    private void validateNit(String nit) {
        log.debug("[USE CASE] Validando NIT: {}", nit);

        Optional.ofNullable(nit)
                .filter(n -> ONLY_NUMBERS_PATTERN.matcher(n).matches())
                .orElseThrow(() -> {
                    log.warn("[USE CASE] NIT rechazado: debe ser únicamente numérico");
                    return new DomainException(ExceptionConstants.INVALID_NIT_MESSAGE);
                });

        log.debug("[USE CASE] NIT válido");
    }

    private void validatePhone(String telefono) {
        log.debug("[USE CASE] Validando teléfono: {}", telefono);

        Optional.ofNullable(telefono)
                .filter(t -> PHONE_PATTERN.matcher(t).matches())
                .orElseThrow(() -> {
                    log.warn("[USE CASE] Teléfono rechazado: formato inválido");
                    return new DomainException(ExceptionConstants.INVALID_PHONE_MESSAGE);
                });

        log.debug("[USE CASE] Teléfono válido");
    }

    private void validateOwnerUser(Long idUsuarioPropietario) {
        log.debug("[USE CASE] Validando usuario propietario con id={}", idUsuarioPropietario);

        UserModel user = userPersistencePort.findUserById(idUsuarioPropietario)
                .orElseThrow(() -> {
                    log.warn("[USE CASE] Usuario no encontrado: id={}", idUsuarioPropietario);
                    return new DomainException(ExceptionConstants.USER_NOT_FOUND_MESSAGE);
                });

        Optional.ofNullable(user.getRol())
                .map(RolModel::getNombre)
                .filter(ExceptionConstants.ROL_PROPIETARIO::equals)
                .orElseThrow(() -> {
                    log.warn("[USE CASE] Usuario no tiene rol de propietario: id={}", idUsuarioPropietario);
                    return new DomainException(ExceptionConstants.USER_NOT_OWNER_MESSAGE);
                });

        log.debug("[USE CASE] Usuario propietario validado correctamente");
    }

    @Override
    public Page<RestaurantModel> listRestaurants(Pageable pageable) {
        log.info("[USE CASE] Listando restaurantes: page={}, size={}", 
                pageable.getPageNumber(), pageable.getPageSize());
        return restaurantPersistencePort.findAllRestaurantsOrderByName(pageable);
    }
}
