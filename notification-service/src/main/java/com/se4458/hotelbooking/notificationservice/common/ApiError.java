package com.se4458.hotelbooking.notificationservice.common;

import java.time.Instant;

public record ApiError(String message, int status, Instant timestamp) {

    public static ApiError of(String message, int status) {
        return new ApiError(message, status, Instant.now());
    }
}
