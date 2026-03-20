package com.pragma.plazoleta.domain.spi;

public interface ISmsNotificationPort {
    void sendOrderReadyNotification(String phoneNumber, String pin, String restaurantName);
}
