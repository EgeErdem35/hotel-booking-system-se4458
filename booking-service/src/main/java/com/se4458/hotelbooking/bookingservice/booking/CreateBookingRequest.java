package com.se4458.hotelbooking.bookingservice.booking;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record CreateBookingRequest(
        @NotNull UUID roomId,
        @NotNull UUID userId,
        @NotNull @FutureOrPresent LocalDate checkIn,
        @NotNull @FutureOrPresent LocalDate checkOut,
        @Min(1) int guestCount
) {
}
