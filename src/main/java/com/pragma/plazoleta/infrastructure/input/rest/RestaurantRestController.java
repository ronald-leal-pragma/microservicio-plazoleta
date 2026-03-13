package com.pragma.plazoleta.infrastructure.input.rest;

import com.pragma.plazoleta.application.dto.request.RestaurantRequestDto;
import com.pragma.plazoleta.application.dto.response.PaginatedResponseDto;
import com.pragma.plazoleta.application.dto.response.RestaurantListResponseDto;
import com.pragma.plazoleta.application.dto.response.RestaurantResponseDto;
import com.pragma.plazoleta.application.handler.IRestaurantHandler;
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
@RequestMapping("/restaurant")
@RequiredArgsConstructor
@Tag(name = "Restaurant", description = "Operaciones relacionadas con restaurantes")
public class RestaurantRestController {

    private final IRestaurantHandler restaurantHandler;

    @Operation(summary = "Crear restaurante",
               description = "Crea un nuevo restaurante en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Restaurante creado exitosamente",
                         content = @Content(schema = @Schema(implementation = RestaurantResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                         content = @Content),
            @ApiResponse(responseCode = "409", description = "El restaurante ya existe",
                         content = @Content)
    })
    @PostMapping("/")
    public ResponseEntity<RestaurantResponseDto> saveRestaurant(
            @Valid @RequestBody RestaurantRequestDto restaurantRequestDto) {
        log.info("[REST] POST /restaurant/ - Solicitud para crear restaurante: {}", 
                restaurantRequestDto.getNombre());
        RestaurantResponseDto created = restaurantHandler.saveRestaurant(restaurantRequestDto);
        log.info("[REST] Restaurante creado exitosamente: {}", restaurantRequestDto.getNombre());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Listar restaurantes",
               description = "Lista todos los restaurantes ordenados alfabéticamente y paginados. Accesible por clientes.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de restaurantes obtenida exitosamente",
                         content = @Content(schema = @Schema(implementation = PaginatedResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "No autorizado", content = @Content)
    })
    @GetMapping("/")
    public ResponseEntity<PaginatedResponseDto<RestaurantListResponseDto>> listRestaurants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("[REST] GET /restaurant/ - Listar restaurantes: page={}, size={}", page, size);
        PaginatedResponseDto<RestaurantListResponseDto> response = restaurantHandler.listRestaurants(page, size);
        log.info("[REST] Restaurantes listados exitosamente: {} elementos", response.getContent().size());
        return ResponseEntity.ok(response);
    }
}

