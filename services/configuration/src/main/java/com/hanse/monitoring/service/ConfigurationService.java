package com.hanse.monitoring.service;

import com.hanse.monitoring.controller.ConfigurationMapper;
import com.hanse.monitoring.controller.ConfigurationRequest;
import com.hanse.monitoring.controller.ConfigurationResponse;
import com.hanse.monitoring.domain.Configuration;
import com.hanse.monitoring.exception.MaxConfigurationsException;
import com.hanse.monitoring.repository.ConfigurationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfigurationService {

    private final ConfigurationRepository repository;
    private final ConfigurationMapper mapper;
    @Value("${application.max-configurations}")
    private Integer maxConfigurations;

    @Transactional
    public Integer createConfiguration(ConfigurationRequest request) {
        if (Boolean.TRUE.equals(request.active())) {
            validateMaxActiveConfigurations();
        }
        var configuration = mapper.toConfiguration(request);
        configuration.setCreatedAt(LocalDateTime.now());
        this.repository.save(configuration);
        log.info("Configuration created with ID: {}", configuration.getId());
        return configuration.getId();
    }

    private void validateMaxActiveConfigurations() {
        long count = this.repository.findByActive(true).size();
        if (count >= maxConfigurations) {
            log.warn("Maximum number of active configurations reached, current count: {}", count);
            throw new MaxConfigurationsException(String.format("Maximum number of %d active configurations reached", maxConfigurations));
        }
    }

    @Transactional
    public Boolean updateConfiguration(Integer configurationId, ConfigurationRequest request) {
        return repository.findById(configurationId).map(configuration -> {
            boolean updated = updateConfigurationFields(configuration, request);
            if (updated) {
                configuration.setModifiedAt(LocalDateTime.now());
                repository.save(configuration);
                log.info("Configuration updated with ID: {}", configurationId);
            }
            return updated;
        }).orElseThrow(() -> logAndThrowEntityNotFoundException(configurationId));
    }

    private boolean updateConfigurationFields(Configuration configuration, ConfigurationRequest request) {
        boolean updated = false;
        if (request.name() != null && !request.name().isEmpty()) {
            configuration.setName(request.name());
            updated = true;
        }
        if (request.uri() != null && !request.uri().isEmpty()) {
            configuration.setUri(request.uri());
            updated = true;
        }
        if (request.interval() != null) {
            configuration.setInterval(request.interval());
            updated = true;
        }

        if (request.active() != null) {
            if(Boolean.TRUE.equals(request.active()) && Boolean.FALSE.equals(configuration.getActive())){
                validateMaxActiveConfigurations();
            }
            configuration.setActive(request.active());
            updated = true;
        }
        return updated;
    }

    public List<ConfigurationResponse> findAllConfigurations(Boolean active) {
        if (active != null) {
            return this.repository.findByActive(active)
                    .stream()
                    .map(this.mapper::fromConfiguration)
                    .toList();
        }else{
            return this.repository.findAll()
                    .stream()
                    .map(this.mapper::fromConfiguration)
                    .toList();
        }
    }

    public ConfigurationResponse findById(Integer configurationId) {
        return this.repository.findById(configurationId)
                 .map(this.mapper::fromConfiguration)
                 .orElseThrow(() -> logAndThrowEntityNotFoundException(configurationId));
    }

    public void deleteById(Integer configurationId) {
        if (this.repository.existsById(configurationId)) {
            this.repository.deleteById(configurationId);
            log.info("Configuration deleted with ID: {}", configurationId);
        } else {
            throw logAndThrowEntityNotFoundException(configurationId);
        }
    }

    private EntityNotFoundException logAndThrowEntityNotFoundException(Integer configurationId) {
        String message = String.format("No configuration found with the provided ID: %d", configurationId);
        log.error(message);
        throw new EntityNotFoundException(message);
    }
}
