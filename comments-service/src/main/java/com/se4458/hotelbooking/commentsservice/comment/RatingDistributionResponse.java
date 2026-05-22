package com.se4458.hotelbooking.commentsservice.comment;

import java.util.Map;
import java.util.UUID;

public record RatingDistributionResponse(UUID hotelId, Map<Integer, Long> distribution) {
}
