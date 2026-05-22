package com.se4458.hotelbooking.aiagentservice.ai;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ProjectApiClient {

    private final RestClient restClient;
    private final String apiGatewayUrl;

    public ProjectApiClient(
            RestClient.Builder restClientBuilder,
            @Value("${app.services.api-gateway-url}") String apiGatewayUrl
    ) {
        this.restClient = restClientBuilder.build();
        this.apiGatewayUrl = trimTrailingSlash(apiGatewayUrl);
    }

    public ApiResponse searchHotels(ParsedIntent intent) {
        String path = "/api/v1/hotels/search";
        JsonNode body = restClient.get()
                .uri(apiGatewayUrl + path + "?destination={destination}&checkIn={checkIn}&checkOut={checkOut}&guests={guests}&page=0&size=5",
                        intent.destination(), intent.checkIn(), intent.checkOut(), intent.guests())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new IllegalStateException("Hotel search API returned " + response.getStatusCode().value());
                })
                .body(JsonNode.class);
        return new ApiResponse(new ApiCallResult("GET", path, 200), body);
    }

    public ApiResponse createBooking(ParsedIntent intent) {
        String path = "/api/v1/bookings";
        JsonNode body = restClient.post()
                .uri(apiGatewayUrl + path)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(new BookingPayload(intent.roomId(), intent.userId(), intent.checkIn(), intent.checkOut(), intent.guests()))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new IllegalStateException("Booking API returned " + response.getStatusCode().value());
                })
                .body(JsonNode.class);
        return new ApiResponse(new ApiCallResult("POST", path, 201), body);
    }

    private String trimTrailingSlash(String value) {
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }

    private record BookingPayload(
            UUID roomId,
            UUID userId,
            java.time.LocalDate checkIn,
            java.time.LocalDate checkOut,
            Integer guestCount
    ) {
    }

    public record ApiResponse(ApiCallResult call, JsonNode body) {
    }
}
