package com.pragma.plazoleta.infrastructure.out.jpa.adapter;

import com.pragma.plazoleta.domain.model.UserModel;
import com.pragma.plazoleta.domain.spi.IUserPersistencePort;
import com.pragma.plazoleta.infrastructure.exception.UserAlreadyExistsException;
import com.pragma.plazoleta.infrastructure.out.jpa.entity.RolEntity;
import com.pragma.plazoleta.infrastructure.out.jpa.entity.UserEntity;
import com.pragma.plazoleta.infrastructure.out.jpa.mapper.IUserEntityMapper;
import com.pragma.plazoleta.infrastructure.out.jpa.repository.IRolRepository;
import com.pragma.plazoleta.infrastructure.out.jpa.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class UserJpaAdapter implements IUserPersistencePort {

    private final IUserRepository userRepository;
    private final IRolRepository rolRepository;
    private final IUserEntityMapper userEntityMapper;

    @Override
    public void saveUser(UserModel userModel) {
        log.info("[JPA ADAPTER] Verificando duplicados para correo={} y documento={}",
                userModel.getCorreo(), userModel.getDocumentoDeIdentidad());

        if (userRepository.existsByCorreo(userModel.getCorreo())) {
            log.warn("[JPA ADAPTER] Correo ya registrado: {}", userModel.getCorreo());
            throw new UserAlreadyExistsException();
        }
        if (userRepository.existsByDocumentoDeIdentidad(userModel.getDocumentoDeIdentidad())) {
            log.warn("[JPA ADAPTER] Documento ya registrado: {}", userModel.getDocumentoDeIdentidad());
            throw new UserAlreadyExistsException();
        }

        Long rolId = userModel.getRol().getId();
        log.debug("[JPA ADAPTER] Buscando rol con id={}", rolId);
        RolEntity rolEntity = rolRepository.findById(rolId)
                .orElseGet(() -> {
                    log.info("[JPA ADAPTER] Rol no encontrado, creando nuevo rol: {}", userModel.getRol().getNombre());
                    RolEntity newRol = new RolEntity();
                    newRol.setId(rolId);
                    newRol.setNombre(userModel.getRol().getNombre());
                    newRol.setDescripcion(userModel.getRol().getDescripcion());
                    return rolRepository.save(newRol);
                });

        log.debug("[JPA ADAPTER] Rol asignado: {}", rolEntity.getNombre());
        UserEntity userEntity = userEntityMapper.toEntity(userModel);
        userEntity.setRol(rolEntity);
        userRepository.save(userEntity);
        log.info("[JPA ADAPTER] Usuario guardado exitosamente con correo={}", userModel.getCorreo());
    }

    @Override
    public Optional<UserModel> findUserById(Long id) {
        log.debug("[JPA ADAPTER] Buscando usuario con id={}", id);
        return userRepository.findById(id)
                .map(userEntityMapper::toModel);
    }
}

