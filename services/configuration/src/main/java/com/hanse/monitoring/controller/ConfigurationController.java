package com.hanse.monitoring.controller;

import com.hanse.monitoring.service.ConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/configurations")
@RequiredArgsConstructor
public class ConfigurationController {

    private final ConfigurationService service;

    @PostMapping
    public ResponseEntity<Integer> createConfiguration(
            @Validated(OnCreate.class) @RequestBody ConfigurationRequest request
    ) {
        return ResponseEntity.ok(this.service.createConfiguration(request));
    }

    @PatchMapping("/{configuration-id}")
    public ResponseEntity<Boolean> updateConfiguration(
            @PathVariable("configuration-id") Integer configurationId,
            @Validated(OnUpdate.class) @RequestBody ConfigurationRequest request
    ) {
        return ResponseEntity.ok(this.service.updateConfiguration(configurationId, request));
    }

    @GetMapping
    public ResponseEntity<List<ConfigurationResponse>> findAll(@RequestParam(required = false) Boolean active) {
        return ResponseEntity.ok(this.service.findAllConfigurations(active));
    }

    @GetMapping("/{configuration-id}")
    public ResponseEntity<ConfigurationResponse> findById(
            @PathVariable("configuration-id") Integer configurationId
    ) {
        return ResponseEntity.ok(this.service.findById(configurationId));
    }

    @DeleteMapping("/{configuration-id}")
    public ResponseEntity<Void> deleteById(
            @PathVariable("configuration-id") Integer configurationId
    ) {
        this.service.deleteById(configurationId);
        return ResponseEntity.accepted().build();
    }

}
