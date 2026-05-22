package com.se4458.hotelbooking.commentsservice.comment;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record CommentResponse(
        UUID commentId,
        UUID hotelId,
        UUID userId,
        double overallRating,
        Map<String, Integer> serviceRatings,
        String comment,
        Instant createdAt
) {
}
