package com.se4458.hotelbooking.notificationservice.notification;

import java.time.LocalDate;

public record NightlyJobResponse(
        LocalDate startDate,
        LocalDate endDate,
        int lowCapacityMatches,
        int notificationsCreated
) {
}
