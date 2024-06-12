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
    public void run(ApplicationArguments args) {
        log.info("Monitoring started.");
        this.executorService = Executors.newScheduledThreadPool(maxConfigurations, Thread.ofVirtual().factory());
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
        configurationHttpClient.listConfigurations(true).forEach(this::scheduleJob);
    }

    private void scheduleJob(Configuration configuration) {
        Runnable jobTask = () -> {
            log.info("Launching job for configuration: {}", configuration);
            executeJob(configuration);
        };
        ScheduledFuture<?> future = executorService.scheduleAtFixedRate(
                jobTask,
                configuration.interval(),
                configuration.interval(),
                TimeUnit.SECONDS
        );
        scheduledFutures.add(future);
    }

    public void executeJob(Configuration configuration) {
        long startTime = System.currentTimeMillis();
        try {
            ResponseEntity<String> monitoringResponse = monitorClient.get()
                                                                     .uri(configuration.uri())
                                                                     .retrieve()
                                                                     .toEntity(String.class);
            long endTime = System.currentTimeMillis();
            double responseTimeSeconds = ((endTime - startTime) / 1000.0);
            HttpStatusCode statusCode = monitoringResponse.getStatusCode();
            analyticsHttpClient.insertAnalytics(new Analytics(configuration.id(), configuration.name(), configuration.uri(), statusCode.isError() ? statusCode.toString() : "", responseTimeSeconds, !statusCode.isError(), statusCode.value()));
        } catch (Exception e) {
            handleException(configuration, startTime, e);
        }
    }

    private void handleException(Configuration configuration, long startTime, Exception e) {
        long endTime = System.currentTimeMillis();
        double responseTimeSeconds = ((endTime - startTime) / 1000.0);
        log.error("Error occurred while retrieving monitoring data for configuration {}: {}", configuration, e.getMessage());
        analyticsHttpClient.insertAnalytics(new Analytics(configuration.id(), configuration.name(), configuration.uri(), e.getMessage(), responseTimeSeconds, false, 0));
    }

    public void shutdown() {
        log.info("Shutting down the application...");
        this.executorService.shutdown();
        try {
            if (!this.executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                log.error("Executor service did not terminate gracefully within 10 seconds.");
                List<Runnable> droppedTasks = this.executorService.shutdownNow();
                log.error("Executor service forcibly terminated. Dropped tasks: {}", droppedTasks.size());
            }
        } catch (InterruptedException e) {
            log.error("Interrupted while awaiting termination of executor service: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
