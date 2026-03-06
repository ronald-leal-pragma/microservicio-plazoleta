package com.pragma.plazoleta.infrastructure.out.jpa.mapper;

import com.pragma.plazoleta.domain.model.UserModel;
import com.pragma.plazoleta.infrastructure.out.jpa.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface IUserEntityMapper {

    @Mapping(target = "rol.id", source = "rol.id")
    @Mapping(target = "rol.nombre", source = "rol.nombre")
    @Mapping(target = "rol.descripcion", source = "rol.descripcion")
    UserEntity toEntity(UserModel userModel);

    @Mapping(target = "rol.id", source = "rol.id")
    @Mapping(target = "rol.nombre", source = "rol.nombre")
    UserModel toModel(UserEntity userEntity);
}
