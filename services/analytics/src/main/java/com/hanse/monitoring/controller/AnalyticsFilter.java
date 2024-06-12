package com.hanse.monitoring.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.domain.Pageable;

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
        Pageable page
) {
}

