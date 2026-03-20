package com.pragma.plazoleta.infrastructure.out.jpa.adapter;

import com.pragma.plazoleta.domain.model.RestaurantModel;
import com.pragma.plazoleta.infrastructure.out.jpa.entity.RestaurantEntity;
import com.pragma.plazoleta.infrastructure.out.jpa.mapper.IRestaurantEntityMapper;
import com.pragma.plazoleta.infrastructure.out.jpa.repository.IRestaurantRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantJpaAdapterTest {

    @Mock
    private IRestaurantRepository restaurantRepository;

    @Mock
    private IRestaurantEntityMapper restaurantEntityMapper;

    @InjectMocks
    private RestaurantJpaAdapter restaurantJpaAdapter;

    private RestaurantModel restaurantModel;
    private RestaurantEntity restaurantEntity;

    @BeforeEach
    void setUp() {
        restaurantModel = new RestaurantModel();
        restaurantModel.setId(1L);
        restaurantModel.setNombre("Restaurante Test");
        restaurantModel.setNit("123456789");

        restaurantEntity = new RestaurantEntity();
        restaurantEntity.setId(1L);
        restaurantEntity.setNombre("Restaurante Test");
        restaurantEntity.setNit("123456789");
    }

    @Test
    @DisplayName("Debe guardar y retornar el modelo mapeado del restaurante")
    void saveRestaurant_shouldSaveAndReturnMappedModel() {
        when(restaurantEntityMapper.toEntity(restaurantModel)).thenReturn(restaurantEntity);
        when(restaurantRepository.save(restaurantEntity)).thenReturn(restaurantEntity);
        when(restaurantEntityMapper.toModel(restaurantEntity)).thenReturn(restaurantModel);

        RestaurantModel result = restaurantJpaAdapter.saveRestaurant(restaurantModel);

        assertNotNull(result);
        assertEquals("Restaurante Test", result.getNombre());
        verify(restaurantRepository).save(restaurantEntity);
    }

    @Test
    @DisplayName("Debe retornar true cuando el NIT ya existe")
    void existsRestaurantByNit_shouldReturnTrueWhenNitExists() {
        when(restaurantRepository.existsByNit("123456789")).thenReturn(true);

        boolean result = restaurantJpaAdapter.existsRestaurantByNit("123456789");

        assertTrue(result);
    }

    @Test
    @DisplayName("Debe retornar false cuando el NIT no existe")
    void existsRestaurantByNit_shouldReturnFalseWhenNitNotExists() {
        when(restaurantRepository.existsByNit("000000000")).thenReturn(false);

        boolean result = restaurantJpaAdapter.existsRestaurantByNit("000000000");

        assertFalse(result);
    }

    @Test
    @DisplayName("Debe retornar true cuando el nombre ya existe")
    void existsRestaurantByName_shouldReturnTrueWhenNameExists() {
        when(restaurantRepository.existsByNombre("Restaurante Test")).thenReturn(true);

        boolean result = restaurantJpaAdapter.existsRestaurantByName("Restaurante Test");

        assertTrue(result);
    }

    @Test
    @DisplayName("Debe retornar false cuando el nombre no existe")
    void existsRestaurantByName_shouldReturnFalseWhenNameNotExists() {
        when(restaurantRepository.existsByNombre("Nuevo")).thenReturn(false);

        boolean result = restaurantJpaAdapter.existsRestaurantByName("Nuevo");

        assertFalse(result);
    }


    @Test
    @DisplayName("Debe retornar Optional con el modelo cuando el restaurante existe")
    void findRestaurantById_shouldReturnOptionalWhenFound() {
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurantEntity));
        when(restaurantEntityMapper.toModel(restaurantEntity)).thenReturn(restaurantModel);

        Optional<RestaurantModel> result = restaurantJpaAdapter.findRestaurantById(1L);

        assertTrue(result.isPresent());
        assertEquals("Restaurante Test", result.get().getNombre());
    }

    @Test
    @DisplayName("Debe retornar Optional vacío cuando el restaurante no existe")
    void findRestaurantById_shouldReturnEmptyWhenNotFound() {
        when(restaurantRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<RestaurantModel> result = restaurantJpaAdapter.findRestaurantById(99L);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Debe retornar true cuando el propietario tiene restaurante")
    void existsRestaurantByOwnerId_shouldReturnTrueWhenOwnerHasRestaurant() {
        when(restaurantRepository.existsByIdUsuarioPropietario(10L)).thenReturn(true);

        boolean result = restaurantJpaAdapter.existsRestaurantByOwnerId(10L);

        assertTrue(result);
    }

    @Test
    @DisplayName("Debe retornar false cuando el propietario no tiene restaurante")
    void existsRestaurantByOwnerId_shouldReturnFalseWhenOwnerHasNoRestaurant() {
        when(restaurantRepository.existsByIdUsuarioPropietario(99L)).thenReturn(false);

        boolean result = restaurantJpaAdapter.existsRestaurantByOwnerId(99L);

        assertFalse(result);
    }

    @Test
    @DisplayName("Debe retornar página de modelos mapeados ordenados por nombre")
    void findAllRestaurantsOrderByName_shouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<RestaurantEntity> entityPage = new PageImpl<>(List.of(restaurantEntity));
        when(restaurantRepository.findAllByOrderByNombreAsc(pageable)).thenReturn(entityPage);
        when(restaurantEntityMapper.toModel(restaurantEntity)).thenReturn(restaurantModel);

        Page<RestaurantModel> result = restaurantJpaAdapter.findAllRestaurantsOrderByName(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Restaurante Test", result.getContent().get(0).getNombre());
    }

    @Test
    @DisplayName("Debe retornar página vacía cuando no hay restaurantes")
    void findAllRestaurantsOrderByName_shouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<RestaurantEntity> emptyPage = new PageImpl<>(List.of());
        when(restaurantRepository.findAllByOrderByNombreAsc(pageable)).thenReturn(emptyPage);

        Page<RestaurantModel> result = restaurantJpaAdapter.findAllRestaurantsOrderByName(pageable);

        assertTrue(result.getContent().isEmpty());
    }
}

