package com.se4458.hotelbooking.notificationservice.notification;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ReservationCreatedEvent(
        UUID bookingId,
        UUID hotelId,
        UUID roomId,
        UUID userId,
        LocalDate checkIn,
        LocalDate checkOut,
        int guestCount,
        BigDecimal totalPrice,
        Instant createdAt
) {
}
