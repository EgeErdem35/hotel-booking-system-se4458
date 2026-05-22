package com.se4458.hotelbooking.hotelsearchservice.hotel;

import java.util.Optional;
import java.util.UUID;

public interface HotelDetailCache {

    Optional<HotelDetailResponse> get(UUID hotelId);

    void put(UUID hotelId, HotelDetailResponse hotelDetail);
}
