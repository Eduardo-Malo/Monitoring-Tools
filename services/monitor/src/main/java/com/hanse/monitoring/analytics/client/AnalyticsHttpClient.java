package com.hanse.monitoring.analytics.client;

import com.hanse.monitoring.analytics.domain.Analytics;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

public interface AnalyticsHttpClient {

    @PostExchange("/analytics")
    void insertAnalytics(@RequestBody Analytics analytics);
}
