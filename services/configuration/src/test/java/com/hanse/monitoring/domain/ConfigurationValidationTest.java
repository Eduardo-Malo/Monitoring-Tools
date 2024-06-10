package com.hanse.monitoring.domain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ConfigurationValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    private static Stream<Arguments> provideInvalidConfigurations() {
        return Stream.of(
                Arguments.of(new Configuration(null, "", "uri", 10, LocalDateTime.now(), null), "name should be present"),
                Arguments.of(new Configuration(null, "name", "", 10, LocalDateTime.now(), null), "uri should be present"),
                Arguments.of(new Configuration(null, "name", "uri", null, LocalDateTime.now(), null), "interval should be present"),
                Arguments.of(new Configuration(null, "name", "uri", -1, LocalDateTime.now(), null), "must be greater than or equal to 0"),
                Arguments.of(new Configuration(null, "name", "uri", 86401, LocalDateTime.now(), null), "must be less than or equal to 86400"),
                Arguments.of(new Configuration(null, "name", "uri", 10, null, null), "createdAt should be present"),
                Arguments.of(new Configuration(null, "name", "uri", 10, LocalDateTime.now().plusDays(1), null), "must be a date in the past or in the present")
        );
    }

    private static Stream<Arguments> provideValidConfigurations() {
        return Stream.of(
                Arguments.of(new Configuration(null, "name1", "uri1", 0, LocalDateTime.now(), null)),
                Arguments.of(new Configuration(null, "name2", "uri2", 86400, LocalDateTime.now(), null)),
                Arguments.of(new Configuration(null, "name3", "uri3", 10, LocalDateTime.now(), null))
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidConfigurations")
    void whenInvalidConfiguration_thenValidationFails(Configuration configuration, String expectedErrorMessage) {
        Set<ConstraintViolation<Configuration>> violations = validator.validate(configuration);
        assertFalse(violations.isEmpty());

        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals(expectedErrorMessage)));
    }

    @ParameterizedTest
    @MethodSource("provideValidConfigurations")
    void whenValidConfiguration_thenValidationSucceeds(Configuration configuration) {
        Set<ConstraintViolation<Configuration>> violations = validator.validate(configuration);
        assertTrue(violations.isEmpty());
    }
}
