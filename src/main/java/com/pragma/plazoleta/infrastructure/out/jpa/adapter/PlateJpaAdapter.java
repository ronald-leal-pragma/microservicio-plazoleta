package com.pragma.plazoleta.infrastructure.out.jpa.adapter;

import com.pragma.plazoleta.domain.model.PlateModel;
import com.pragma.plazoleta.domain.spi.IPlatePersistencePort;
import com.pragma.plazoleta.infrastructure.out.jpa.entity.PlateEntity;
import com.pragma.plazoleta.infrastructure.out.jpa.mapper.IPlateEntityMapper;
import com.pragma.plazoleta.infrastructure.out.jpa.repository.IPlateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class PlateJpaAdapter implements IPlatePersistencePort {

    private final IPlateRepository plateRepository;
    private final IPlateEntityMapper plateEntityMapper;

    @Override
    public PlateModel savePlate(PlateModel plateModel) {
        log.debug("[JPA ADAPTER] Mapeando modelo a entidad");
        PlateEntity plateEntity = plateEntityMapper.toEntity(plateModel);

        log.debug("[JPA ADAPTER] Guardando plato en base de datos");
        PlateEntity saved = plateRepository.save(plateEntity);

        log.info("[JPA ADAPTER] Plato guardado exitosamente: nombre={}, precio={}",
                plateModel.getNombre(), plateModel.getPrecio());

        return plateEntityMapper.toModel(saved);
    }

    @Override
    public boolean existsPlateByNameAndRestaurantId(String name, Long restaurantId) {
        log.debug("[JPA ADAPTER] Consultando existencia de plato: nombre={}, restaurante={}", name, restaurantId);
        return plateRepository.existsByNombreAndIdRestaurante(name, restaurantId);
    }

    @Override
    public Optional<PlateModel> findPlateById(Long id) {
        log.debug("[JPA ADAPTER] Buscando plato con id={}", id);
        return plateRepository.findById(id)
                .map(plateEntityMapper::toModel);
    }

    @Override
    public PlateModel updatePlate(PlateModel plateModel) {
        log.info("[JPA ADAPTER] Actualizando plato: id={}", plateModel.getId());
        
        PlateEntity plateEntity = plateEntityMapper.toEntity(plateModel);
        PlateEntity saved = plateRepository.save(plateEntity);
        
        log.info("[JPA ADAPTER] Plato actualizado exitosamente: id={}, precio={}",
                plateModel.getId(), plateModel.getPrecio());
        return plateEntityMapper.toModel(saved);
    }

    @Override
    public Page<PlateModel> findPlatesByRestaurantId(Long restaurantId, Pageable pageable) {
        log.debug("[JPA ADAPTER] Buscando platos del restaurante: id={}, page={}, size={}",
                restaurantId, pageable.getPageNumber(), pageable.getPageSize());
        return plateRepository.findByIdRestauranteAndActivaTrue(restaurantId, pageable)
                .map(plateEntityMapper::toModel);
    }

    @Override
    public Page<PlateModel> findPlatesByRestaurantIdAndCategory(Long restaurantId, String category, Pageable pageable) {
        log.debug("[JPA ADAPTER] Buscando platos del restaurante por categoría: id={}, categoria={}, page={}, size={}",
                restaurantId, category, pageable.getPageNumber(), pageable.getPageSize());
        return plateRepository.findByIdRestauranteAndCategoriaAndActivaTrue(restaurantId, category, pageable)
                .map(plateEntityMapper::toModel);
    }
}
