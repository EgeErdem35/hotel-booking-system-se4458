package com.se4458.hotelbooking.hotelsearchservice.hotel;

import java.time.LocalDate;

public record SearchCriteria(
        String destination,
        LocalDate checkIn,
        LocalDate checkOut,
        int guests,
        int page,
        int size
) {
}
