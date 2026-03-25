package com.pragma.plazoleta.infrastructure.out.rest;

import com.pragma.plazoleta.domain.spi.ITraceabilityNotificationPort;
import com.pragma.plazoleta.infrastructure.out.rest.dto.TraceabilityRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RequiredArgsConstructor
@Component
public class TraceabilityRestAdapter implements ITraceabilityNotificationPort {

    private final RestTemplate restTemplate;

    @Value("${traceability.service.url:http://localhost:8083}")
    private String traceabilityServiceUrl;

    @Override
    public void sendTraceabilityLog(Long pedidoId, Long clienteId, String estadoAnterior, String estadoNuevo) {
        sendTraceabilityLog(pedidoId, clienteId, estadoAnterior, estadoNuevo, null);
    }

    @Override
    public void sendTraceabilityLog(Long pedidoId, Long clienteId, String estadoAnterior, String estadoNuevo, java.util.Map<String, String> metadata) {
        try {
            TraceabilityRequest request = TraceabilityRequest.builder()
                    .pedidoId(pedidoId)
                    .clienteId(clienteId)
                    .estadoAnterior(estadoAnterior)
                    .estadoNuevo(estadoNuevo)
                    .metadata(metadata)
                    .build();

            String url = traceabilityServiceUrl + "/traceability/";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            try {
                ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attrs != null) {
                    HttpServletRequest currentRequest = attrs.getRequest();
                    String auth = currentRequest.getHeader(HttpHeaders.AUTHORIZATION);
                    if (auth != null) {
                        headers.set(HttpHeaders.AUTHORIZATION, auth);
                    }
                }
            } catch (Exception ex) {
                log.debug("[TRACEABILITY ADAPTER] No se pudo leer Authorization de contexto: {}", ex.getMessage());
            }
            HttpEntity<TraceabilityRequest> entity = new HttpEntity<>(request, headers);

            restTemplate.postForEntity(url, entity, Void.class);
            log.info("[TRACEABILITY ADAPTER] Enviado log a {} para pedido {} metadata={}", url, pedidoId, metadata);
        } catch (Exception e) {
            log.error("[TRACEABILITY ADAPTER] Error enviando log de trazabilidad: {}", e.getMessage());
        }
    }
}
