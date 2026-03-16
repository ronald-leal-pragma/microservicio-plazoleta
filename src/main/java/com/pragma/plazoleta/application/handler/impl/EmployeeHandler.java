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

        Long ownerId = getAuthenticatedUserId();
        log.debug("[HANDLER] ID del propietario autenticado extraído: {}", ownerId);

        UserModel employeeModel = employeeRequestMapper.toUser(employeeRequestDto);

        UserModel createdEmployee = employeeServicePort.createEmployee(
                employeeModel, ownerId, employeeRequestDto.getIdRestaurante());

        log.info("[HANDLER] Empleado creado exitosamente, mapeando respuesta");

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