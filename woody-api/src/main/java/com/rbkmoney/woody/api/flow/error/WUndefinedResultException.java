package com.rbkmoney.woody.api.flow.error;

public class WUndefinedResultException extends WRuntimeException {
    private static WErrorDefinition createErrDef(String msg, Throwable cause) {
        WErrorDefinition errorDefinition = new WErrorDefinition(WErrorSource.INTERNAL);
        errorDefinition.setErrorType(WErrorType.UNDEFINED_RESULT);
        errorDefinition.setErrorSource(WErrorSource.INTERNAL);
        errorDefinition.setErrorReason(msg);
        if (cause != null) {
            errorDefinition.setErrorName(cause.getClass().getSimpleName());
            errorDefinition.setErrorMessage(cause.getMessage());
        } else {
            errorDefinition.setErrorName(WUndefinedResultException.class.getSimpleName());
            errorDefinition.setErrorMessage(msg);
        }
        return errorDefinition;
    }

    public WUndefinedResultException() {
        this(WErrorType.UNDEFINED_RESULT.getKey());
    }

    public WUndefinedResultException(String message) {
        super(message, createErrDef(message, null));
    }

    public WUndefinedResultException(String message, Throwable cause) {
        super(message, cause, createErrDef(message, cause));
    }

    public WUndefinedResultException(Throwable cause) {
        super(cause, createErrDef(WErrorType.UNDEFINED_RESULT.getKey(), null));
    }

    public WUndefinedResultException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace, createErrDef(message, cause));
    }

    public WUndefinedResultException(WErrorDefinition errorDefinition) {
        super(errorDefinition);
    }

    public WUndefinedResultException(String message, WErrorDefinition errorDefinition) {
        super(message, errorDefinition);
    }

    public WUndefinedResultException(String message, Throwable cause, WErrorDefinition errorDefinition) {
        super(message, cause, errorDefinition);
    }

    public WUndefinedResultException(Throwable cause, WErrorDefinition errorDefinition) {
        super(cause, errorDefinition);
    }

    public WUndefinedResultException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, WErrorDefinition errorDefinition) {
        super(message, cause, enableSuppression, writableStackTrace, errorDefinition);
    }
}
