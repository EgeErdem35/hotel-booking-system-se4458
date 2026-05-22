package com.se4458.hotelbooking.aiagentservice.ai;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

public record AiChatResponse(
        String sessionId,
        String reply,
        ParsedIntent intent,
        List<ApiCallResult> apiCalls,
        JsonNode data
) {
}
