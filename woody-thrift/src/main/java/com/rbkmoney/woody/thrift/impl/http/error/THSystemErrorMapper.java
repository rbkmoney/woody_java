package com.rbkmoney.woody.thrift.impl.http.error;

import com.rbkmoney.woody.api.flow.error.*;
import com.rbkmoney.woody.api.trace.ContextSpan;
import com.rbkmoney.woody.api.trace.ContextUtils;

public class THSystemErrorMapper implements WErrorMapper {

    @Override
    public WErrorDefinition mapToDef(Throwable t, ContextSpan contextSpan) {
        if (t instanceof WRuntimeException) {
            return ((WRuntimeException) t).getErrorDefinition();
        } else {
            WErrorDefinition errorDefinition = ContextUtils.getErrorDefinition(contextSpan);
            if (errorDefinition != null) {
                WErrorType errorType = errorDefinition.getErrorType();
                if (errorType == WErrorType.UNEXPECTED_ERROR ||
                        errorType == WErrorType.UNAVAILABLE_RESULT || errorType == WErrorType.UNDEFINED_RESULT) {
                    return errorDefinition;
                }
            }
            return null;
        }
    }

    @Override
    public Exception mapToError(WErrorDefinition eDefinition, ContextSpan contextSpan) {
        WErrorType errorType = eDefinition.getErrorType();
        if (errorType != null) {
            switch (errorType) {
                case UNAVAILABLE_RESULT:
                    return new WUnavailableResultException(eDefinition);
                case UNDEFINED_RESULT:
                    return new WUndefinedResultException(eDefinition);
                case UNEXPECTED_ERROR:
                    return new WRuntimeException(eDefinition);
                default:
                    return null;
            }
        }
        return null;
    }
}
