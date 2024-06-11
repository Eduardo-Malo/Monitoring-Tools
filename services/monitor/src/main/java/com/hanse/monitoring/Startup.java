package com.hanse.monitoring;

import com.hanse.monitoring.analytics.client.AnalyticsHttpClient;
import com.hanse.monitoring.analytics.domain.Analytics;
import com.hanse.monitoring.configuration.client.ConfigurationHttpClient;
import com.hanse.monitoring.configuration.domain.Configuration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class Startup implements ApplicationRunner {

    private final AnalyticsHttpClient analyticsHttpClient;
    private final ConfigurationHttpClient configurationHttpClient;
    private final RestClient monitorClient;

    public Startup(ConfigurationHttpClient configurationClient, AnalyticsHttpClient analyticsHttpClient, RestClient monitorClient) {
        this.configurationHttpClient = configurationClient;
        this.analyticsHttpClient = analyticsHttpClient;
        this.monitorClient = monitorClient;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Monitoring started.");
        configurationHttpClient.listConfigurations(true).forEach(this::launchJob);
    }

    private void launchJob(Configuration configuration) {
        log.info("Launching job for configuration: {}", configuration);
        long startTime = System.currentTimeMillis();
        try {
            ResponseEntity<String> monitoringResponse = monitorClient.get().uri(configuration.uri()).retrieve().toEntity(String.class);
            long endTime = System.currentTimeMillis();
            double responseTimeSeconds = ((endTime - startTime) / 1000.0);
            var statusCode = monitoringResponse.getStatusCode();
            analyticsHttpClient.insertAnalytics(new Analytics(configuration.id(), configuration.name(), configuration.uri(), statusCode.isError() ? statusCode.toString() : "", responseTimeSeconds, !statusCode.isError(), statusCode.value()));
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            double responseTimeSeconds = ((endTime - startTime) / 1000.0);
            log.error("Error occurred while retrieving monitoring data: {}", e.getMessage());
            analyticsHttpClient.insertAnalytics(new Analytics(configuration.id(), configuration.name(), configuration.uri(), e.getMessage(), responseTimeSeconds, false, 0));
        }
    }
}
