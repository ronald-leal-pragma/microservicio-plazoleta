package com.pragma.plazoleta.domain.exception.message;

public final class PlateErrorMessages {
    private PlateErrorMessages() {}

    public static final String INVALID_PRICE = "El precio debe ser un número entero positivo mayor a 0";
    public static final String NOT_FOUND = "El plato no existe";
    public static final String ALREADY_EXISTS = "El plato ya existe";
    public static final String NOT_BELONGS_TO_RESTAURANT = "El plato no pertenece al restaurante indicado";
    public static final String NOT_ACTIVE = "El plato no está disponible";
}
