package com.se4458.hotelbooking.aiagentservice.ai;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AiChatService {

    private final IntentParser intentParser;
    private final ProjectApiClient projectApiClient;
    private final OpenAiClient openAiClient;

    public AiChatService(
            IntentParser intentParser,
            ProjectApiClient projectApiClient,
            OpenAiClient openAiClient
    ) {
        this.intentParser = intentParser;
        this.projectApiClient = projectApiClient;
        this.openAiClient = openAiClient;
    }

    public AiChatResponse chat(AiChatRequest request) {
        String sessionId = request.sessionId() == null || request.sessionId().isBlank()
                ? UUID.randomUUID().toString()
                : request.sessionId();

        ParsedIntent intent = intentParser.parse(request.message());
        List<ApiCallResult> apiCalls = new ArrayList<>();
        JsonNode apiData = null;
        String fallbackReply;

        try {
            if ("booking".equals(intent.action()) && canCreateBooking(intent)) {
                ProjectApiClient.ApiResponse booking = projectApiClient.createBooking(intent);
                apiCalls.add(booking.call());
                apiData = booking.body();
                fallbackReply = bookingReply(apiData);
            } else if (canSearch(intent)) {
                ProjectApiClient.ApiResponse search = projectApiClient.searchHotels(intent);
                apiCalls.add(search.call());
                apiData = search.body();
                fallbackReply = searchReply(intent, apiData);
            } else {
                fallbackReply = missingInfoReply(intent);
            }
        } catch (Exception ex) {
            fallbackReply = "I understood the request, but a project API call failed: " + ex.getMessage();
        }

        String reply = openAiClient.complete(request.message(), intent, apiData)
                .orElse(fallbackReply);

        return new AiChatResponse(sessionId, reply, intent, apiCalls, apiData);
    }

    private boolean canSearch(ParsedIntent intent) {
        return intent.destination() != null
                && intent.checkIn() != null
                && intent.checkOut() != null
                && intent.guests() != null;
    }

    private boolean canCreateBooking(ParsedIntent intent) {
        return intent.roomId() != null
                && intent.userId() != null
                && intent.checkIn() != null
                && intent.checkOut() != null
                && intent.guests() != null;
    }

    private String searchReply(ParsedIntent intent, JsonNode apiData) {
        JsonNode content = apiData.path("content");
        if (!content.isArray() || content.isEmpty()) {
            return "I searched %s from %s to %s for %d guests, but no available hotels were returned."
                    .formatted(intent.destination(), intent.checkIn(), intent.checkOut(), intent.guests());
        }

        JsonNode first = content.get(0);
        return "I found %d hotel option(s). Top match: %s in %s, from %s per night. Select a hotel in the UI to view rooms and book."
                .formatted(
                        content.size(),
                        first.path("name").asText("hotel"),
                        first.path("destination").asText(intent.destination()),
                        first.path("lowestPricePerNight").asText("the listed price")
                );
    }

    private String bookingReply(JsonNode apiData) {
        return "Booking confirmed. Reservation %s is %s for %s, total price %s."
                .formatted(
                        apiData.path("id").asText("created"),
                        apiData.path("status").asText("CONFIRMED"),
                        apiData.path("hotelName").asText("the selected hotel"),
                        apiData.path("totalPrice").asText("calculated by booking service")
                );
    }

    private String missingInfoReply(ParsedIntent intent) {
        if ("booking".equals(intent.action())) {
            return "To create a booking, send roomId, userId, checkIn, checkOut, and guest count. You can pick a room from the hotel detail panel first.";
        }
        return "Tell me the destination, check-in date, check-out date, and guest count. Example: Find a hotel in Istanbul from 2026-07-15 to 2026-07-18 for 2 guests.";
    }
}
