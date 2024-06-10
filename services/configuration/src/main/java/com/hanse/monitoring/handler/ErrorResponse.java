package com.hanse.monitoring.handler;

import java.util.Map;

public record ErrorResponse(
        Map<String, String> errors
) {

}
