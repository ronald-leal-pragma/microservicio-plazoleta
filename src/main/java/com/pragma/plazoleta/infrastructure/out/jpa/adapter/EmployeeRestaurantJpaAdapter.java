package com.pragma.plazoleta.infrastructure.out.jpa.adapter;

import com.pragma.plazoleta.domain.model.EmployeeRestaurantModel;
import com.pragma.plazoleta.domain.spi.IEmployeeRestaurantPersistencePort;
import com.pragma.plazoleta.infrastructure.out.jpa.mapper.IEmployeeRestaurantEntityMapper;
import com.pragma.plazoleta.infrastructure.out.jpa.repository.IEmployeeRestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class EmployeeRestaurantJpaAdapter implements IEmployeeRestaurantPersistencePort {

    private final IEmployeeRestaurantRepository repository;
    private final IEmployeeRestaurantEntityMapper mapper;

    @Override
    public EmployeeRestaurantModel save(EmployeeRestaurantModel model) {
        log.debug("[JPA ADAPTER] Guardando relación empleado-restaurante: empleado={}, restaurante={}",
                model.getIdEmpleado(), model.getIdRestaurante());
        var saved = repository.save(mapper.toEntity(model));
        log.debug("[JPA ADAPTER] Relación guardada con id={}", saved.getId());
        return mapper.toModel(saved);
    }

    @Override
    public Optional<EmployeeRestaurantModel> findByEmployeeId(Long employeeId) {
        log.debug("[JPA ADAPTER] Buscando restaurante del empleado: {}", employeeId);
        return repository.findByIdEmpleado(employeeId)
                .map(mapper::toModel);
    }

    @Override
    public boolean existsByEmployeeAndRestaurant(Long employeeId, Long restaurantId) {
        log.debug("[JPA ADAPTER] Verificando relación empleado={}, restaurante={}", employeeId, restaurantId);
        return repository.existsByIdEmpleadoAndIdRestaurante(employeeId, restaurantId);
    }
}
