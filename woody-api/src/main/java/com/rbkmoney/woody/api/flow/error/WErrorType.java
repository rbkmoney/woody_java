package com.rbkmoney.woody.api.flow.error;

/**
 * Created by vpankrashkin on 12.12.16.
 */
public enum WErrorType {
    UNAVAILABLE_RESULT("Resource Unavailable"),
    UNDEFINED_RESULT("Result Unknown"),
    UNEXPECTED_ERROR("Result Unexpected"),
    PROVIDER_ERROR("Provider Result Unexpected"),
    BUSINESS_ERROR("Business Error");

    private final String key;

    WErrorType(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public static WErrorType getValueByKey(String key) {
        if (key == null) {
            return null;
        }
        for (WErrorType errorType: WErrorType.values()) {
            if (errorType.getKey().equalsIgnoreCase(key)) {
                return errorType;
            }
        }
        return null;
    }
}
