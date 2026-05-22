package com.se4458.hotelbooking.commentsservice.comment;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public CommentResponse createComment(@Valid @RequestBody CreateCommentRequest request) {
        return commentService.createComment(request);
    }

    @GetMapping("/hotel/{hotelId}")
    public PageResponse<CommentResponse> getHotelComments(
            @PathVariable UUID hotelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return commentService.getHotelComments(hotelId, page, size);
    }

    @GetMapping("/hotel/{hotelId}/summary")
    public RatingSummaryResponse getSummary(@PathVariable UUID hotelId) {
        return commentService.getSummary(hotelId);
    }

    @GetMapping("/hotel/{hotelId}/distribution")
    public RatingDistributionResponse getStarDistribution(@PathVariable UUID hotelId) {
        return commentService.getStarDistribution(hotelId);
    }

    @GetMapping("/hotel/{hotelId}/service-distribution")
    public ServiceRatingDistributionResponse getServiceDistribution(@PathVariable UUID hotelId) {
        return commentService.getServiceDistribution(hotelId);
    }
}
