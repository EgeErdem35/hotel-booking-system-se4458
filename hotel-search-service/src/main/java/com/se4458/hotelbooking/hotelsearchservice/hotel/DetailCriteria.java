package com.se4458.hotelbooking.hotelsearchservice.hotel;

import java.time.LocalDate;

public record DetailCriteria(LocalDate checkIn, LocalDate checkOut, Integer guests) {

    public boolean hasAvailabilityFilter() {
        return checkIn != null || checkOut != null || guests != null;
    }
}
