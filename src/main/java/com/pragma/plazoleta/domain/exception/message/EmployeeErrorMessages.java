package com.pragma.plazoleta.domain.exception.message;

public final class EmployeeErrorMessages {
    private EmployeeErrorMessages() {}

    public static final String OWNER_WITHOUT_RESTAURANT = "El propietario no tiene un restaurante asociado";
    public static final String NOT_BELONGS_TO_RESTAURANT = "El empleado no pertenece al restaurante";
    public static final String ALREADY_ASSIGNED_TO_RESTAURANT = "El empleado ya está asignado a este restaurante";
}
