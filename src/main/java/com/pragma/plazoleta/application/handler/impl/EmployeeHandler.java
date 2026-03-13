package com.pragma.plazoleta.application.handler.impl;

import com.pragma.plazoleta.application.dto.request.EmployeeRequestDto;
import com.pragma.plazoleta.application.dto.response.EmployeeResponseDto;
import com.pragma.plazoleta.application.handler.IEmployeeHandler;
import com.pragma.plazoleta.application.mapper.IEmployeeRequestMapper;
import com.pragma.plazoleta.domain.api.IEmployeeServicePort;
import com.pragma.plazoleta.domain.model.UserModel;
import com.pragma.plazoleta.infrastructure.configuration.jwt.JwtUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeHandler implements IEmployeeHandler {

    private final IEmployeeServicePort employeeServicePort;
    private final IEmployeeRequestMapper employeeRequestMapper;

    @Override
    public EmployeeResponseDto createEmployee(EmployeeRequestDto employeeRequestDto) {
        log.info("[HANDLER] Iniciando petición para crear empleado");

        // 1. Extraer el ID del propietario desde el contexto de seguridad (JWT)
        Long ownerId = getAuthenticatedUserId();
        log.debug("[HANDLER] ID del propietario autenticado extraído: {}", ownerId);

        // 2. Mapear el Request DTO al Modelo de Dominio
        UserModel employeeModel = employeeRequestMapper.toUser(employeeRequestDto);

        // 3. Ejecutar el Caso de Uso pasando el modelo y el ID del creador
        UserModel createdEmployee = employeeServicePort.createEmployee(employeeModel, ownerId);

        log.info("[HANDLER] Empleado creado exitosamente, mapeando respuesta");

        // 4. Retornar la respuesta usando el patrón Builder (como hemos venido haciendo)
        return EmployeeResponseDto.builder()
                .id(createdEmployee.getId())
                .nombre(createdEmployee.getNombre())
                .apellido(createdEmployee.getApellido())
                .correo(createdEmployee.getCorreo())
                .rol(createdEmployee.getRol() != null ? createdEmployee.getRol().getNombre() : null)
                .build();
    }


    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof JwtUserDetails userDetails) {
            return userDetails.getId();
        }

        log.error("[HANDLER] No se pudo extraer la autenticación del contexto de seguridad");
        throw new SecurityException("Usuario no autenticado o token inválido");
    }
}