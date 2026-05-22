package com.se4458.hotelbooking.commentsservice.comment;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

@Repository
public class CommentRepository {

    private static final Logger log = LoggerFactory.getLogger(CommentRepository.class);

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;
    private final Map<UUID, List<CommentResponse>> fallbackComments = new ConcurrentHashMap<>();

    public CommentRepository(
            DynamoDbClient dynamoDbClient,
            @Value("${app.dynamodb.comments-table}") String tableName
    ) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
    }

    public CommentResponse save(CreateCommentRequest request) {
        UUID commentId = UUID.randomUUID();
        Instant createdAt = Instant.now();
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("hotelId", AttributeValue.fromS(request.hotelId().toString()));
        item.put("createdAt", AttributeValue.fromS(createdAt.toString()));
        item.put("commentId", AttributeValue.fromS(commentId.toString()));
        item.put("userId", AttributeValue.fromS(request.userId().toString()));
        item.put("overallRating", AttributeValue.fromN(Double.toString(request.overallRating())));
        item.put("comment", AttributeValue.fromS(request.comment()));
        item.put("serviceRatings", AttributeValue.fromM(toRatingMap(request.serviceRatings())));

        CommentResponse comment = new CommentResponse(
                commentId,
                request.hotelId(),
                request.userId(),
                request.overallRating(),
                request.serviceRatings(),
                request.comment(),
                createdAt
        );

        try {
            dynamoDbClient.putItem(PutItemRequest.builder()
                    .tableName(tableName)
                    .item(item)
                    .build());
        } catch (DynamoDbException | SdkClientException exception) {
            log.warn("Could not write comment to DynamoDB. Falling back to in-memory comments.", exception);
        }

        remember(comment);
        return comment;
    }

    public List<CommentResponse> findByHotelId(UUID hotelId) {
        try {
            QueryResponse response = dynamoDbClient.query(QueryRequest.builder()
                    .tableName(tableName)
                    .keyConditionExpression("hotelId = :hotelId")
                    .expressionAttributeValues(Map.of(
                            ":hotelId", AttributeValue.fromS(hotelId.toString())
                    ))
                    .scanIndexForward(false)
                    .build());

            List<CommentResponse> comments = new ArrayList<>();
            for (Map<String, AttributeValue> item : response.items()) {
                comments.add(toCommentResponse(item));
            }
            comments.sort(Comparator.comparing(CommentResponse::createdAt).reversed());
            return comments;
        } catch (DynamoDbException | SdkClientException exception) {
            log.warn("Could not read comments from DynamoDB. Falling back to in-memory comments.", exception);
        }

        return fallbackComments.getOrDefault(hotelId, List.of()).stream()
                .sorted(Comparator.comparing(CommentResponse::createdAt).reversed())
                .toList();
    }

    private CommentResponse toCommentResponse(Map<String, AttributeValue> item) {
        return new CommentResponse(
                UUID.fromString(item.get("commentId").s()),
                UUID.fromString(item.get("hotelId").s()),
                UUID.fromString(item.get("userId").s()),
                Double.parseDouble(item.get("overallRating").n()),
                fromRatingMap(item.get("serviceRatings").m()),
                item.get("comment").s(),
                Instant.parse(item.get("createdAt").s())
        );
    }

    private Map<String, AttributeValue> toRatingMap(Map<String, Integer> serviceRatings) {
        Map<String, AttributeValue> result = new HashMap<>();
        serviceRatings.forEach((key, value) -> result.put(key, AttributeValue.fromN(Integer.toString(value))));
        return result;
    }

    private Map<String, Integer> fromRatingMap(Map<String, AttributeValue> serviceRatings) {
        Map<String, Integer> result = new HashMap<>();
        serviceRatings.forEach((key, value) -> result.put(key, Integer.parseInt(value.n())));
        return result;
    }

    private void remember(CommentResponse comment) {
        fallbackComments.compute(comment.hotelId(), (hotelId, comments) -> {
            List<CommentResponse> updated = comments == null ? new ArrayList<>() : new ArrayList<>(comments);
            updated.add(comment);
            return List.copyOf(updated);
        });
    }
}
