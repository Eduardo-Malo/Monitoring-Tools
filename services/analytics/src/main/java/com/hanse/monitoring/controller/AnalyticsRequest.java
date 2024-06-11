package com.hanse.monitoring.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record AnalyticsRequest(
        @NotNull(message = "jobId should be present")
        Integer jobId,
        @NotNull(message = "jobName should be present")
        @NotEmpty(message = "jobName should be present")
        @NotBlank(message = "jobName should be present")
        String jobName,
        @NotNull(message = "uri should be present")
        @NotEmpty(message = "uri should be present")
        @NotBlank(message = "uri should be present")
        String uri,
        String errorMessage,
        @NotNull(message = "responseTime should be present")
        Long responseTime,
        @NotNull(message = "result should be present")
        Boolean result,
        @NotNull(message = "responseCode should be present")
        Integer responseCode
) {
}

