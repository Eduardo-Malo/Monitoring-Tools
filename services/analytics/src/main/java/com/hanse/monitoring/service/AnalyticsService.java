package com.hanse.monitoring.service;

import com.hanse.monitoring.controller.AnalyticsMapper;
import com.hanse.monitoring.controller.AnalyticsRequest;
import com.hanse.monitoring.controller.AnalyticsResponse;
import com.hanse.monitoring.repository.AnalyticsRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final AnalyticsRepository repository;
    private final AnalyticsMapper mapper;

    @Transactional
    public Integer createAnalytics(AnalyticsRequest request) {
        var analytics = mapper.toAnalytics(request);
        analytics.setCreatedAt(LocalDateTime.now());
        this.repository.save(analytics);
        log.info("Analytics created with ID: {}", analytics.getId());
        return analytics.getId();
    }

    public List<AnalyticsResponse> findAllAnalytics(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Integer responseCode,
            Boolean result,
            Integer jobId,
            Double minResponseTime,
            Double maxResponseTime
    ) {
        return this.repository.findAllAnalytics(startDate, endDate, responseCode, result, jobId, minResponseTime, maxResponseTime)
                              .stream()
                              .map(this.mapper::fromAnalytics)
                              .toList();
    }

    public AnalyticsResponse findById(Integer analyticsId) {
        return this.repository.findById(analyticsId)
                              .map(this.mapper::fromAnalytics)
                              .orElseThrow(() -> logAndThrowEntityNotFoundException(analyticsId));
    }

    private EntityNotFoundException logAndThrowEntityNotFoundException(Integer analyticsId) {
        String message = String.format("No analytics found with the provided ID: %d", analyticsId);
        log.error(message);
        throw new EntityNotFoundException(message);
    }
}
