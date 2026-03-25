package com.pragma.plazoleta.infrastructure.out.rest;

import com.pragma.plazoleta.domain.spi.ISmsNotificationPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SmsRestAdapter implements ISmsNotificationPort {

    private static final Logger log = LoggerFactory.getLogger(SmsRestAdapter.class);

    private final RestTemplate restTemplate;

    @Value("${mensajeria.service.url}")
    private String mensajeriaBaseUrl;

    @Value("${mensajeria.service.auth-type}")
    private String authType;

    @Value("${mensajeria.service.api-key}")
    private String apiKey;

    @Override
    public void sendOrderReadyNotification(String phoneNumber, String pin, String restaurantName) {
        String url = buildUrl();
        Map<String, String> payload = createPayload(phoneNumber, pin, restaurantName);
        HttpHeaders headers = buildHeaders();

        HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);

        try {
            restTemplate.postForEntity(url, request, Void.class);
            log.info("[SMS REST ADAPTER] SMS request sent successfully to {} for phone {}", url, phoneNumber);
        } catch (Exception e) {
            log.error("[SMS REST ADAPTER] Error sending SMS via {}: {}", url, e.getMessage());
        }
    }


    private String buildUrl() {
        return mensajeriaBaseUrl.endsWith("/") ? mensajeriaBaseUrl + "sms" : mensajeriaBaseUrl + "/sms";
    }

    private Map<String, String> createPayload(String phoneNumber, String pin, String restaurantName) {
        String bodyText = String.format("Tu pedido en %s ya está listo. PIN: %s", restaurantName, pin);
        return Map.of(
                "to", phoneNumber,
                "body", bodyText
        );
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        forwardIncomingHeaders(headers);
        applyFallbackAuthIfNeeded(headers);

        return headers;
    }

    private void forwardIncomingHeaders(HttpHeaders headers) {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                return;
            }

            HttpServletRequest req = attrs.getRequest();

            String incomingAuth = req.getHeader(HttpHeaders.AUTHORIZATION);
            if (incomingAuth != null && !incomingAuth.isBlank()) {
                headers.set(HttpHeaders.AUTHORIZATION, incomingAuth);
            }

            Enumeration<String> headerNames = req.getHeaderNames();
            if (headerNames != null) {
                Collections.list(headerNames).stream()
                        .filter(h -> h.toLowerCase().startsWith("x-"))
                        .forEach(h -> headers.set(h, req.getHeader(h)));
            }
        } catch (Exception ex) {
            log.debug("[SMS REST ADAPTER] No HTTP request context to forward headers: {}", ex.getMessage());
        }
    }

    private void applyFallbackAuthIfNeeded(HttpHeaders headers) {
        if (headers.containsKey(HttpHeaders.AUTHORIZATION) || apiKey.isBlank()) {
            return;
        }

        if ("bearer".equalsIgnoreCase(authType)) {
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);
        } else if ("apikey".equalsIgnoreCase(authType)) {
            headers.set("x-api-key", apiKey);
        }
    }
}