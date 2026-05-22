package com.se4458.hotelbooking.hoteladminservice.hotel;

import java.time.LocalDate;
import java.util.UUID;

public record AvailabilityResponse(
        UUID roomId,
        LocalDate startDate,
        LocalDate endDate,
        int availableCount,
        int affectedDays
) {
}
