package com.se4458.hotelbooking.commentsservice.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;

public record CreateCommentRequest(
        @NotNull UUID hotelId,
        @NotNull UUID userId,
        double overallRating,
        @NotNull Map<String, Integer> serviceRatings,
        @NotBlank String comment
) {
}
