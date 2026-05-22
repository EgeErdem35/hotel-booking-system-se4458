package com.se4458.hotelbooking.bookingservice.booking;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

record CreateBookingCommand(
        UUID hotelId,
        UUID roomId,
        UUID userId,
        LocalDate checkIn,
        LocalDate checkOut,
        int guestCount,
        BigDecimal totalPrice
) {
}
