package com.pragma.plazoleta.application.handler;

import com.pragma.plazoleta.application.dto.request.RestaurantRequestDto;
import com.pragma.plazoleta.application.dto.response.PaginatedResponseDto;
import com.pragma.plazoleta.application.dto.response.RestaurantListResponseDto;
import com.pragma.plazoleta.application.dto.response.RestaurantResponseDto;
import com.pragma.plazoleta.application.handler.impl.RestaurantHandler;
import com.pragma.plazoleta.application.mapper.IRestaurantRequestMapper;
import com.pragma.plazoleta.domain.api.IRestaurantServicePort;
import com.pragma.plazoleta.domain.exception.DomainException;
import com.pragma.plazoleta.domain.exception.ExceptionConstants;
import com.pragma.plazoleta.domain.model.RestaurantModel;
import com.pragma.plazoleta.domain.model.UserModel;
import com.pragma.plazoleta.domain.spi.IUserPersistencePort;
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

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantHandlerTest {

    @Mock
    private IRestaurantServicePort restaurantServicePort;

    @Mock
    private IRestaurantRequestMapper restaurantRequestMapper;

    @Mock
    private IUserPersistencePort userPersistencePort;

    @InjectMocks
    private RestaurantHandler restaurantHandler;

    private static final String CORREO_PROPIETARIO = "propietario@mail.com";
    private static final Long PROPIETARIO_ID = 1L;

    private RestaurantRequestDto requestDto;
    private RestaurantModel restaurantModel;
    private UserModel propietario;

    @BeforeEach
    void setUp() {
        requestDto = new RestaurantRequestDto();
        requestDto.setNombre("Restaurante Test");
        requestDto.setNit("123456789");
        requestDto.setDireccion("Calle 10 # 20-30");
        requestDto.setTelefono("+573001234567");
        requestDto.setUrlLogo("http://logo.com/logo.png");
        requestDto.setCorreoPropietario(CORREO_PROPIETARIO);

        restaurantModel = new RestaurantModel();
        restaurantModel.setId(1L);
        restaurantModel.setNombre("Restaurante Test");
        restaurantModel.setNit("123456789");
        restaurantModel.setCreadoEn(Instant.now());
        restaurantModel.setUrlLogo("http://logo.com/logo.png");
        restaurantModel.setIdUsuarioPropietario(PROPIETARIO_ID);

        propietario = UserModel.builder()
                .id(PROPIETARIO_ID)
                .nombre("Juan")
                .correo(CORREO_PROPIETARIO)
                .build();
    }

    @Test
    @DisplayName("Debe retornar RestaurantResponseDto cuando el restaurante es creado")
    void saveRestaurant_shouldReturnResponseDtoWhenCreated() {
        when(userPersistencePort.findUserByEmail(CORREO_PROPIETARIO)).thenReturn(Optional.of(propietario));
        when(restaurantRequestMapper.toRestaurant(requestDto)).thenReturn(restaurantModel);
        when(restaurantServicePort.saveRestaurant(restaurantModel)).thenReturn(restaurantModel);

        RestaurantResponseDto result = restaurantHandler.saveRestaurant(requestDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Restaurante Test", result.getNombre());
        assertEquals("123456789", result.getNit());
        assertNotNull(result.getCreadoEn());
    }

    @Test
    @DisplayName("Debe asignar el ID del propietario al modelo antes de guardar")
    void saveRestaurant_shouldSetOwnerIdFromFoundUser() {
        when(userPersistencePort.findUserByEmail(CORREO_PROPIETARIO)).thenReturn(Optional.of(propietario));
        when(restaurantRequestMapper.toRestaurant(requestDto)).thenReturn(restaurantModel);
        when(restaurantServicePort.saveRestaurant(any())).thenReturn(restaurantModel);

        restaurantHandler.saveRestaurant(requestDto);

        assertEquals(PROPIETARIO_ID, restaurantModel.getIdUsuarioPropietario());
    }

    @Test
    @DisplayName("Debe lanzar DomainException cuando el propietario no existe por correo")
    void saveRestaurant_shouldThrowWhenOwnerNotFoundByEmail() {
        when(userPersistencePort.findUserByEmail(CORREO_PROPIETARIO)).thenReturn(Optional.empty());

        DomainException ex = assertThrows(DomainException.class,
                () -> restaurantHandler.saveRestaurant(requestDto));

        assertEquals(ExceptionConstants.USER_NOT_FOUND_MESSAGE, ex.getMessage());
        verify(restaurantServicePort, never()).saveRestaurant(any());
    }

    @Test
    @DisplayName("Debe retornar creadoEn nulo cuando el modelo no tiene fecha")
    void saveRestaurant_shouldReturnNullCreadoEnWhenModelHasNullDate() {
        restaurantModel.setCreadoEn(null);
        when(userPersistencePort.findUserByEmail(CORREO_PROPIETARIO)).thenReturn(Optional.of(propietario));
        when(restaurantRequestMapper.toRestaurant(requestDto)).thenReturn(restaurantModel);
        when(restaurantServicePort.saveRestaurant(restaurantModel)).thenReturn(restaurantModel);

        RestaurantResponseDto result = restaurantHandler.saveRestaurant(requestDto);

        assertNull(result.getCreadoEn());
    }

    @Test
    @DisplayName("Debe llamar al servicio, mapper y puerto de usuario al guardar restaurante")
    void saveRestaurant_shouldCallServiceMapperAndUserPort() {
        when(userPersistencePort.findUserByEmail(CORREO_PROPIETARIO)).thenReturn(Optional.of(propietario));
        when(restaurantRequestMapper.toRestaurant(requestDto)).thenReturn(restaurantModel);
        when(restaurantServicePort.saveRestaurant(restaurantModel)).thenReturn(restaurantModel);

        restaurantHandler.saveRestaurant(requestDto);

        verify(userPersistencePort).findUserByEmail(CORREO_PROPIETARIO);
        verify(restaurantRequestMapper).toRestaurant(requestDto);
        verify(restaurantServicePort).saveRestaurant(restaurantModel);
    }

    @Test
    @DisplayName("Debe retornar página de restaurantes con información básica")
    void listRestaurants_shouldReturnPaginatedResponse() {
        RestaurantModel r1 = new RestaurantModel();
        r1.setNombre("Restaurante A");
        r1.setUrlLogo("http://logo-a.com");

        RestaurantModel r2 = new RestaurantModel();
        r2.setNombre("Restaurante B");
        r2.setUrlLogo("http://logo-b.com");

        Page<RestaurantModel> page = new PageImpl<>(List.of(r1, r2), PageRequest.of(0, 10), 2);
        when(restaurantServicePort.listRestaurants(any())).thenReturn(page);

        PaginatedResponseDto<RestaurantListResponseDto> result = restaurantHandler.listRestaurants(0, 10);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(0, result.getPageNumber());
        assertEquals(10, result.getPageSize());
        assertEquals(2L, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertTrue(result.isLast());
        assertEquals("Restaurante A", result.getContent().get(0).getNombre());
        assertEquals("Restaurante B", result.getContent().get(1).getNombre());
    }

    @Test
    @DisplayName("Debe retornar página vacía cuando no hay restaurantes")
    void listRestaurants_shouldReturnEmptyPageWhenNoData() {
        Page<RestaurantModel> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(restaurantServicePort.listRestaurants(any())).thenReturn(emptyPage);

        PaginatedResponseDto<RestaurantListResponseDto> result = restaurantHandler.listRestaurants(0, 10);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0L, result.getTotalElements());
    }

    @Test
    @DisplayName("Debe pasar los parámetros de paginación correctamente al servicio")
    void listRestaurants_shouldPassPaginationParamsToService() {
        Page<RestaurantModel> page = new PageImpl<>(List.of(), PageRequest.of(2, 5), 0);
        when(restaurantServicePort.listRestaurants(PageRequest.of(2, 5))).thenReturn(page);

        restaurantHandler.listRestaurants(2, 5);

        verify(restaurantServicePort).listRestaurants(PageRequest.of(2, 5));
    }
}
