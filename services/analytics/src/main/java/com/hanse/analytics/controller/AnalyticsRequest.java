package com.hanse.analytics.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record AnalyticsRequest(
        @NotNull(message = "jobName should be present")
        @NotEmpty(message = "jobName should be present")
        @NotBlank(message = "jobName should be present")
        String jobName,
        String errorMessage,
        @NotNull(message = "responseTime should be present")
        Long responseTime,
        @NotNull(message = "result should be present")
        Boolean result,
        @NotNull(message = "responseCode should be present")
        Integer responseCode
) {
}

