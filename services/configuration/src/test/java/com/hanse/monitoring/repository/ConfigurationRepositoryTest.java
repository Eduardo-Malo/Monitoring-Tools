package com.hanse.monitoring.repository;

import com.hanse.monitoring.domain.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ConfigurationRepositoryTest {

    @Container
    @ServiceConnection
    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:alpine");

    @Autowired
    private ConfigurationRepository repository;

    private Configuration configuration;

    @BeforeEach
    void setUp() {
        configuration = Configuration.builder()
                                     .name("Test Config")
                                     .uri("http://test.com")
                                     .interval(10)
                                     .createdAt(LocalDateTime.now())
                                     .modifiedAt(LocalDateTime.now())
                                     .build();
    }

    @Test
    void testSaveConfiguration() {
        Configuration savedConfig = repository.save(configuration);
        assertNotNull(savedConfig.getId());
        assertEquals("Test Config", savedConfig.getName());
    }

    @Test
    void testFindConfigurationById() {
        Configuration savedConfig = repository.save(configuration);
        Optional<Configuration> foundConfig = repository.findById(savedConfig.getId());
        assertTrue(foundConfig.isPresent());
        assertEquals("Test Config", foundConfig.get().getName());
    }

    @Test
    void testUpdateConfiguration() {
        Configuration savedConfig = repository.save(configuration);
        savedConfig.setName("Updated Config");
        Configuration updatedConfig = repository.save(savedConfig);

        Optional<Configuration> foundConfig = repository.findById(updatedConfig.getId());
        assertTrue(foundConfig.isPresent());
        assertEquals("Updated Config", foundConfig.get().getName());
    }

    @Test
    void testDeleteConfiguration() {
        Configuration savedConfig = repository.save(configuration);
        repository.deleteById(savedConfig.getId());
        Optional<Configuration> foundConfig = repository.findById(savedConfig.getId());
        assertFalse(foundConfig.isPresent());
    }

    @Test
    void testFindAllConfigurations() {
        repository.save(configuration);
        repository.save(Configuration.builder()
                                     .name("Another Config")
                                     .uri("http://another.com")
                                     .interval(20)
                                     .createdAt(LocalDateTime.now())
                                     .modifiedAt(LocalDateTime.now())
                                     .build());

        Iterable<Configuration> configurations = repository.findAll();
        assertEquals(2, ((Collection<?>) configurations).size());
    }
}
