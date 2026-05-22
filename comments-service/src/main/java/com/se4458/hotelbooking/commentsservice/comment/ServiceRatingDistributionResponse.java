package com.se4458.hotelbooking.commentsservice.comment;

import java.util.Map;
import java.util.UUID;

public record ServiceRatingDistributionResponse(
        UUID hotelId,
        Map<String, ServiceRatingSummary> services
) {
}
