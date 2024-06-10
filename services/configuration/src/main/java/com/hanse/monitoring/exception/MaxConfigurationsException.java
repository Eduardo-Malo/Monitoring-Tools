package com.hanse.monitoring.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class MaxConfigurationsException extends RuntimeException {

    private final String msg;
}
