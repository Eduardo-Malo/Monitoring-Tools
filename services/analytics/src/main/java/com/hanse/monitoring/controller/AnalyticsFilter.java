package com.hanse.monitoring.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record AnalyticsFilter(
        LocalDateTime startDate,
        LocalDateTime endDate,
        Integer responseCode,
        Boolean result,
        Integer jobId,
        Double minResponseTime,
        Double maxResponseTime,
        String[] sort
) {
}

