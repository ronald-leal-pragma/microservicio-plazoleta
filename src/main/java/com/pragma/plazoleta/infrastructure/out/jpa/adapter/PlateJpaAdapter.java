package com.pragma.plazoleta.infrastructure.out.jpa.adapter;

import com.pragma.plazoleta.domain.model.PlateModel;
import com.pragma.plazoleta.domain.spi.IPlatePersistencePort;
import com.pragma.plazoleta.infrastructure.exception.NoDataFoundException;
import com.pragma.plazoleta.infrastructure.out.jpa.entity.PlateEntity;
import com.pragma.plazoleta.infrastructure.out.jpa.mapper.IPlateEntityMapper;
import com.pragma.plazoleta.infrastructure.out.jpa.repository.IPlateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class PlateJpaAdapter implements IPlatePersistencePort {

    private final IPlateRepository plateRepository;
    private final IPlateEntityMapper plateEntityMapper;

    @Override
    public void savePlate(PlateModel plateModel) {
        log.info("[JPA ADAPTER] Verificando si el plato existe: nombre={}, restaurante={}",
                plateModel.getNombre(), plateModel.getIdRestaurante());

        if (plateRepository.existsByNombreAndIdRestaurante(
                plateModel.getNombre(), plateModel.getIdRestaurante())) {
            log.warn("[JPA ADAPTER] Plato ya existe en este restaurante: nombre={}", 
                    plateModel.getNombre());
            throw new NoDataFoundException();
        }

        log.debug("[JPA ADAPTER] Mapeando modelo a entidad");
        PlateEntity plateEntity = plateEntityMapper.toEntity(plateModel);
        
        log.debug("[JPA ADAPTER] Guardando plato en base de datos");
        plateRepository.save(plateEntity);
        
        log.info("[JPA ADAPTER] Plato guardado exitosamente: nombre={}, precio={}",
                plateModel.getNombre(), plateModel.getPrecio());
    }

    @Override
    public Optional<PlateModel> findPlateById(Long id) {
        log.debug("[JPA ADAPTER] Buscando plato con id={}", id);
        return plateRepository.findById(id)
                .map(plateEntityMapper::toModel);
    }

    @Override
    public void updatePlate(PlateModel plateModel) {
        log.info("[JPA ADAPTER] Actualizando plato: id={}", plateModel.getId());
        
        PlateEntity plateEntity = plateEntityMapper.toEntity(plateModel);
        plateRepository.save(plateEntity);
        
        log.info("[JPA ADAPTER] Plato actualizado exitosamente: id={}, precio={}",
                plateModel.getId(), plateModel.getPrecio());
    }
}
