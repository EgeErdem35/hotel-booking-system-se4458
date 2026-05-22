package com.se4458.hotelbooking.hotelsearchservice.hotel;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record HotelSearchResultResponse(
        UUID id,
        String name,
        String description,
        String destination,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        BigDecimal starRating,
        List<String> amenities,
        String imageUrl,
        BigDecimal lowestPricePerNight,
        long availableRoomTypes
) {
}
