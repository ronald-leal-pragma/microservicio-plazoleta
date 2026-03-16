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

    public static final String ROL_EMPLEADO = "EMPLEADO";
    public static final Long ROL_EMPLEADO_ID = 3L;

    // Employee validation messages
    public static final String OWNER_WITHOUT_RESTAURANT_MESSAGE = "El propietario no tiene un restaurante asociado";
    public static final String RESTAURANT_NOT_BELONGS_TO_OWNER_MESSAGE = "El restaurante no pertenece al propietario";
    public static final String EMPLOYEE_NOT_BELONGS_TO_RESTAURANT_MESSAGE = "El empleado no pertenece al restaurante";

    // Order validation messages
    public static final String CLIENT_HAS_ACTIVE_ORDER_MESSAGE = "El cliente ya tiene un pedido en proceso";
    public static final String PLATE_NOT_BELONGS_TO_RESTAURANT_MESSAGE = "El plato no pertenece al restaurante indicado";
    public static final String PLATE_NOT_ACTIVE_MESSAGE = "El plato no está disponible";
    public static final String ORDER_NOT_FOUND_MESSAGE = "El pedido no existe";
}
