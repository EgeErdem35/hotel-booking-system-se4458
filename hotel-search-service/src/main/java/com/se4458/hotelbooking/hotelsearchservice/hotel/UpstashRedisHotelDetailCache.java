package com.se4458.hotelbooking.hotelsearchservice.hotel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Component
public class UpstashRedisHotelDetailCache implements HotelDetailCache {

    private static final Logger log = LoggerFactory.getLogger(UpstashRedisHotelDetailCache.class);
    private static final String KEY_PREFIX = "hotel:details:";

    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final String restUrl;
    private final String restToken;
    private final long ttlSeconds;

    public UpstashRedisHotelDetailCache(
            ObjectMapper objectMapper,
            RestClient.Builder restClientBuilder,
            @Value("${app.redis.upstash-rest-url:}") String restUrl,
            @Value("${app.redis.upstash-rest-token:}") String restToken,
            @Value("${app.redis.hotel-detail-ttl-seconds:300}") long ttlSeconds
    ) {
        this.objectMapper = objectMapper;
        this.restClient = restClientBuilder.build();
        this.restUrl = restUrl == null ? "" : restUrl.trim();
        this.restToken = restToken == null ? "" : restToken.trim();
        this.ttlSeconds = ttlSeconds;
    }

    @Override
    public Optional<HotelDetailResponse> get(UUID hotelId) {
        if (!isEnabled()) {
            return Optional.empty();
        }

        try {
            JsonNode response = execute(List.of("GET", key(hotelId)));
            JsonNode result = response.get("result");
            if (result == null || result.isNull()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(result.asText(), HotelDetailResponse.class));
        } catch (Exception ex) {
            log.warn("Could not read hotel detail cache for hotel {}.", hotelId, ex);
            return Optional.empty();
        }
    }

    @Override
    public void put(UUID hotelId, HotelDetailResponse hotelDetail) {
        if (!isEnabled()) {
            return;
        }

        try {
            String payload = objectMapper.writeValueAsString(hotelDetail);
            execute(List.of("SETEX", key(hotelId), String.valueOf(ttlSeconds), payload));
        } catch (Exception ex) {
            log.warn("Could not write hotel detail cache for hotel {}.", hotelId, ex);
        }
    }

    private JsonNode execute(List<String> command) throws Exception {
        String body = restClient.post()
                .uri(restUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + restToken)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(command)
                .retrieve()
                .body(String.class);

        JsonNode response = objectMapper.readTree(body);
        JsonNode error = response.get("error");
        if (error != null && !error.isNull()) {
            throw new IllegalStateException(error.asText());
        }
        return response;
    }

    private boolean isEnabled() {
        return StringUtils.hasText(restUrl) && StringUtils.hasText(restToken) && ttlSeconds > 0;
    }

    private String key(UUID hotelId) {
        return KEY_PREFIX + hotelId;
    }
}
