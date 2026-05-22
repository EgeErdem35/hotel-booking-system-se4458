package com.se4458.hotelbooking.notificationservice.notification;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID userId,
        UUID hotelId,
        UUID bookingId,
        String message,
        String type,
        String status,
        Instant createdAt
) {
}
