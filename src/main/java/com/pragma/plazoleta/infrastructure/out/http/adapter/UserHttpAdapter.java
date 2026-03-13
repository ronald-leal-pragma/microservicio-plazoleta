package com.pragma.plazoleta.infrastructure.out.http.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.plazoleta.domain.model.RolModel;
import com.pragma.plazoleta.domain.model.UserModel;
import com.pragma.plazoleta.domain.spi.IUserPersistencePort;
import com.pragma.plazoleta.infrastructure.exception.UserServiceException;
import com.pragma.plazoleta.infrastructure.out.http.dto.UserCreateRequestDto;
import com.pragma.plazoleta.infrastructure.out.http.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class UserHttpAdapter implements IUserPersistencePort {

    private final RestTemplate restTemplate;
    private final String usuariosServiceUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public Optional<UserModel> findUserById(Long id) {
        String url = String.format("%s/user/%d", usuariosServiceUrl, id);
        log.debug("[HTTP ADAPTER] Consultando usuario id={} en {}", id, url);

        try {
            return Optional.of(restTemplate.getForEntity(url, UserResponseDto.class))
                    .filter(response -> response.getStatusCode().is2xxSuccessful())
                    .map(ResponseEntity::getBody)
                    .map(this::mapToModel);
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("[HTTP ADAPTER] Usuario no encontrado: id={}", id);
            return Optional.empty();
        } catch (Exception e) {
            log.error("[HTTP ADAPTER] Error inesperado en microservicio-usuarios: {}", e.getMessage());
            return Optional.empty();
        }
    }


    @Override
    public UserModel saveUser(UserModel userModel) {
        String url = String.format("%s/user/employee/", usuariosServiceUrl);
        log.debug("[HTTP ADAPTER] Creando empleado en microservicio-usuarios: correo={}", userModel.getCorreo());

        UserCreateRequestDto requestDto = UserCreateRequestDto.builder()
                .nombre(userModel.getNombre())
                .apellido(userModel.getApellido())
                .documentoDeIdentidad(userModel.getDocumentoDeIdentidad())
                .celular(userModel.getCelular())
                .correo(userModel.getCorreo())
                .clave(userModel.getClave())
                .idRol(Optional.ofNullable(userModel.getRol()).map(RolModel::getId).orElse(null))
                .build();

        try {
            ResponseEntity<UserResponseDto> response = restTemplate.postForEntity(url, requestDto, UserResponseDto.class);
            log.info("[HTTP ADAPTER] Empleado creado exitosamente en microservicio-usuarios");
            return Optional.ofNullable(response.getBody())
                    .map(this::mapToModel)
                    .orElse(userModel);
        } catch (HttpClientErrorException e) {
            String message = extractErrorMessage(e.getResponseBodyAsString());
            log.warn("[HTTP ADAPTER] Error del cliente al crear empleado: status={}, message={}",
                    e.getStatusCode().value(), message);
            throw new UserServiceException(message, e.getStatusCode().value());
        } catch (HttpServerErrorException e) {
            log.error("[HTTP ADAPTER] Error del servidor de usuarios: {}", e.getMessage());
            throw new UserServiceException("Error en el servicio de usuarios", e.getStatusCode().value());
        } catch (Exception e) {
            log.error("[HTTP ADAPTER] Error inesperado al crear empleado: {}", e.getMessage());
            throw new UserServiceException("Error de comunicación con el servicio de usuarios", 500);
        }
    }

    private String extractErrorMessage(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            if (root.has("message")) {
                return root.get("message").asText();
            }
        } catch (Exception e) {
            log.debug("[HTTP ADAPTER] No se pudo parsear el mensaje de error: {}", e.getMessage());
        }
        return "Error al procesar la solicitud en el servicio de usuarios";
    }

    private UserModel mapToModel(UserResponseDto dto) {
        return UserModel.builder()
                .id(dto.getId())
                .nombre(dto.getNombre())
                .apellido(dto.getApellido())
                .correo(dto.getCorreo())
                .rol(Optional.ofNullable(dto.getRol())
                        .map(rolNombre -> RolModel.builder().nombre(rolNombre).build())
                        .orElse(null))
                .build();
    }
}