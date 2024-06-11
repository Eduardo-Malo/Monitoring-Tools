package com.hanse.analytics.handler;

import java.util.Map;

public record ErrorResponse(
        Map<String, String> errors
) {

}
