package com.hanse.monitoring.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigurationRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    private static Stream<Arguments> provideInvalidConfigurationRequests() {
        return Stream.of(
                Arguments.of(new ConfigurationRequest(null, "uri", 10, true), "name should be present"),
                Arguments.of(new ConfigurationRequest("", "uri", 10, true), "name should be present"),
                Arguments.of(new ConfigurationRequest(" ", "uri", 10, true), "name should be present"),
                Arguments.of(new ConfigurationRequest("name", null, 10, true), "uri should be present"),
                Arguments.of(new ConfigurationRequest("name", "", 10, true), "uri should be present"),
                Arguments.of(new ConfigurationRequest("name", " ", 10, true), "uri should be present"),
                Arguments.of(new ConfigurationRequest("name", "uri", -1, true), "must be greater than or equal to 0"),
                Arguments.of(new ConfigurationRequest("name", "uri", 86401, true), "must be less than or equal to 86400")
        );
    }

    private static Stream<Arguments> provideValidConfigurationRequests() {
        return Stream.of(
                Arguments.of(new ConfigurationRequest("name1", "uri1", 0, true)),
                Arguments.of(new ConfigurationRequest("name2", "uri2", 86400, true)),
                Arguments.of(new ConfigurationRequest("name3", "uri3", 10, true))
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidConfigurationRequests")
    void whenInvalidConfigurationRequest_thenValidationFails(ConfigurationRequest request, String expectedErrorMessage) {
        Set<ConstraintViolation<ConfigurationRequest>> violations = validator.validate(request, OnCreate.class);
        assertFalse(violations.isEmpty());

        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals(expectedErrorMessage)));
    }

    @ParameterizedTest
    @MethodSource("provideValidConfigurationRequests")
    void whenValidConfigurationRequest_thenValidationSucceeds(ConfigurationRequest request) {
        Set<ConstraintViolation<ConfigurationRequest>> violations = validator.validate(request, OnCreate.class);
        assertTrue(violations.isEmpty());
    }
}
