package com.pragma.plazoleta.infrastructure.out.http.adapter;

import com.pragma.plazoleta.domain.model.RolModel;
import com.pragma.plazoleta.domain.model.UserModel;
import com.pragma.plazoleta.infrastructure.exception.UserServiceException;
import com.pragma.plazoleta.infrastructure.out.http.dto.UserResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserHttpAdapterTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private UserHttpAdapter userHttpAdapter;

    private static final String BASE_URL = "http://localhost:8081";

    @BeforeEach
    void setUp() throws Exception {
        java.lang.reflect.Field field = UserHttpAdapter.class.getDeclaredField("usuariosServiceUrl");
        field.setAccessible(true);
        field.set(userHttpAdapter, BASE_URL);
    }

    @Test
    @DisplayName("Debe retornar Optional con UserModel cuando el usuario existe")
    void findUserById_shouldReturnOptionalWithUserWhenFound() {
        UserResponseDto responseDto = UserResponseDto.builder()
                .id(1L)
                .nombre("Carlos")
                .apellido("Gómez")
                .correo("carlos@mail.com")
                .rol("PROPIETARIO")
                .build();

        when(restTemplate.getForEntity(
                eq(BASE_URL + "/user/1"),
                eq(UserResponseDto.class)
        )).thenReturn(ResponseEntity.ok(responseDto));

        Optional<UserModel> result = userHttpAdapter.findUserById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("Carlos", result.get().getNombre());
        assertEquals("PROPIETARIO", result.get().getRol().getNombre());
    }

    @Test
    @DisplayName("Debe mapear el rol correctamente cuando el usuario tiene rol")
    void findUserById_shouldMapRoleCorrectly() {
        UserResponseDto responseDto = UserResponseDto.builder()
                .id(1L)
                .nombre("Ana")
                .correo("ana@mail.com")
                .rol("EMPLEADO")
                .build();

        when(restTemplate.getForEntity(anyString(), eq(UserResponseDto.class)))
                .thenReturn(ResponseEntity.ok(responseDto));

        Optional<UserModel> result = userHttpAdapter.findUserById(1L);

        assertTrue(result.isPresent());
        assertNotNull(result.get().getRol());
        assertEquals("EMPLEADO", result.get().getRol().getNombre());
    }

    @Test
    @DisplayName("Debe retornar Optional vacío cuando el usuario no es encontrado (404)")
    void findUserById_shouldReturnEmptyWhenNotFound() {
        when(restTemplate.getForEntity(anyString(), eq(UserResponseDto.class)))
                .thenThrow(HttpClientErrorException.NotFound.class);

        Optional<UserModel> result = userHttpAdapter.findUserById(99L);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Debe retornar Optional vacío cuando ocurre un error inesperado al buscar")
    void findUserById_shouldReturnEmptyOnUnexpectedException() {
        when(restTemplate.getForEntity(anyString(), eq(UserResponseDto.class)))
                .thenThrow(new RuntimeException("Error de red"));

        Optional<UserModel> result = userHttpAdapter.findUserById(1L);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Debe crear el usuario y retornar el modelo mapeado")
    void saveUser_shouldCreateAndReturnMappedUserModel() {
        UserModel userToSave = UserModel.builder()
                .nombre("Ana")
                .apellido("López")
                .correo("ana@mail.com")
                .documentoDeIdentidad("12345")
                .celular("3001234567")
                .clave("clave123")
                .rol(RolModel.builder().id(3L).nombre("EMPLEADO").build())
                .build();

        UserResponseDto responseDto = UserResponseDto.builder()
                .id(50L)
                .nombre("Ana")
                .apellido("López")
                .correo("ana@mail.com")
                .rol("EMPLEADO")
                .build();

        when(restTemplate.postForEntity(anyString(), any(), eq(UserResponseDto.class)))
                .thenReturn(ResponseEntity.ok(responseDto));

        UserModel result = userHttpAdapter.saveUser(userToSave);

        assertNotNull(result);
        assertEquals(50L, result.getId());
        assertEquals("Ana", result.getNombre());
        assertEquals("EMPLEADO", result.getRol().getNombre());
    }

    @Test
    @DisplayName("Debe retornar el modelo original cuando el cuerpo de respuesta es nulo")
    void saveUser_shouldReturnOriginalModelWhenResponseBodyIsNull() {
        UserModel userToSave = UserModel.builder()
                .nombre("Ana")
                .correo("ana@mail.com")
                .rol(RolModel.builder().id(3L).build())
                .build();

        when(restTemplate.postForEntity(anyString(), any(), eq(UserResponseDto.class)))
                .thenReturn(ResponseEntity.ok(null));

        UserModel result = userHttpAdapter.saveUser(userToSave);

        assertNotNull(result);
        assertEquals("Ana", result.getNombre());
    }

    @Test
    @DisplayName("Debe lanzar UserServiceException cuando hay error del cliente (4xx)")
    void saveUser_shouldThrowUserServiceExceptionOnClientError() {
        UserModel userToSave = UserModel.builder()
                .nombre("Ana")
                .correo("ana@mail.com")
                .rol(RolModel.builder().id(3L).build())
                .build();

        HttpClientErrorException clientException = new HttpClientErrorException(
                HttpStatus.CONFLICT, "Conflict",
                "{\"message\":\"El correo ya existe\"}".getBytes(), null);

        when(restTemplate.postForEntity(anyString(), any(), eq(UserResponseDto.class)))
                .thenThrow(clientException);

        UserServiceException ex = assertThrows(UserServiceException.class,
                () -> userHttpAdapter.saveUser(userToSave));

        assertEquals("El correo ya existe", ex.getMessage());
        assertEquals(409, ex.getStatusCode());
    }

    @Test
    @DisplayName("Debe lanzar UserServiceException cuando hay error del servidor (5xx)")
    void saveUser_shouldThrowUserServiceExceptionOnServerError() {
        UserModel userToSave = UserModel.builder()
                .nombre("Ana")
                .correo("ana@mail.com")
                .rol(RolModel.builder().id(3L).build())
                .build();

        when(restTemplate.postForEntity(anyString(), any(), eq(UserResponseDto.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        UserServiceException ex = assertThrows(UserServiceException.class,
                () -> userHttpAdapter.saveUser(userToSave));

        assertEquals(500, ex.getStatusCode());
    }

    @Test
    @DisplayName("Debe lanzar UserServiceException cuando ocurre un error inesperado al crear")
    void saveUser_shouldThrowUserServiceExceptionOnUnexpectedException() {
        UserModel userToSave = UserModel.builder()
                .nombre("Ana")
                .correo("ana@mail.com")
                .rol(RolModel.builder().id(3L).build())
                .build();

        when(restTemplate.postForEntity(anyString(), any(), eq(UserResponseDto.class)))
                .thenThrow(new RuntimeException("Error de red inesperado"));

        UserServiceException ex = assertThrows(UserServiceException.class,
                () -> userHttpAdapter.saveUser(userToSave));

        assertEquals(500, ex.getStatusCode());
    }
}

