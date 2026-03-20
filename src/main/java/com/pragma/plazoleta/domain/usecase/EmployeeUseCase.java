package com.pragma.plazoleta.domain.usecase;

import com.pragma.plazoleta.domain.api.IEmployeeServicePort;
import com.pragma.plazoleta.domain.exception.DomainException;
import com.pragma.plazoleta.domain.exception.message.EmployeeErrorMessages;
import com.pragma.plazoleta.domain.exception.message.RestaurantErrorMessages;
import com.pragma.plazoleta.domain.exception.message.UserErrorMessages;
import com.pragma.plazoleta.domain.model.EmployeeRestaurantModel;
import com.pragma.plazoleta.domain.model.RolModel;
import com.pragma.plazoleta.domain.model.RoleType;
import com.pragma.plazoleta.domain.model.UserModel;
import com.pragma.plazoleta.domain.spi.IEmployeeRestaurantPersistencePort;
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
    private final IEmployeeRestaurantPersistencePort employeeRestaurantPersistencePort;

    @Override
    public UserModel createEmployee(UserModel employeeModel, Long ownerId, Long restaurantId) {
        log.info("[USE CASE] Iniciando creación de empleado para propietario id={}, restaurante id={}", 
                ownerId, restaurantId);

        validateOwnerRole(ownerId);

        validateRestaurantBelongsToOwner(restaurantId, ownerId);

        log.debug("[USE CASE] Asignando rol EMPLEADO al nuevo usuario");
        employeeModel.setRol(RoleType.EMPLEADO.toModel());

        log.info("[USE CASE] Todas las validaciones OK, enviando a persistencia");
        UserModel created = userPersistencePort.saveUser(employeeModel);

        validateEmployeeNotAlreadyAssigned(created.getId(), restaurantId);

        log.info("[USE CASE] Guardando relación empleado-restaurante");
        EmployeeRestaurantModel employeeRestaurant = EmployeeRestaurantModel.builder()
                .idEmpleado(created.getId())
                .idRestaurante(restaurantId)
                .build();
        employeeRestaurantPersistencePort.save(employeeRestaurant);

        log.info("[USE CASE] Empleado creado exitosamente con id={}", created.getId());
        return created;
    }

    private void validateRestaurantBelongsToOwner(Long restaurantId, Long ownerId) {
        log.debug("[USE CASE] Validando que restaurante id={} pertenece al propietario id={}", 
                restaurantId, ownerId);

        restaurantPersistencePort.findRestaurantById(restaurantId)
                .filter(r -> r.getIdUsuarioPropietario().equals(ownerId))
                .orElseThrow(() -> {
                    log.warn("[USE CASE] Restaurante no pertenece al propietario o no existe");
                    return new DomainException(RestaurantErrorMessages.NOT_BELONGS_TO_OWNER);
                });

        log.debug("[USE CASE] Restaurante validado correctamente");
    }

    private void validateEmployeeNotAlreadyAssigned(Long employeeId, Long restaurantId) {
        log.debug("[USE CASE] Validando que empleado id={} no esté asignado a restaurante id={}", 
                employeeId, restaurantId);

        if (employeeRestaurantPersistencePort.existsByEmployeeAndRestaurant(employeeId, restaurantId)) {
            log.warn("[USE CASE] Empleado ya está asignado al restaurante");
            throw new DomainException(EmployeeErrorMessages.ALREADY_ASSIGNED_TO_RESTAURANT);
        }
    }

    private void validateOwnerRole(Long ownerId) {
        log.debug("[USE CASE] Validando rol propietario para id={}", ownerId);

        UserModel owner = userPersistencePort.findUserById(ownerId)
                .orElseThrow(() -> {
                    log.warn("[USE CASE] Propietario no encontrado: id={}", ownerId);
                    return new DomainException(UserErrorMessages.USER_NOT_FOUND);
                });

        Optional.ofNullable(owner.getRol())
                .map(RolModel::getNombre)
                .filter(RoleType.PROPIETARIO.getNombre()::equals)
                .orElseThrow(() -> {
                    log.warn("[USE CASE] Usuario id={} no tiene rol de propietario", ownerId);
                    return new DomainException(UserErrorMessages.USER_NOT_OWNER);
                });

        log.debug("[USE CASE] Rol propietario validado correctamente");
    }
}