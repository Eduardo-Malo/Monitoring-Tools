package com.hanse.monitoring.controller;

import com.hanse.monitoring.domain.Configuration;
import org.springframework.stereotype.Service;

@Service
public class ConfigurationMapper {

    /**
     * Converts a ConfigurationRequest to a Configuration entity.
     *
     * @param request the ConfigurationRequest to convert
     * @return the converted Configuration entity
     */
    public Configuration toConfiguration(ConfigurationRequest request) {
        if (request == null) {
            return null;
        }
        return Configuration.builder()
                            .name(request.name())
                            .uri(request.uri())
                            .interval(request.interval())
                            .build();
    }

    /**
     * Converts a Configuration entity to a ConfigurationResponse.
     *
     * @param configuration the Configuration entity to convert
     * @return the converted ConfigurationResponse
     */
    public ConfigurationResponse fromConfiguration(Configuration configuration) {
        if (configuration == null) {
            return null;
        }
        return new ConfigurationResponse(
                configuration.getId(),
                configuration.getName(),
                configuration.getUri(),
                configuration.getInterval()
        );
    }
}
