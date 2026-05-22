package com.se4458.hotelbooking.hotelsearchservice.hotel;

import java.math.BigDecimal;
import java.util.UUID;

public record HotelMapResponse(
        UUID id,
        String name,
        String destination,
        BigDecimal latitude,
        BigDecimal longitude
) {
}
