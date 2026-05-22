package com.se4458.hotelbooking.bookingservice.booking;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record BookingResponse(
        UUID id,
        UUID hotelId,
        UUID roomId,
        UUID userId,
        String hotelName,
        String roomType,
        LocalDate checkIn,
        LocalDate checkOut,
        int guestCount,
        BigDecimal totalPrice,
        String status,
        Instant createdAt
) {
}
