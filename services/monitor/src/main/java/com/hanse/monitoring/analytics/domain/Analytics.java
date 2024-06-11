package com.hanse.monitoring.analytics.domain;

public record Analytics(
        Integer jobId,
        String jobName,
        String uri,
        String errorMessage,
        Double responseTime,
        Boolean result,
        Integer responseCode
) {
}
