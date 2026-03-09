package com.pragma.plazoleta.domain.usecase;

import com.pragma.plazoleta.domain.api.IRestaurantServicePort;
import com.pragma.plazoleta.domain.exception.DomainException;
import com.pragma.plazoleta.domain.exception.ExceptionConstants;
import com.pragma.plazoleta.domain.model.RestaurantModel;
import com.pragma.plazoleta.domain.model.UserModel;
import com.pragma.plazoleta.domain.spi.IRestaurantPersistencePort;
import com.pragma.plazoleta.domain.spi.IUserPersistencePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestaurantUseCase implements IRestaurantServicePort {

    private static final Logger log = LoggerFactory.getLogger(RestaurantUseCase.class);

    private final IRestaurantPersistencePort restaurantPersistencePort;
    private final IUserPersistencePort userPersistencePort;

    public RestaurantUseCase(IRestaurantPersistencePort restaurantPersistencePort,
                             IUserPersistencePort userPersistencePort) {
        this.restaurantPersistencePort = restaurantPersistencePort;
        this.userPersistencePort = userPersistencePort;
    }

    @Override
    public void saveRestaurant(RestaurantModel restaurantModel) {
        log.info("[USE CASE] Iniciando validaciones para crear restaurante: nombre={}", 
                restaurantModel.getNombre());

        // Validación 1: El nombre del restaurante puede contener números, pero no solo números
        validateRestaurantName(restaurantModel.getNombre());
        
        // Validación 2: El NIT debe ser únicamente numérico
        validateNit(restaurantModel.getNit());
        
        // Validación 3: El teléfono debe ser numérico, máximo 13 caracteres, puede contener +
        validatePhone(restaurantModel.getTelefono());
        
        // Validación 4: Validar que el usuario propietario existe y tiene el rol correcto
        validateOwnerUser(restaurantModel.getIdUsuarioPropietario());

        log.info("[USE CASE] Todas las validaciones OK, persistiendo restaurante");
        restaurantPersistencePort.saveRestaurant(restaurantModel);
        log.info("[USE CASE] Restaurante creado exitosamente: {}", restaurantModel.getNombre());
    }

    private void validateRestaurantName(String nombre) {
        log.debug("[USE CASE] Validando nombre del restaurante: {}", nombre);
        
        // Verificar que el nombre no contenga solo números
        if (nombre != null && nombre.matches("^[0-9]+$")) {
            log.warn("[USE CASE] Nombre rechazado: contiene solo números");
            throw new DomainException(ExceptionConstants.INVALID_RESTAURANT_NAME_MESSAGE);
        }
        
        log.debug("[USE CASE] Nombre del restaurante válido");
    }

    private void validateNit(String nit) {
        log.debug("[USE CASE] Validando NIT: {}", nit);
        
        // Verificar que el NIT sea únicamente numérico
        if (nit == null || !nit.matches("^[0-9]+$")) {
            log.warn("[USE CASE] NIT rechazado: debe ser únicamente numérico");
            throw new DomainException(ExceptionConstants.INVALID_NIT_MESSAGE);
        }
        
        log.debug("[USE CASE] NIT válido");
    }

    private void validatePhone(String telefono) {
        log.debug("[USE CASE] Validando teléfono: {}", telefono);
        
        // Verificar que el teléfono sea numérico, puede contener + al inicio, máximo 13 caracteres
        if (telefono == null || !telefono.matches("^\\+?[0-9]{1,13}$")) {
            log.warn("[USE CASE] Teléfono rechazado: formato inválido");
            throw new DomainException(ExceptionConstants.INVALID_PHONE_MESSAGE);
        }
        
        log.debug("[USE CASE] Teléfono válido");
    }

    private void validateOwnerUser(Long idUsuarioPropietario) {
        log.debug("[USE CASE] Validando usuario propietario con id={}", idUsuarioPropietario);
        
        // Buscar el usuario por ID
        UserModel user = userPersistencePort.findUserById(idUsuarioPropietario)
                .orElseThrow(() -> {
                    log.warn("[USE CASE] Usuario no encontrado: id={}", idUsuarioPropietario);
                    return new DomainException(ExceptionConstants.USER_NOT_FOUND_MESSAGE);
                });
        
        // Verificar que el usuario tenga el rol de PROPIETARIO
        if (user.getRol() == null || 
            !ExceptionConstants.ROL_PROPIETARIO.equals(user.getRol().getNombre())) {
            log.warn("[USE CASE] Usuario no tiene rol de propietario: id={}, rol={}", 
                    idUsuarioPropietario, 
                    user.getRol() != null ? user.getRol().getNombre() : "null");
            throw new DomainException(ExceptionConstants.USER_NOT_OWNER_MESSAGE);
        }
        
        log.debug("[USE CASE] Usuario propietario validado correctamente");
    }
}
