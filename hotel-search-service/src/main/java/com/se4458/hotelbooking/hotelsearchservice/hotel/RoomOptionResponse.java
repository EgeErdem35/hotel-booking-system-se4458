package com.se4458.hotelbooking.hotelsearchservice.hotel;

import java.math.BigDecimal;
import java.util.UUID;

public record RoomOptionResponse(
        UUID id,
        UUID hotelId,
        String roomType,
        int capacity,
        int totalCount,
        BigDecimal pricePerNight,
        Integer minAvailableCount
) {
}
