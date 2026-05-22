package com.se4458.hotelbooking.hoteladminservice.hotel;

import java.math.BigDecimal;
import java.util.UUID;

public record RoomResponse(
        UUID id,
        UUID hotelId,
        String roomType,
        int capacity,
        int totalCount,
        BigDecimal pricePerNight
) {
}
