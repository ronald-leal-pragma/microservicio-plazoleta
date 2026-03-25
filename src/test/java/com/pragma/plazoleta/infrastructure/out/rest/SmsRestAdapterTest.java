package com.pragma.plazoleta.infrastructure.out.rest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class SmsRestAdapterTest {

    @AfterEach
    void cleanup() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void forwardsIncomingHeadersToMensajeria() {
        RestTemplate rest = mock(RestTemplate.class);
        when(rest.postForEntity(anyString(), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(ResponseEntity.ok().<Void>build());

        SmsRestAdapter adapter = new SmsRestAdapter(rest);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer token123");
        req.addHeader("x-trace-id", "trace-1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));

        adapter.sendOrderReadyNotification("+5712345", "123456", "MiRestaurante");

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(rest, times(1)).postForEntity(eq("http://localhost:8082/sms"), captor.capture(), eq(Void.class));

        HttpEntity payload = captor.getValue();
        HttpHeaders headers = payload.getHeaders();

        assertEquals("Bearer token123", headers.getFirst("Authorization"));
        assertEquals("trace-1", headers.getFirst("x-trace-id"));
    }

    @Test
    void usesConfiguredApiKeyWhenNoIncomingHeaders_apikey() {
        RestTemplate rest = mock(RestTemplate.class);
        when(rest.postForEntity(anyString(), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(ResponseEntity.ok().<Void>build());

        SmsRestAdapter adapter = new SmsRestAdapter(rest);

        // No RequestContext set -> adapter must apply configured api-key header
        adapter.sendOrderReadyNotification("+5712345", "654321", "MiRestaurante");

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(rest, times(1)).postForEntity(eq("http://localhost:8082/sms"), captor.capture(), eq(Void.class));

        HttpEntity payload = captor.getValue();
        HttpHeaders headers = payload.getHeaders();

        assertEquals("my-key-xyz", headers.getFirst("x-api-key"));
    }

    @Test
    void usesConfiguredApiKeyWhenNoIncomingHeaders_bearer() {
        RestTemplate rest = mock(RestTemplate.class);
        when(rest.postForEntity(anyString(), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(ResponseEntity.ok().<Void>build());

        SmsRestAdapter adapter = new SmsRestAdapter(rest);

        adapter.sendOrderReadyNotification("+5712345", "654321", "MiRestaurante");

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(rest, times(1)).postForEntity(eq("http://localhost:8082/sms"), captor.capture(), eq(Void.class));

        HttpEntity payload = captor.getValue();
        HttpHeaders headers = payload.getHeaders();

        assertEquals("Bearer token-abc", headers.getFirst("Authorization"));
    }
}
