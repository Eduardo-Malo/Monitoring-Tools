package com.hanse.analytics.service;

import com.hanse.analytics.controller.AnalyticsMapper;
import com.hanse.analytics.controller.AnalyticsRequest;
import com.hanse.analytics.controller.AnalyticsResponse;
import com.hanse.analytics.repository.AnalyticsRepository;
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

    public List<AnalyticsResponse> findAllAnalytics() {
        return this.repository.findAll()
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
