package com.pragma.plazoleta.infrastructure.input.rest;

import com.pragma.plazoleta.application.dto.request.RestaurantRequestDto;
import com.pragma.plazoleta.application.handler.IRestaurantHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
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
@RequestMapping("/restaurant")
@RequiredArgsConstructor
@Tag(name = "Restaurant", description = "Operaciones relacionadas con restaurantes")
public class RestaurantRestController {

    private final IRestaurantHandler restaurantHandler;

    @Operation(summary = "Crear restaurante",
               description = "Crea un nuevo restaurante en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Restaurante creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                         content = @Content),
            @ApiResponse(responseCode = "409", description = "El restaurante ya existe",
                         content = @Content)
    })
    @PostMapping("/")
    public ResponseEntity<Void> saveRestaurant(@Valid @RequestBody RestaurantRequestDto restaurantRequestDto) {
        log.info("[REST] POST /restaurant/ - Solicitud para crear restaurante: {}", 
                restaurantRequestDto.getNombre());
        restaurantHandler.saveRestaurant(restaurantRequestDto);
        log.info("[REST] Restaurante creado exitosamente: {}", restaurantRequestDto.getNombre());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
