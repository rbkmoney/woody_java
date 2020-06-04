package com.rbkmoney.woody.api.flow.error;

import java.util.Objects;

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

    public WErrorDefinition() {
        this(WErrorSource.INTERNAL);
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WErrorDefinition)) return false;
        WErrorDefinition that = (WErrorDefinition) o;
        return generationSource == that.generationSource &&
                errorType == that.errorType &&
                errorSource == that.errorSource &&
                Objects.equals(errorReason, that.errorReason) &&
                Objects.equals(errorName, that.errorName) &&
                Objects.equals(errorMessage, that.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(generationSource, errorType, errorSource, errorReason, errorName, errorMessage);
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
