package com.se4458.hotelbooking.hoteladminservice.auth;

import java.util.UUID;

public record AdminAuthentication(UUID userId, String bearerToken) {
}
