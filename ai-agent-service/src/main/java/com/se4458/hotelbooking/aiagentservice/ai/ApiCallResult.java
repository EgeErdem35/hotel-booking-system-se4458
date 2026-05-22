package com.se4458.hotelbooking.aiagentservice.ai;

public record ApiCallResult(
        String method,
        String path,
        int status
) {
}
