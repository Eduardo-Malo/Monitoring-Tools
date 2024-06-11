package com.hanse.analytics.controller;

import com.hanse.analytics.domain.Analytics;
import org.springframework.stereotype.Component;

@Component
public class AnalyticsMapper {
    /**
     * Converts a AnalyticsRequest to a Configuration entity.
     *
     * @param request the AnalyticsRequest to convert
     * @return the converted Analytics entity
     */
    public Analytics toAnalytics(AnalyticsRequest request) {
        if (request == null) {
            return null;
        }
        return Analytics.builder()
                        .jobName(request.jobName())
                        .errorMessage(request.errorMessage())
                        .responseTime(request.responseTime())
                        .result(request.result())
                        .responseCode(request.responseCode())
                        .build();
    }

    /**
     * Converts an Analytics entity to a AnalyticsResponse.
     *
     * @param analytics the Analytics entity to convert
     * @return the converted ConfigurationResponse
     */
    public AnalyticsResponse fromAnalytics(Analytics analytics) {
        if (analytics == null) {
            return null;
        }
        return new AnalyticsResponse(
                analytics.getId(),
                analytics.getJobName(),
                analytics.getErrorMessage(),
                analytics.getResponseTime(),
                analytics.getResult(),
                analytics.getResponseCode(),
                analytics.getCreatedAt()
        );
    }
}
