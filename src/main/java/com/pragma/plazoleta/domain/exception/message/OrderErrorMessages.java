package com.pragma.plazoleta.domain.exception.message;

public final class OrderErrorMessages {
    private OrderErrorMessages() {}

    public static final String NOT_FOUND = "El pedido no existe";
    public static final String CLIENT_HAS_ACTIVE_ORDER = "El cliente ya tiene un pedido en proceso";
    public static final String NOT_PENDING = "Solo se pueden asignar pedidos en estado PENDIENTE";
    public static final String NOT_IN_PREPARATION = "Solo se pueden marcar como listos pedidos en estado EN_PREPARACION";
    public static final String NOT_BELONGS_TO_RESTAURANT = "El pedido no pertenece al restaurante del empleado";
}
