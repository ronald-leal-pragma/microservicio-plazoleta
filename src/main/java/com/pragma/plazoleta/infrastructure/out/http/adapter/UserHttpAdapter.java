package com.pragma.plazoleta.infrastructure.out.http.adapter;

import com.pragma.plazoleta.domain.model.RolModel;
import com.pragma.plazoleta.domain.model.UserModel;
import com.pragma.plazoleta.domain.spi.IUserPersistencePort;
import com.pragma.plazoleta.infrastructure.out.http.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class UserHttpAdapter implements IUserPersistencePort {

    private final RestTemplate restTemplate;
    private final String usuariosServiceUrl;

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