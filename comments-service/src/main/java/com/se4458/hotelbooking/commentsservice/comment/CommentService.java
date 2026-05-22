package com.se4458.hotelbooking.commentsservice.comment;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public CommentResponse createComment(CreateCommentRequest request) {
        validateRating(request.overallRating(), "overallRating");
        if (request.serviceRatings() == null || request.serviceRatings().isEmpty()) {
            throw new IllegalArgumentException("serviceRatings is required.");
        }
        request.serviceRatings().forEach((key, value) -> {
            if (!StringUtils.hasText(key)) {
                throw new IllegalArgumentException("service rating key must not be blank.");
            }
            validateRating(value, "serviceRatings." + key);
        });
        return commentRepository.save(request);
    }

    public PageResponse<CommentResponse> getHotelComments(UUID hotelId, int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be greater than or equal to 0.");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("size must be between 1 and 100.");
        }
        List<CommentResponse> comments = commentRepository.findByHotelId(hotelId);
        int fromIndex = Math.min(page * size, comments.size());
        int toIndex = Math.min(fromIndex + size, comments.size());
        return PageResponse.of(comments.subList(fromIndex, toIndex), page, size, comments.size());
    }

    public RatingSummaryResponse getSummary(UUID hotelId) {
        List<CommentResponse> comments = commentRepository.findByHotelId(hotelId);
        double average = comments.stream()
                .mapToDouble(CommentResponse::overallRating)
                .average()
                .orElse(0);
        return new RatingSummaryResponse(hotelId, comments.size(), roundOneDecimal(average));
    }

    public RatingDistributionResponse getStarDistribution(UUID hotelId) {
        List<CommentResponse> comments = commentRepository.findByHotelId(hotelId);
        Map<Integer, Long> distribution = new LinkedHashMap<>();
        for (int rating = 1; rating <= 5; rating++) {
            distribution.put(rating, 0L);
        }
        for (CommentResponse comment : comments) {
            int bucket = Math.max(1, Math.min(5, (int) Math.round(comment.overallRating())));
            distribution.put(bucket, distribution.get(bucket) + 1);
        }
        return new RatingDistributionResponse(hotelId, distribution);
    }

    public ServiceRatingDistributionResponse getServiceDistribution(UUID hotelId) {
        List<CommentResponse> comments = commentRepository.findByHotelId(hotelId);
        Map<String, ServiceRatingStats> totals = new LinkedHashMap<>();
        for (CommentResponse comment : comments) {
            comment.serviceRatings().forEach((serviceName, rating) -> {
                ServiceRatingStats current = totals.getOrDefault(serviceName, new ServiceRatingStats(0, 0));
                totals.put(serviceName, new ServiceRatingStats(current.count() + 1, current.totalRating() + rating));
            });
        }

        Map<String, ServiceRatingSummary> summaries = new LinkedHashMap<>();
        totals.forEach((serviceName, stats) -> summaries.put(
                serviceName,
                new ServiceRatingSummary(stats.count(), roundOneDecimal((double) stats.totalRating() / stats.count()))
        ));
        return new ServiceRatingDistributionResponse(hotelId, summaries);
    }

    private void validateRating(double rating, String fieldName) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException(fieldName + " must be between 1 and 5.");
        }
    }

    private double roundOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private record ServiceRatingStats(long count, long totalRating) {
    }
}
