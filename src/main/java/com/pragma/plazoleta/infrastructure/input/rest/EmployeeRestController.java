package com.pragma.plazoleta.infrastructure.input.rest;

import com.pragma.plazoleta.application.dto.request.EmployeeRequestDto;
import com.pragma.plazoleta.application.dto.response.EmployeeResponseDto;
import com.pragma.plazoleta.application.handler.IEmployeeHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/employee")
@RequiredArgsConstructor
@Tag(name = "Employee", description = "Operaciones relacionadas con empleados")
public class EmployeeRestController {

    private final IEmployeeHandler employeeHandler;

    @Operation(summary = "Crear empleado",
               description = "Crea una cuenta de empleado. Solo el propietario de un restaurante puede crear empleados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Empleado creado exitosamente",
                         content = @Content(schema = @Schema(implementation = EmployeeResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o validaciones de negocio fallidas",
                         content = @Content),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene permisos para crear empleados",
                         content = @Content),
            @ApiResponse(responseCode = "409", description = "Ya existe un usuario con ese correo o documento",
                         content = @Content)
    })
    @PostMapping("/")
    public ResponseEntity<EmployeeResponseDto> createEmployee(
            @Valid @RequestBody EmployeeRequestDto employeeRequestDto) {
        log.info("[REST] POST /employee/ - Solicitud para crear empleado: correo={}",
                employeeRequestDto.getCorreo());
        EmployeeResponseDto created = employeeHandler.createEmployee(employeeRequestDto);
        log.info("[REST] Empleado creado exitosamente: correo={}", employeeRequestDto.getCorreo());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
