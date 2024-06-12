package com.hanse.monitoring;

import com.hanse.monitoring.analytics.client.AnalyticsHttpClient;
import com.hanse.monitoring.analytics.domain.Analytics;
import com.hanse.monitoring.configuration.client.ConfigurationHttpClient;
import com.hanse.monitoring.configuration.domain.Configuration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class Startup implements ApplicationRunner {

    private final AnalyticsHttpClient analyticsHttpClient;
    private final ConfigurationHttpClient configurationHttpClient;
    private final RestClient monitorClient;
    private ScheduledExecutorService executorService;
    List<ScheduledFuture<?>> scheduledFutures;
    @Value("${application.max-configurations}")
    private Integer maxConfigurations;

    public Startup(ConfigurationHttpClient configurationClient, AnalyticsHttpClient analyticsHttpClient, RestClient monitorClient) {
        this.configurationHttpClient = configurationClient;
        this.analyticsHttpClient = analyticsHttpClient;
        this.monitorClient = monitorClient;
        scheduledFutures = new ArrayList<>();
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Monitoring started.");
        this.executorService = Executors.newScheduledThreadPool(maxConfigurations);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            shutdown();
            try {
                if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    shutdown();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
           log.info("Shutting down the application...");
        }));


        configurationHttpClient.listConfigurations(true).forEach(this::launchJob);
    }

    public void launchJob(Configuration configuration) {
        Runnable jobTask = () -> {
            log.info("Launching job for configuration: {}", configuration);
            long startTime = System.currentTimeMillis();
            try {
                ResponseEntity<String> monitoringResponse = monitorClient.get().uri(configuration.uri()).retrieve().toEntity(String.class);
                long endTime = System.currentTimeMillis();
                double responseTimeSeconds = ((endTime - startTime) / 1000.0);
                HttpStatusCode statusCode = monitoringResponse.getStatusCode();
                analyticsHttpClient.insertAnalytics(new Analytics(configuration.id(), configuration.name(), configuration.uri(), statusCode.isError() ? statusCode.toString() : "", responseTimeSeconds, !statusCode.isError(), statusCode.value()));
            } catch (Exception e) {
                long endTime = System.currentTimeMillis();
                double responseTimeSeconds = ((endTime - startTime) / 1000.0);
                log.error("Error occurred while retrieving monitoring data: {}", e.getMessage());
                analyticsHttpClient.insertAnalytics(new Analytics(configuration.id(), configuration.name(), configuration.uri(), e.getMessage(), responseTimeSeconds, false, 0));
            }
        };

        scheduledFutures.add(executorService.scheduleAtFixedRate(
                jobTask,
                0,
                configuration.interval(),
                TimeUnit.SECONDS
        ));
    }

    public void shutdown() {
        log.info("Gracefully shutting down the threads...");
        executorService.shutdown();
    }
}
