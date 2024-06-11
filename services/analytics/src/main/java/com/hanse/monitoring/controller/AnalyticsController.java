package com.hanse.monitoring.controller;

import com.hanse.monitoring.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<List<AnalyticsResponse>> findAll() {
        return ResponseEntity.ok(this.service.findAllAnalytics());
    }

    @GetMapping("/{analytics-id}")
    public ResponseEntity<AnalyticsResponse> findById(
            @PathVariable("analytics-id") Integer analyticsId
    ) {
        return ResponseEntity.ok(this.service.findById(analyticsId));
    }

}
