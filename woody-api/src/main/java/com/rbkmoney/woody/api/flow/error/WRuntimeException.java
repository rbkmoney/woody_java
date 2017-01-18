package com.rbkmoney.woody.api.flow.error;

import java.util.Objects;

/**
 * Created by vpankrashkin on 26.12.16.
 */
public class WRuntimeException extends RuntimeException {
    private final WErrorDefinition errorDefinition;

    public WRuntimeException(WErrorDefinition errorDefinition) {
        Objects.requireNonNull(errorDefinition);
        this.errorDefinition = errorDefinition;
    }

    public WRuntimeException(String message, WErrorDefinition errorDefinition) {
        super(message);
        Objects.requireNonNull(errorDefinition);
        this.errorDefinition = errorDefinition;
    }

    public WRuntimeException(String message, Throwable cause, WErrorDefinition errorDefinition) {
        super(message, cause);
        Objects.requireNonNull(errorDefinition);
        this.errorDefinition = errorDefinition;
    }

    public WRuntimeException(Throwable cause, WErrorDefinition errorDefinition) {
        super(cause);
        Objects.requireNonNull(errorDefinition);
        this.errorDefinition = errorDefinition;
    }

    public WRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, WErrorDefinition errorDefinition) {
        super(message, cause, enableSuppression, writableStackTrace);
        Objects.requireNonNull(errorDefinition);
        this.errorDefinition = errorDefinition;
    }

    public WErrorDefinition getErrorDefinition() {
        return errorDefinition;
    }
}
