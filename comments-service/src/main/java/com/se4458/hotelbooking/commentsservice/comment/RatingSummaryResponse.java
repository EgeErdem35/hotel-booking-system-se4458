package com.se4458.hotelbooking.commentsservice.comment;

import java.util.UUID;

public record RatingSummaryResponse(UUID hotelId, long totalComments, double averageRating) {
}
