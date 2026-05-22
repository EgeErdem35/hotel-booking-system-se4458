package com.se4458.hotelbooking.aiagentservice.ai;

import java.time.LocalDate;
import java.util.UUID;

public record ParsedIntent(
        String action,
        String destination,
        LocalDate checkIn,
        LocalDate checkOut,
        Integer guests,
        UUID roomId,
        UUID userId
) {
}
