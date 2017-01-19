package com.rbkmoney.woody.api.flow.error;

/**
 * Created by vpankrashkin on 26.12.16.
 */
public class WUnavailableResultException extends WRuntimeException {

    private static WErrorDefinition createErrDef(String reason, Throwable cause) {
        WErrorDefinition errorDefinition = new WErrorDefinition(WErrorSource.INTERNAL);
        errorDefinition.setErrorType(WErrorType.UNAVAILABLE_RESULT);
        errorDefinition.setErrorSource(WErrorSource.INTERNAL);
        errorDefinition.setErrorReason(reason);
        if (cause != null) {
            errorDefinition.setErrorName(cause.getClass().getSimpleName());
            errorDefinition.setErrorMessage(cause.getMessage());
        } else {
            errorDefinition.setErrorName(WUnavailableResultException.class.getSimpleName());
            errorDefinition.setErrorMessage(reason);
        }
        return errorDefinition;
    }

    public WUnavailableResultException() {
        this(WErrorType.UNAVAILABLE_RESULT.getKey());
    }

    public WUnavailableResultException(String message) {
        super(message, createErrDef(message, null));
    }

    public WUnavailableResultException(String message, Throwable cause) {
        super(message, cause, createErrDef(message, null));
    }

    public WUnavailableResultException(Throwable cause) {
        super(cause, createErrDef(WErrorType.UNAVAILABLE_RESULT.getKey(), null));
    }

    public WUnavailableResultException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace, createErrDef(message, cause));
    }

    public WUnavailableResultException(WErrorDefinition errorDefinition) {
        super(errorDefinition);
    }

    public WUnavailableResultException(String message, WErrorDefinition errorDefinition) {
        super(message, errorDefinition);
    }

    public WUnavailableResultException(String message, Throwable cause, WErrorDefinition errorDefinition) {
        super(message, cause, errorDefinition);
    }

    public WUnavailableResultException(Throwable cause, WErrorDefinition errorDefinition) {
        super(cause, errorDefinition);
    }

    public WUnavailableResultException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, WErrorDefinition errorDefinition) {
        super(message, cause, enableSuppression, writableStackTrace, errorDefinition);
    }
}


