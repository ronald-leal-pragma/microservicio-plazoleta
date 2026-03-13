package com.pragma.plazoleta.infrastructure.out.jpa.adapter;

import com.pragma.plazoleta.domain.model.PlateModel;
import com.pragma.plazoleta.infrastructure.out.jpa.entity.PlateEntity;
import com.pragma.plazoleta.infrastructure.out.jpa.mapper.IPlateEntityMapper;
import com.pragma.plazoleta.infrastructure.out.jpa.repository.IPlateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlateJpaAdapterTest {

    @Mock
    private IPlateRepository plateRepository;

    @Mock
    private IPlateEntityMapper plateEntityMapper;

    @InjectMocks
    private PlateJpaAdapter plateJpaAdapter;

    private PlateModel plateModel;
    private PlateEntity plateEntity;

    @BeforeEach
    void setUp() {
        plateModel = PlateModel.builder()
                .id(1L)
                .nombre("Bandeja Paisa")
                .precio(25000)
                .descripcion("Plato típico")
                .activa(true)
                .idRestaurante(2L)
                .build();

        plateEntity = new PlateEntity();
        plateEntity.setId(1L);
        plateEntity.setNombre("Bandeja Paisa");
        plateEntity.setPrecio(25000);
    }


    @Test
    @DisplayName("Debe guardar y retornar el modelo mapeado del plato")
    void savePlate_shouldSaveAndReturnMappedModel() {
        when(plateEntityMapper.toEntity(plateModel)).thenReturn(plateEntity);
        when(plateRepository.save(plateEntity)).thenReturn(plateEntity);
        when(plateEntityMapper.toModel(plateEntity)).thenReturn(plateModel);

        PlateModel result = plateJpaAdapter.savePlate(plateModel);

        assertNotNull(result);
        assertEquals("Bandeja Paisa", result.getNombre());
        verify(plateRepository).save(plateEntity);
    }


    @Test
    @DisplayName("Debe retornar true cuando el plato ya existe en el restaurante")
    void existsPlateByNameAndRestaurantId_shouldReturnTrueWhenExists() {
        when(plateRepository.existsByNombreAndIdRestaurante("Bandeja Paisa", 2L)).thenReturn(true);

        boolean result = plateJpaAdapter.existsPlateByNameAndRestaurantId("Bandeja Paisa", 2L);

        assertTrue(result);
    }

    @Test
    @DisplayName("Debe retornar false cuando el plato no existe en el restaurante")
    void existsPlateByNameAndRestaurantId_shouldReturnFalseWhenNotExists() {
        when(plateRepository.existsByNombreAndIdRestaurante("Ajiaco", 2L)).thenReturn(false);

        boolean result = plateJpaAdapter.existsPlateByNameAndRestaurantId("Ajiaco", 2L);

        assertFalse(result);
    }


    @Test
    @DisplayName("Debe retornar Optional con el modelo cuando el plato existe")
    void findPlateById_shouldReturnOptionalWhenFound() {
        when(plateRepository.findById(1L)).thenReturn(Optional.of(plateEntity));
        when(plateEntityMapper.toModel(plateEntity)).thenReturn(plateModel);

        Optional<PlateModel> result = plateJpaAdapter.findPlateById(1L);

        assertTrue(result.isPresent());
        assertEquals("Bandeja Paisa", result.get().getNombre());
    }

    @Test
    @DisplayName("Debe retornar Optional vacío cuando el plato no existe")
    void findPlateById_shouldReturnEmptyWhenNotFound() {
        when(plateRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<PlateModel> result = plateJpaAdapter.findPlateById(99L);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Debe actualizar y retornar el modelo mapeado del plato")
    void updatePlate_shouldUpdateAndReturnMappedModel() {
        plateModel.setPrecio(30000);
        when(plateEntityMapper.toEntity(plateModel)).thenReturn(plateEntity);
        when(plateRepository.save(plateEntity)).thenReturn(plateEntity);
        when(plateEntityMapper.toModel(plateEntity)).thenReturn(plateModel);

        PlateModel result = plateJpaAdapter.updatePlate(plateModel);

        assertNotNull(result);
        verify(plateRepository).save(plateEntity);
    }


    @Test
    @DisplayName("Debe retornar página de platos activos del restaurante")
    void findPlatesByRestaurantId_shouldReturnActivePlates() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<PlateEntity> entityPage = new PageImpl<>(List.of(plateEntity));
        when(plateRepository.findByIdRestauranteAndActivaTrue(2L, pageable)).thenReturn(entityPage);
        when(plateEntityMapper.toModel(plateEntity)).thenReturn(plateModel);

        Page<PlateModel> result = plateJpaAdapter.findPlatesByRestaurantId(2L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Bandeja Paisa", result.getContent().get(0).getNombre());
    }

    @Test
    @DisplayName("Debe retornar página vacía cuando no hay platos activos")
    void findPlatesByRestaurantId_shouldReturnEmptyPageWhenNoActivePlates() {
        Pageable pageable = PageRequest.of(0, 10);
        when(plateRepository.findByIdRestauranteAndActivaTrue(2L, pageable))
                .thenReturn(new PageImpl<>(List.of()));

        Page<PlateModel> result = plateJpaAdapter.findPlatesByRestaurantId(2L, pageable);

        assertTrue(result.getContent().isEmpty());
    }

    @Test
    @DisplayName("Debe retornar página de platos filtrados por categoría")
    void findPlatesByRestaurantIdAndCategory_shouldReturnFilteredPlates() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<PlateEntity> entityPage = new PageImpl<>(List.of(plateEntity));
        when(plateRepository.findByIdRestauranteAndCategoriaAndActivaTrue(2L, "Típico", pageable))
                .thenReturn(entityPage);
        when(plateEntityMapper.toModel(plateEntity)).thenReturn(plateModel);

        Page<PlateModel> result = plateJpaAdapter.findPlatesByRestaurantIdAndCategory(2L, "Típico", pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    @DisplayName("Debe retornar página vacía cuando no hay platos en la categoría")
    void findPlatesByRestaurantIdAndCategory_shouldReturnEmptyWhenNoCategoryPlates() {
        Pageable pageable = PageRequest.of(0, 10);
        when(plateRepository.findByIdRestauranteAndCategoriaAndActivaTrue(2L, "Postres", pageable))
                .thenReturn(new PageImpl<>(List.of()));

        Page<PlateModel> result = plateJpaAdapter.findPlatesByRestaurantIdAndCategory(2L, "Postres", pageable);

        assertTrue(result.getContent().isEmpty());
    }
}

