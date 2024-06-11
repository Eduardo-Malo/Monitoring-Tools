package com.hanse.monitoring.controller;

import com.hanse.monitoring.domain.Configuration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationMapperTest {

    private final ConfigurationMapper mapper = new ConfigurationMapper();

    @Test
    void toConfiguration_ShouldMapRequestToEntity() {
        ConfigurationRequest request = new ConfigurationRequest("name", "uri", 10, true);

        Configuration configuration = mapper.toConfiguration(request);

        assertNotNull(configuration);
        assertEquals(request.name(), configuration.getName());
        assertEquals(request.uri(), configuration.getUri());
        assertEquals(request.interval(), configuration.getInterval());
    }

    @Test
    void toConfiguration_ShouldReturnNull_WhenRequestIsNull() {
        Configuration configuration = mapper.toConfiguration(null);

        assertNull(configuration);
    }

    @Test
    void fromConfiguration_ShouldMapEntityToResponse() {
        Configuration configuration = Configuration.builder()
                                                   .id(1)
                                                   .name("name")
                                                   .uri("uri")
                                                   .interval(10)
                                                   .build();

        ConfigurationResponse response = mapper.fromConfiguration(configuration);

        assertNotNull(response);
        assertEquals(configuration.getId(), response.id());
        assertEquals(configuration.getName(), response.name());
        assertEquals(configuration.getUri(), response.uri());
        assertEquals(configuration.getInterval(), response.interval());
    }

    @Test
    void fromConfiguration_ShouldReturnNull_WhenEntityIsNull() {
        ConfigurationResponse response = mapper.fromConfiguration(null);

        assertNull(response);
    }
}
