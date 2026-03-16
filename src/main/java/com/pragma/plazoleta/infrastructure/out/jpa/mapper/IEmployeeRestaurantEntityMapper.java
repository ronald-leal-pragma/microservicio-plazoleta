package com.pragma.plazoleta.infrastructure.out.jpa.mapper;

import com.pragma.plazoleta.domain.model.EmployeeRestaurantModel;
import com.pragma.plazoleta.infrastructure.out.jpa.entity.EmployeeRestaurantEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface IEmployeeRestaurantEntityMapper {
    
    EmployeeRestaurantEntity toEntity(EmployeeRestaurantModel model);
    
    EmployeeRestaurantModel toModel(EmployeeRestaurantEntity entity);
}
