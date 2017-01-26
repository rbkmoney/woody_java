package com.rbkmoney.woody.api.trace.context.metadata;

/**
 * Created by vpankrashkin on 20.01.17.
 */
public class MetadataConversionException extends RuntimeException {
    public MetadataConversionException() {
    }

    public MetadataConversionException(String message) {
        super(message);
    }

    public MetadataConversionException(String message, Throwable cause) {
        super(message, cause);
    }

    public MetadataConversionException(Throwable cause) {
        super(cause);
    }

    public MetadataConversionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
