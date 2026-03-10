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
        String url = usuariosServiceUrl + "/user/" + id;
        log.debug("[HTTP ADAPTER] Consultando usuario id={} en {}", id, url);
        try {
            ResponseEntity<UserResponseDto> response = restTemplate.getForEntity(url, UserResponseDto.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                UserResponseDto dto = response.getBody();
                UserModel model = new UserModel();
                model.setId(dto.getId());
                model.setNombre(dto.getNombre());
                model.setApellido(dto.getApellido());
                model.setCorreo(dto.getCorreo());
                if (dto.getRol() != null) {
                    RolModel rolModel = new RolModel();
                    rolModel.setNombre(dto.getRol());
                    model.setRol(rolModel);
                }
                log.debug("[HTTP ADAPTER] Usuario encontrado: id={}, rol={}", id, dto.getRol());
                return Optional.of(model);
            }
            return Optional.empty();
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("[HTTP ADAPTER] Usuario no encontrado: id={}", id);
            return Optional.empty();
        } catch (Exception e) {
            log.error("[HTTP ADAPTER] Error consultando microservicio-usuarios: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
