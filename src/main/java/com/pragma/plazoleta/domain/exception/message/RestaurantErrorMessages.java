package com.pragma.plazoleta.domain.exception.message;

public final class RestaurantErrorMessages {
    private RestaurantErrorMessages() {}

    public static final String INVALID_NAME = "El nombre del restaurante no puede contener solo números";
    public static final String INVALID_NIT = "El NIT debe ser únicamente numérico";
    public static final String INVALID_PHONE = "El teléfono debe ser numérico, puede contener el símbolo + y tener máximo 13 caracteres";
    public static final String NOT_FOUND = "El restaurante no existe";
    public static final String NIT_ALREADY_EXISTS = "El NIT del restaurante ya existe";
    public static final String NAME_ALREADY_EXISTS = "El nombre del restaurante ya existe";
    public static final String NOT_BELONGS_TO_OWNER = "El restaurante no pertenece al propietario";
    public static final String USER_NOT_RESTAURANT_OWNER = "El usuario no es el propietario del restaurante";
}
