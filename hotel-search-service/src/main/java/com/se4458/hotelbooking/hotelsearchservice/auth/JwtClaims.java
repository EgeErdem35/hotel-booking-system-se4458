package com.se4458.hotelbooking.hotelsearchservice.auth;

import java.time.Instant;
import java.util.UUID;

public record JwtClaims(UUID subject, Instant expiresAt) {
}
