package com.pragma.plazoleta.domain.spi;

public interface ITraceabilityNotificationPort {
    void sendTraceabilityLog(Long pedidoId, Long clienteId, String estadoAnterior, String estadoNuevo);

    void sendTraceabilityLog(Long pedidoId, Long clienteId, String estadoAnterior, String estadoNuevo, java.util.Map<String, String> metadata);
}
