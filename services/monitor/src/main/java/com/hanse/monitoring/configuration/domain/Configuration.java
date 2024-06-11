package com.hanse.monitoring.configuration.domain;

public record Configuration(
        Integer id,
        String name,
        String uri,
        Integer interval,
        Boolean active
) {
}
