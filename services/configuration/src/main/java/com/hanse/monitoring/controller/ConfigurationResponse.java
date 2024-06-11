package com.hanse.monitoring.controller;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ConfigurationResponse(
        Integer id,
        String name,
        String uri,
        Integer interval,
        Boolean active
) {
}
