package com.hanse.monitoring.controller;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record AnalyticsResponse(
        Integer id,
        Integer jobId,
        String jobName,
        String uri,
        String errorMessage,
        Long responseTime,
        Boolean result,
        Integer responseCode,
        LocalDateTime createdAt
) {
}

