package com.pragma.plazoleta.application.handler;

import com.pragma.plazoleta.application.dto.request.PlateRequestDto;
import com.pragma.plazoleta.application.dto.request.PlateUpdateRequestDto;
import com.pragma.plazoleta.application.dto.response.PaginatedResponseDto;
import com.pragma.plazoleta.application.dto.response.PlateListResponseDto;
import com.pragma.plazoleta.application.dto.response.PlateResponseDto;
import com.pragma.plazoleta.application.handler.impl.PlateHandler;
import com.pragma.plazoleta.application.mapper.IPlateRequestMapper;
import com.pragma.plazoleta.domain.api.IPlateServicePort;
import com.pragma.plazoleta.domain.model.PlateModel;
import com.pragma.plazoleta.infrastructure.configuration.jwt.JwtUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PlateHandlerTest {

    @Mock
    private IPlateServicePort plateServicePort;

    @Mock
    private IPlateRequestMapper plateRequestMapper;

    @InjectMocks
    private PlateHandler plateHandler;

    private static final Long USER_ID = 10L;

    private PlateModel plateModel;

    @BeforeEach
    void setUp() {
        plateModel = PlateModel.builder()
                .id(1L)
                .nombre("Bandeja Paisa")
                .precio(25000)
                .descripcion("Plato típico")
                .urlImagen("http://img.com/img.jpg")
                .categoria("Típico")
                .activa(true)
                .idRestaurante(2L)
                .creadoEn(Instant.now())
                .build();

        JwtUserDetails userDetails = new JwtUserDetails(USER_ID, "owner@mail.com", "PROPIETARIO");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }


    @Test
    @DisplayName("Debe retornar PlateResponseDto cuando el plato es creado exitosamente")
    void savePlate_shouldReturnResponseDtoWhenCreated() {
        PlateRequestDto requestDto = new PlateRequestDto();
        requestDto.setNombre("Bandeja Paisa");
        requestDto.setPrecio(25000);
        requestDto.setDescripcion("Plato típico");
        requestDto.setUrlImagen("http://img.com/img.jpg");
        requestDto.setCategoria("Típico");
        requestDto.setIdRestaurante(2L);

        when(plateRequestMapper.toPlate(requestDto)).thenReturn(plateModel);
        when(plateServicePort.savePlate(plateModel, USER_ID)).thenReturn(plateModel);

        PlateResponseDto result = plateHandler.savePlate(requestDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Bandeja Paisa", result.getNombre());
        assertEquals(25000, result.getPrecio());
        assertEquals("Plato típico", result.getDescripcion());
        assertNotNull(result.getCreadoEn());
    }

    @Test
    @DisplayName("Debe retornar creadoEn nulo cuando el modelo no tiene fecha")
    void savePlate_shouldReturnNullCreadoEnWhenDateIsNull() {
        plateModel.setCreadoEn(null);
        PlateRequestDto requestDto = new PlateRequestDto();
        when(plateRequestMapper.toPlate(requestDto)).thenReturn(plateModel);
        when(plateServicePort.savePlate(plateModel, USER_ID)).thenReturn(plateModel);

        PlateResponseDto result = plateHandler.savePlate(requestDto);

        assertNull(result.getCreadoEn());
    }

    @Test
    @DisplayName("Debe llamar al servicio con el ID del usuario autenticado")
    void savePlate_shouldCallServiceWithAuthenticatedUserId() {
        PlateRequestDto requestDto = new PlateRequestDto();
        when(plateRequestMapper.toPlate(requestDto)).thenReturn(plateModel);
        when(plateServicePort.savePlate(any(), eq(USER_ID))).thenReturn(plateModel);

        plateHandler.savePlate(requestDto);

        verify(plateServicePort).savePlate(plateModel, USER_ID);
    }

    @Test
    @DisplayName("Debe retornar PlateResponseDto actualizado")
    void updatePlate_shouldReturnUpdatedResponseDto() {
        PlateUpdateRequestDto updateDto = new PlateUpdateRequestDto();
        updateDto.setPrecio(30000);
        updateDto.setDescripcion("Nueva descripción");

        plateModel.setPrecio(30000);
        plateModel.setDescripcion("Nueva descripción");

        when(plateServicePort.updatePlate(1L, 30000, "Nueva descripción", USER_ID)).thenReturn(plateModel);

        PlateResponseDto result = plateHandler.updatePlate(1L, updateDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(30000, result.getPrecio());
        assertEquals("Nueva descripción", result.getDescripcion());
    }

    @Test
    @DisplayName("Debe llamar al servicio con los parámetros correctos al actualizar")
    void updatePlate_shouldCallServiceWithCorrectParams() {
        PlateUpdateRequestDto updateDto = new PlateUpdateRequestDto();
        updateDto.setPrecio(30000);
        updateDto.setDescripcion("Desc actualizada");

        when(plateServicePort.updatePlate(1L, 30000, "Desc actualizada", USER_ID)).thenReturn(plateModel);

        plateHandler.updatePlate(1L, updateDto);

        verify(plateServicePort).updatePlate(1L, 30000, "Desc actualizada", USER_ID);
    }

    @Test
    @DisplayName("Debe retornar PlateResponseDto con activa=false al desactivar")
    void togglePlateStatus_shouldReturnDeactivatedPlate() {
        plateModel.setActiva(false);
        when(plateServicePort.togglePlateStatus(1L, false, USER_ID)).thenReturn(plateModel);

        PlateResponseDto result = plateHandler.togglePlateStatus(1L, false);

        assertNotNull(result);
        assertFalse(result.getActiva());
    }

    @Test
    @DisplayName("Debe retornar PlateResponseDto con activa=true al activar")
    void togglePlateStatus_shouldReturnActivatedPlate() {
        plateModel.setActiva(true);
        when(plateServicePort.togglePlateStatus(1L, true, USER_ID)).thenReturn(plateModel);

        PlateResponseDto result = plateHandler.togglePlateStatus(1L, true);

        assertNotNull(result);
        assertTrue(result.getActiva());
    }


    @Test
    @DisplayName("Debe retornar lista paginada de platos del restaurante")
    void listPlatesByRestaurant_shouldReturnPaginatedPlates() {
        Page<PlateModel> page = new PageImpl<>(List.of(plateModel), PageRequest.of(0, 10), 1);
        when(plateServicePort.listPlatesByRestaurant(eq(2L), isNull(), any())).thenReturn(page);

        PaginatedResponseDto<PlateListResponseDto> result =
                plateHandler.listPlatesByRestaurant(2L, null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Bandeja Paisa", result.getContent().get(0).getNombre());
        assertEquals(25000, result.getContent().get(0).getPrecio());
        assertEquals("Típico", result.getContent().get(0).getCategoria());
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay platos")
    void listPlatesByRestaurant_shouldReturnEmptyWhenNoPlates() {
        Page<PlateModel> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(plateServicePort.listPlatesByRestaurant(any(), any(), any())).thenReturn(emptyPage);

        PaginatedResponseDto<PlateListResponseDto> result =
                plateHandler.listPlatesByRestaurant(2L, "Típico", 0, 10);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    @DisplayName("Debe pasar la categoría al servicio cuando se especifica")
    void listPlatesByRestaurant_shouldPassCategoryToService() {
        Page<PlateModel> page = new PageImpl<>(List.of(plateModel), PageRequest.of(0, 10), 1);
        when(plateServicePort.listPlatesByRestaurant(eq(2L), eq("Típico"), any())).thenReturn(page);

        plateHandler.listPlatesByRestaurant(2L, "Típico", 0, 10);

        verify(plateServicePort).listPlatesByRestaurant(eq(2L), eq("Típico"), eq(PageRequest.of(0, 10)));
    }
}

