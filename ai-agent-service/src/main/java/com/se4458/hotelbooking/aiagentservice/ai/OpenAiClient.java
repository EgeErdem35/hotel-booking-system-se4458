package com.se4458.hotelbooking.aiagentservice.ai;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class OpenAiClient {

    private final RestClient restClient;
    private final String apiKey;
    private final String model;
    private final String baseUrl;

    public OpenAiClient(
            RestClient.Builder restClientBuilder,
            @Value("${app.openai.api-key:}") String apiKey,
            @Value("${app.openai.model:gpt-4.1-mini}") String model,
            @Value("${app.openai.base-url:https://api.openai.com}") String baseUrl
    ) {
        this.restClient = restClientBuilder.build();
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.model = model;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    public Optional<String> complete(String userMessage, ParsedIntent intent, JsonNode apiData) {
        if (!StringUtils.hasText(apiKey)) {
            return Optional.empty();
        }

        Map<String, Object> request = Map.of(
                "model", model,
                "instructions", """
                        You are the AI hotel booking assistant for the SE4458 Hotel Booking System.
                        Use the provided project API data as the source of truth.
                        Keep the response concise, practical, and mention concrete hotel or booking details when available.
                        If required information is missing, ask one clear follow-up question.
                        """,
                "input", List.of(
                        Map.of(
                                "role", "user",
                                "content", List.of(Map.of(
                                        "type", "input_text",
                                        "text", """
                                                User message:
                                                %s

                                                Parsed intent:
                                                %s

                                                Project API data:
                                                %s
                                                """.formatted(userMessage, intent, apiData == null ? "{}" : apiData.toString())
                                ))
                        )
                )
        );

        try {
            JsonNode response = restClient.post()
                    .uri(baseUrl + "/v1/responses")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(JsonNode.class);

            return extractText(response);
        } catch (RestClientException ex) {
            return Optional.empty();
        }
    }

    private Optional<String> extractText(JsonNode response) {
        JsonNode outputText = response.path("output_text");
        if (outputText.isTextual() && StringUtils.hasText(outputText.asText())) {
            return Optional.of(outputText.asText());
        }

        JsonNode output = response.path("output");
        if (output.isArray()) {
            StringBuilder text = new StringBuilder();
            for (JsonNode item : output) {
                JsonNode content = item.path("content");
                if (!content.isArray()) {
                    continue;
                }
                for (JsonNode contentItem : content) {
                    JsonNode value = contentItem.path("text");
                    if (value.isTextual()) {
                        text.append(value.asText()).append("\n");
                    }
                }
            }
            if (!text.isEmpty()) {
                return Optional.of(text.toString().trim());
            }
        }

        return Optional.empty();
    }
}
