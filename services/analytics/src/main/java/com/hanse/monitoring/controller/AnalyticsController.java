package com.hanse.monitoring.controller;

import com.hanse.monitoring.domain.Analytics;
import com.hanse.monitoring.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService service;

    @PostMapping
    public ResponseEntity<Integer> createAnalytics(
            @Validated @RequestBody AnalyticsRequest request
    ) {
        return ResponseEntity.ok(this.service.createAnalytics(request));
    }

    @GetMapping
    public ResponseEntity<List<AnalyticsResponse>> findAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Integer responseCode,
            @RequestParam(required = false) Boolean result,
            @RequestParam(required = false) Integer jobId,
            @RequestParam(required = false) Double minResponseTime,
            @RequestParam(required = false) Double maxResponseTime,
            @RequestParam(required = false, defaultValue = "createdAt,desc") String[] sort){
        AnalyticsFilter filter = new AnalyticsFilter(startDate, endDate, responseCode, result, jobId, minResponseTime, maxResponseTime, sort);
        return ResponseEntity.ok(this.service.findAllAnalytics(filter));
    }

    @GetMapping("/{analytics-id}")
    public ResponseEntity<AnalyticsResponse> findById(
            @PathVariable("analytics-id") Integer analyticsId
    ) {
        return ResponseEntity.ok(this.service.findById(analyticsId));
    }

}
