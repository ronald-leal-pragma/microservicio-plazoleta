package com.pragma.plazoleta.domain.exception.message;

public final class UserErrorMessages {
    private UserErrorMessages() {}

    public static final String UNDERAGE_USER = "El propietario debe ser mayor de edad";
    public static final String USER_NOT_FOUND = "El usuario propietario no existe";
    public static final String USER_NOT_OWNER = "El usuario no tiene el rol de propietario";
}
