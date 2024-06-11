package com.hanse.monitoring.integration;

import com.hanse.monitoring.controller.ConfigurationRequest;
import com.hanse.monitoring.controller.ConfigurationResponse;
import com.hanse.monitoring.domain.Configuration;
import com.hanse.monitoring.handler.ErrorResponse;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ConfigurationTestIT {

    @Container
    @ServiceConnection
    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:alpine");

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${application.max-configurations}")
    private Integer maxConfigurations;

    ConfigurationRequest request;
    Integer configurationId;

    @BeforeEach
    public void setUp() {
        restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        request = new ConfigurationRequest("name", "uri", 1, true);
        ResponseEntity<Integer> response = restTemplate.postForEntity(getRootUrl(), new ConfigurationRequest("name1", "uri1", 1, true), Integer.class);
        configurationId = response.getBody();
        restTemplate.postForEntity(getRootUrl(), new ConfigurationRequest("name2", "uri2", 1, true), Integer.class);
        restTemplate.postForEntity(getRootUrl(), new ConfigurationRequest("name3", "uri3", 1, true), Integer.class);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM configuration");
    }

    private String getRootUrl() {
        return "http://localhost:" + port + "/api/v1/configurations";
    }

    private @NotNull Configuration testDatabase(Integer configurationId, String name, String uri, Integer interval) {
        RowMapper<Configuration> rowMapper = (rs, _) -> {
            Configuration config = new Configuration();
            config.setId(rs.getObject("id") != null ? (Integer) rs.getObject("id") : null);
            config.setName(rs.getString("name") != null ? rs.getString("name") : null);
            config.setUri(rs.getString("uri") != null ? rs.getString("uri") : null);
            config.setInterval(rs.getObject("interval") != null ? (Integer) rs.getObject("interval") : null);
            if (rs.getTimestamp("created_at") != null) {
                config.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            }
            if (rs.getTimestamp("modified_at") != null) {
                config.setModifiedAt(rs.getTimestamp("modified_at").toLocalDateTime());
            }
            return config;
        };

        Configuration result = jdbcTemplate.queryForObject(
                "SELECT id, name, uri, interval, created_at, modified_at FROM configuration WHERE id = ?",
                rowMapper,
                configurationId
        );

        assert result != null;
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getUri()).isEqualTo(uri);
        assertThat(result.getInterval()).isEqualTo(interval);
        assertThat(result.getId()).isEqualTo(configurationId);
        assertThat(result.getCreatedAt()).isNotNull();
        return result;
    }

    private Integer countConfigurations() {
        String sql = "SELECT COUNT(*) FROM configuration";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    @Test
    void testCreateConfiguration() {
        ResponseEntity<Integer> response = restTemplate.postForEntity(getRootUrl(), request, Integer.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        Configuration result = testDatabase(response.getBody(), request.name(), request.uri(), request.interval());
        assertThat(result.getModifiedAt()).isNull();
    }

    @Test
    void testCreateConfigurationFailValidation() {
        ConfigurationRequest badRequest = new ConfigurationRequest("", "",-1, true);
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(getRootUrl(), badRequest, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertAll(
                () -> assertTrue(Objects.requireNonNull(response.getBody()).errors().containsKey("name")),
                () -> assertTrue(Objects.requireNonNull(response.getBody()).errors().containsKey("uri")),
                () -> assertTrue(Objects.requireNonNull(response.getBody()).errors().containsKey("interval")),
                () -> assertEquals("name should be present", Objects.requireNonNull(response.getBody()).errors().get("name")),
                () -> assertEquals("uri should be present", Objects.requireNonNull(response.getBody()).errors().get("uri")),
                () -> assertEquals("must be greater than or equal to 0", Objects.requireNonNull(response.getBody()).errors().get("interval"))
        );
    }

    @Test
    void testCreateConfigurationFailMaxNumberConfigurations() {
        for (int i = 0; i < maxConfigurations-3; i++) {
            restTemplate.postForEntity(getRootUrl(), new ConfigurationRequest("name"+i+100, "uri"+i+100, 1, true), Integer.class);
        }

        ResponseEntity<String> response = restTemplate.postForEntity(getRootUrl(), request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEqualTo(String.format("Maximum number of %d active configurations reached", maxConfigurations));
    }

    @Test
    void testUpdateConfiguration() {
        ResponseEntity<Boolean> response = restTemplate.exchange(getRootUrl() + "/" + configurationId,
                HttpMethod.PATCH, new HttpEntity<>(request), Boolean.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isTrue();

        Configuration result = testDatabase(configurationId, request.name(), request.uri(), request.interval());
        assertThat(result.getModifiedAt()).isNotNull();
    }

    @Test
    void testFindAllConfigurations() {
        ResponseEntity<ConfigurationResponse[]> response = restTemplate.getForEntity(getRootUrl(), ConfigurationResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(Objects.requireNonNull(response.getBody())).hasSizeGreaterThan(2);

        Arrays.stream(response.getBody()).forEach(configuration -> {
            assertThat(configuration.id()).isNotNull();
            assertThat(configuration.name()).isNotEmpty();
            assertThat(configuration.uri()).isNotEmpty();
            assertThat(configuration.interval()).isNotNull();
        });

        int count = countConfigurations();
        assertThat(count).isEqualTo(3);
    }

    @Test
    void testFindConfigurationById() {
        ResponseEntity<ConfigurationResponse> response = restTemplate.getForEntity(getRootUrl() + "/" + configurationId, ConfigurationResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        Configuration result = testDatabase(configurationId, "name1", "uri1", 1);
        assertThat(result.getModifiedAt()).isNull();
    }

    @Test
    void testDeleteConfiguration() {
        ResponseEntity<Void> response = restTemplate.exchange(getRootUrl() + "/" + configurationId, HttpMethod.DELETE, null, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    }

    @Test
    void testDeleteConfigurationNotFound() {
        ResponseEntity<Void> response = restTemplate.exchange(getRootUrl() + "/" + configurationId, HttpMethod.DELETE, null, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        ResponseEntity<Map<String, String>> response2 = restTemplate.exchange(getRootUrl() + "/" + configurationId, HttpMethod.DELETE, null, new ParameterizedTypeReference<>() { });
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        assertAll(
                () -> assertTrue(Objects.requireNonNull(response2.getBody()).containsKey("message")),
                () -> assertTrue(Objects.requireNonNull(response2.getBody()).containsKey("timestamp")),
                () -> assertEquals(String.format("No configuration found with the provided ID: %s", configurationId), Objects.requireNonNull(response2.getBody()).get("message"))
        );
    }
}
