package com.se4458.hotelbooking.aiagentservice.ai;

import jakarta.validation.constraints.NotBlank;

public record AiChatRequest(
        String sessionId,
        @NotBlank String message
) {
}
