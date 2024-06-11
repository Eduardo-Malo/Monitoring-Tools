package com.hanse.monitoring.service;

import com.hanse.monitoring.controller.ConfigurationMapper;
import com.hanse.monitoring.controller.ConfigurationRequest;
import com.hanse.monitoring.controller.ConfigurationResponse;
import com.hanse.monitoring.domain.Configuration;
import com.hanse.monitoring.exception.MaxConfigurationsException;
import com.hanse.monitoring.repository.ConfigurationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigurationServiceTest {

    @Mock
    private ConfigurationRepository repository;

    @Mock
    private ConfigurationMapper mapper;

    @InjectMocks
    private ConfigurationService service;

    private Integer maxConfigurations;

    @BeforeEach
    void setUp() {
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("application.yml"));
        Properties properties = yaml.getObject();
        assert properties != null;
        maxConfigurations = Integer.valueOf(properties.getProperty("application.max-configurations"));
        ReflectionTestUtils.setField(service, "maxConfigurations", maxConfigurations);
    }

    private static Stream<Arguments> provideCreateConfigurationRequests() {
        return Stream.of(
                Arguments.of(new ConfigurationRequest("name1", "uri1", 10, true), 1),
                Arguments.of(new ConfigurationRequest("name2", "uri2", 20, true), 2)
        );
    }

    private static Stream<Arguments> provideUpdateConfigurationRequests() {
        return Stream.of(
                Arguments.of(new ConfigurationRequest("newName1", "newUri1", 10, true), 1),
                Arguments.of(new ConfigurationRequest("newName2", "newUri2", 20, true), 2)
        );
    }

    @ParameterizedTest
    @MethodSource("provideCreateConfigurationRequests")
    void createConfiguration_ShouldSaveConfiguration(ConfigurationRequest request, Integer configurationId) {
        Configuration configuration = new Configuration();
        configuration.setId(configurationId);

        when(mapper.toConfiguration(request)).thenReturn(configuration);
        when(repository.save(configuration)).thenReturn(configuration);

        Integer id = service.createConfiguration(request);

        assertNotNull(id);
        assertEquals(configurationId, id);
        verify(repository, times(1)).findByActive(true);
        verify(repository, times(1)).save(configuration);
    }

    @Test
    void createConfiguration_ShouldThrowException_WhenMaxConfigurationsReached() {
        when(repository.findByActive(true)).thenReturn(List.of(new Configuration(), new Configuration(), new Configuration(), new Configuration(), new Configuration()));

        ConfigurationRequest request = new ConfigurationRequest("name", "uri", 10, true);

        MaxConfigurationsException exception = assertThrows(MaxConfigurationsException.class, () ->
                service.createConfiguration(request));

        assertEquals(String.format("Maximum number of %d active configurations reached", maxConfigurations), exception.getMsg());
        verify(repository, times(1)).findByActive(true);
        verify(repository, never()).save(any());
    }

    @ParameterizedTest
    @MethodSource("provideUpdateConfigurationRequests")
    void updateConfiguration_ShouldUpdateConfiguration(ConfigurationRequest request, Integer configurationId) {
        Configuration configuration = new Configuration();
        configuration.setId(configurationId);
        configuration.setName("oldName");
        configuration.setUri("oldUri");
        configuration.setInterval(10);

        when(repository.findById(configurationId)).thenReturn(Optional.of(configuration));
        when(repository.save(configuration)).thenReturn(configuration);

        Boolean updated = service.updateConfiguration(configurationId, request);

        assertTrue(updated);
        assertEquals(request.name(), configuration.getName());
        assertEquals(request.uri(), configuration.getUri());
        assertEquals(request.interval(), configuration.getInterval());

        verify(repository, times(1)).findById(configuration.getId());
        verify(repository, times(1)).save(configuration);
    }

    @Test
    void updateConfiguration_ShouldThrowException_WhenConfigurationNotFound() {
        when(repository.findById(1)).thenReturn(Optional.empty());

        ConfigurationRequest request = new ConfigurationRequest("newName", "newUri", 20, true);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
            service.updateConfiguration(1, request));

        assertEquals("No configuration found with the provided ID: 1", exception.getMessage());

        verify(repository, times(1)).findById(1);
        verify(repository, never()).save(any());
    }

    @Test
    void findAllConfigurations_ShouldReturnConfigurationList() {
        Configuration configuration = new Configuration();
        ConfigurationResponse response = new ConfigurationResponse(1, "name", "uri", 10, true);

        when(repository.findByActive(true)).thenReturn(List.of(configuration));
        when(mapper.fromConfiguration(configuration)).thenReturn(response);

        List<ConfigurationResponse> configurations = service.findAllConfigurations(true);

        assertNotNull(configurations);
        assertEquals(1, configurations.size());
        assertEquals(response, configurations.getFirst());
        verify(repository, times(1)).findByActive(true);
    }

    @Test
    void findById_ShouldReturnConfigurationResponse() {
        Configuration configuration = new Configuration();
        ConfigurationResponse response = new ConfigurationResponse(1, "name", "uri", 10, true);

        when(repository.findById(1)).thenReturn(Optional.of(configuration));
        when(mapper.fromConfiguration(configuration)).thenReturn(response);

        ConfigurationResponse found = service.findById(1);

        assertNotNull(found);
        assertEquals(response, found);
        verify(repository, times(1)).findById(1);
    }

    @Test
    void findById_ShouldThrowException_WhenConfigurationNotFound() {
        when(repository.findById(1)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
            service.findById(1));

        assertEquals("No configuration found with the provided ID: 1", exception.getMessage());
        verify(repository, times(1)).findById(1);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void deleteById_ShouldDeleteConfiguration(Integer configurationId) {
        when(repository.existsById(configurationId)).thenReturn(true);

        service.deleteById(configurationId);

        verify(repository, times(1)).deleteById(configurationId);
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 5, 6})
    void deleteById_ShouldThrowException_WhenConfigurationNotFound(Integer configurationId) {
        when(repository.existsById(configurationId)).thenReturn(false);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
            service.deleteById(configurationId));

        assertEquals("No configuration found with the provided ID: " + configurationId, exception.getMessage());
        verify(repository, never()).deleteById(configurationId);
    }
}
