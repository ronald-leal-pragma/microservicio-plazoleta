package com.pragma.plazoleta.infrastructure.input.rest;

import com.pragma.plazoleta.application.dto.request.PlateRequestDto;
import com.pragma.plazoleta.application.dto.request.PlateUpdateRequestDto;
import com.pragma.plazoleta.application.dto.response.PlateResponseDto;
import com.pragma.plazoleta.application.handler.IPlateHandler;
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
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/plate")
@RequiredArgsConstructor
@Tag(name = "Plate", description = "Operaciones relacionadas con platos")
public class PlateRestController {

    private final IPlateHandler plateHandler;

    @Operation(summary = "Crear plato",
               description = "Crea un nuevo plato asociado a un restaurante. Solo el propietario del restaurante puede crear platos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Plato creado exitosamente",
                         content = @Content(schema = @Schema(implementation = PlateResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o validaciones de negocio fallidas",
                         content = @Content),
            @ApiResponse(responseCode = "404", description = "El plato ya existe en este restaurante",
                         content = @Content)
    })
    @PostMapping("/")
    public ResponseEntity<PlateResponseDto> savePlate(@Valid @RequestBody PlateRequestDto plateRequestDto) {
        log.info("[REST] POST /plate/ - Solicitud para crear plato: nombre={}, restaurante={}", 
                plateRequestDto.getNombre(), plateRequestDto.getIdRestaurante());
        PlateResponseDto created = plateHandler.savePlate(plateRequestDto);
        log.info("[REST] Plato creado exitosamente: {}", plateRequestDto.getNombre());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Actualizar plato",
               description = "Actualiza el precio y la descripción de un plato. Solo el propietario del restaurante puede modificar platos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Plato actualizado exitosamente",
                         content = @Content(schema = @Schema(implementation = PlateResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o el usuario no es propietario",
                         content = @Content),
            @ApiResponse(responseCode = "404", description = "Plato o restaurante no encontrado",
                         content = @Content)
    })
    @PutMapping("/{idPlate}")
    public ResponseEntity<PlateResponseDto> updatePlate(
            @PathVariable Long idPlate,
            @Valid @RequestBody PlateUpdateRequestDto plateUpdateRequestDto) {
        log.info("[REST] PUT /plate/{} - Solicitud para actualizar plato", idPlate);
        PlateResponseDto updated = plateHandler.updatePlate(idPlate, plateUpdateRequestDto);
        log.info("[REST] Plato actualizado exitosamente: id={}", idPlate);
        return ResponseEntity.ok(updated);
    }
}

