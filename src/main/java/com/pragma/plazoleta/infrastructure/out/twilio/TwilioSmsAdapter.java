package com.pragma.plazoleta.infrastructure.out.twilio;

import com.pragma.plazoleta.domain.spi.ISmsNotificationPort;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TwilioSmsAdapter implements ISmsNotificationPort {

    @Value("${twilio.phone-number}")
    private String twilioPhoneNumber;

    private static final String ORDER_READY_MESSAGE_TEMPLATE = 
            "¡Tu pedido en %s está LISTO! 🍽️\n\nPIN de seguridad: %s\n\nPreséntalo para recoger tu pedido.";

    @Override
    public void sendOrderReadyNotification(String phoneNumber, String pin, String restaurantName) {
        log.info("[TWILIO] Enviando SMS de pedido listo a: {}", phoneNumber);
        
        try {
            String messageBody = String.format(ORDER_READY_MESSAGE_TEMPLATE, restaurantName, pin);
            
            Message message = Message.creator(
                    new PhoneNumber(phoneNumber),
                    new PhoneNumber(twilioPhoneNumber),
                    messageBody
            ).create();

            log.info("[TWILIO] SMS enviado exitosamente. SID: {}, Estado: {}", 
                    message.getSid(), message.getStatus());
        } catch (Exception e) {
            log.error("[TWILIO] Error al enviar SMS a {}: {}", phoneNumber, e.getMessage(), e);
            // No lanzamos excepción para no afectar el flujo principal del pedido
        }
    }
}
