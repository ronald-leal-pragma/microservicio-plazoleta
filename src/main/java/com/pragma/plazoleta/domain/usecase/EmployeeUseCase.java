package com.pragma.plazoleta.domain.usecase;

import com.pragma.plazoleta.domain.api.IEmployeeServicePort;
import com.pragma.plazoleta.domain.exception.DomainException;
import com.pragma.plazoleta.domain.exception.ExceptionConstants;
import com.pragma.plazoleta.domain.model.RolModel;
import com.pragma.plazoleta.domain.model.UserModel;
import com.pragma.plazoleta.domain.spi.IRestaurantPersistencePort;
import com.pragma.plazoleta.domain.spi.IUserPersistencePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class EmployeeUseCase implements IEmployeeServicePort {

    private final IUserPersistencePort userPersistencePort;
    private final IRestaurantPersistencePort restaurantPersistencePort;

    @Override
    public UserModel createEmployee(UserModel employeeModel, Long ownerId) {
        log.info("[USE CASE] Iniciando creación de empleado para propietario id={}", ownerId);

        // Validación 1: Verificar que el usuario que intenta crear sea efectivamente un propietario
        validateOwnerRole(ownerId);

        // Validación 2: Verificar que dicho propietario tenga al menos un restaurante asociado
        validateOwnerHasRestaurant(ownerId);

        log.debug("[USE CASE] Asignando rol EMPLEADO al nuevo usuario");
        employeeModel.setRol(RolModel.builder()
                .nombre(ExceptionConstants.ROL_EMPLEADO)
                .id(ExceptionConstants.ROL_EMPLEADO_ID)
                .build());

        log.info("[USE CASE] Todas las validaciones OK, enviando a persistencia");
        UserModel created = userPersistencePort.saveUser(employeeModel);

        log.info("[USE CASE] Empleado creado exitosamente");
        return created;
    }

    private void validateOwnerRole(Long ownerId) {
        log.debug("[USE CASE] Validando rol propietario para id={}", ownerId);

        UserModel owner = userPersistencePort.findUserById(ownerId)
                .orElseThrow(() -> {
                    log.warn("[USE CASE] Propietario no encontrado: id={}", ownerId);
                    return new DomainException(ExceptionConstants.USER_NOT_FOUND_MESSAGE);
                });

        Optional.ofNullable(owner.getRol())
                .map(RolModel::getNombre)
                .filter(ExceptionConstants.ROL_PROPIETARIO::equals)
                .orElseThrow(() -> {
                    log.warn("[USE CASE] Usuario id={} no tiene rol de propietario", ownerId);
                    return new DomainException(ExceptionConstants.USER_NOT_OWNER_MESSAGE);
                });

        log.debug("[USE CASE] Rol propietario validado correctamente");
    }

    private void validateOwnerHasRestaurant(Long ownerId) {
        log.debug("[USE CASE] Validando que propietario id={} tiene restaurante", ownerId);

        Optional.ofNullable(ownerId)
                .filter(restaurantPersistencePort::existsRestaurantByOwnerId)
                .orElseThrow(() -> {
                    log.warn("[USE CASE] Propietario id={} no tiene restaurante asociado", ownerId);
                    return new DomainException(ExceptionConstants.OWNER_WITHOUT_RESTAURANT_MESSAGE);
                });

        log.debug("[USE CASE] Propietario tiene restaurante asociado");
    }
}