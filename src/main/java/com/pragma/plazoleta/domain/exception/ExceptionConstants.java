package com.pragma.plazoleta.domain.exception;

public class ExceptionConstants {
    private ExceptionConstants() {}

    public static final String UNDERAGE_USER_MESSAGE = "El propietario debe ser mayor de edad";
    public static final String ROL_PROPIETARIO = "PROPIETARIO";
    public static final Long ROL_PROPIETARIO_ID = 2L;
    
    // Restaurant validation messages
    public static final String INVALID_RESTAURANT_NAME_MESSAGE = "El nombre del restaurante no puede contener solo números";
    public static final String INVALID_NIT_MESSAGE = "El NIT debe ser únicamente numérico";
    public static final String INVALID_PHONE_MESSAGE = "El teléfono debe ser numérico, puede contener el símbolo + y tener máximo 13 caracteres";
    public static final String USER_NOT_FOUND_MESSAGE = "El usuario propietario no existe";
    public static final String USER_NOT_OWNER_MESSAGE = "El usuario no tiene el rol de propietario";
    public static final String RESTAURANT_NIT_ALREADY_EXISTS_MESSAGE = "El NIT del restaurante ya existe";
    public static final String RESTAURANT_NAME_ALREADY_EXISTS_MESSAGE = "El nombre del restaurante ya existe";
    
    // Plate validation messages
    public static final String INVALID_PRICE_MESSAGE = "El precio debe ser un número entero positivo mayor a 0";
    public static final String RESTAURANT_NOT_FOUND_MESSAGE = "El restaurante no existe";
    public static final String USER_NOT_RESTAURANT_OWNER_MESSAGE = "El usuario no es el propietario del restaurante";
    public static final String PLATE_ALREADY_EXISTS_MESSAGE = "El plato ya existe";
    public static final String PLATE_NOT_FOUND_MESSAGE = "El plato no existe";
}
