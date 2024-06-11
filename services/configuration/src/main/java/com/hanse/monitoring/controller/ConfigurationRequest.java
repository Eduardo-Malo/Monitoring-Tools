package com.hanse.monitoring.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ConfigurationRequest(
        @NotNull(message = "name should be present", groups = OnCreate.class)
        @NotEmpty(message = "name should be present", groups = OnCreate.class)
        @NotBlank(message = "name should be present", groups = OnCreate.class)
        String name,
        @NotNull(message = "uri should be present", groups = OnCreate.class)
        @NotEmpty(message = "uri should be present", groups = OnCreate.class)
        @NotBlank(message = "uri should be present", groups = OnCreate.class)
        String uri,
        @NotNull(message = "interval should be present", groups = OnCreate.class)
        @Min(groups = {OnCreate.class, OnUpdate.class}, value = 0)
        @Max(groups = {OnCreate.class, OnUpdate.class}, value = 86400)
        Integer interval,
        Boolean active
) {
}
