package com.se4458.hotelbooking.notificationservice.notification;

import java.math.BigDecimal;
import java.util.UUID;

public record LowCapacityHotel(
        UUID hotelId,
        UUID adminUserId,
        String hotelName,
        String roomType,
        long totalCapacity,
        long availableCapacity,
        BigDecimal availableRatio
) {
}
