package com.hanse.monitoring.service;

import com.hanse.monitoring.controller.AnalyticsFilter;
import com.hanse.monitoring.controller.AnalyticsMapper;
import com.hanse.monitoring.controller.AnalyticsRequest;
import com.hanse.monitoring.controller.AnalyticsResponse;
import com.hanse.monitoring.repository.AnalyticsRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
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

    public List<AnalyticsResponse> findAllAnalytics(AnalyticsFilter filter) {
        return this.repository.findAllAnalytics(
                           filter.startDate(),
                           filter.endDate(),
                           filter.responseCode(),
                           filter.result(),
                           filter.jobId(),
                           filter.minResponseTime(),
                           filter.maxResponseTime(),
                           Sort.by(getSortDirection(filter.sort()[1]), filter.sort()[0]))
                              .stream()
                              .map(this.mapper::fromAnalytics)
                              .toList();
    }

    private Sort.Direction getSortDirection(String sort) {
        return sort.endsWith("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
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
