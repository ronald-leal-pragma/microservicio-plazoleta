package com.pragma.plazoleta.domain.spi;

import com.pragma.plazoleta.domain.model.EmployeeRestaurantModel;

import java.util.Optional;

public interface IEmployeeRestaurantPersistencePort {
    
    EmployeeRestaurantModel save(EmployeeRestaurantModel model);
    
    Optional<EmployeeRestaurantModel> findByEmployeeId(Long employeeId);
    
    boolean existsByEmployeeAndRestaurant(Long employeeId, Long restaurantId);
}
