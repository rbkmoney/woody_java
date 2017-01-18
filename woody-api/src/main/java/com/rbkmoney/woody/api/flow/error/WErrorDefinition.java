package com.rbkmoney.woody.api.flow.error;

/**
 * Created by vpankrashkin on 12.12.16.
 */
public class WErrorDefinition {
    private final WErrorSource generationSource;
    private WErrorType errorType;
    private WErrorSource errorSource;
    private String errorReason;
    private String errorName;
    private String errorMessage;

    public WErrorDefinition(WErrorSource generationSource) {
        this.generationSource = generationSource;
    }

    public WErrorSource getGenerationSource() {
        return generationSource;
    }

    public WErrorType getErrorType() {
        return errorType;
    }

    public WErrorSource getErrorSource() {
        return errorSource;
    }

    public String getErrorReason() {
        return errorReason;
    }

    public String getErrorName() {
        return errorName;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorType(WErrorType errorType) {
        this.errorType = errorType;
    }

    public void setErrorSource(WErrorSource errorSource) {
        this.errorSource = errorSource;
    }

    public void setErrorReason(String errorReason) {
        this.errorReason = errorReason;
    }

    public void setErrorName(String errorName) {
        this.errorName = errorName;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "WErrorDefinition{" +
                "generationSource=" + generationSource +
                ", errorType=" + errorType +
                ", errorSource=" + errorSource +
                ", errorReason='" + errorReason + '\'' +
                ", errorName='" + errorName + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
