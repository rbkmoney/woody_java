package com.rbkmoney.woody.thrift.impl.http.error;

import com.rbkmoney.woody.api.flow.error.*;
import com.rbkmoney.woody.api.trace.ContextSpan;

/**
 * Created by vpankrashkin on 26.12.16.
 */
public class THDefaultErrorMapper implements WErrorMapper {
    @Override
    public WErrorDefinition mapToDef(Throwable t, ContextSpan contextSpan) {

            WErrorDefinition errorDefinition = new WErrorDefinition(WErrorSource.INTERNAL);
            errorDefinition.setErrorType(WErrorType.UNEXPECTED_ERROR);
            errorDefinition.setErrorSource(WErrorSource.INTERNAL);
            errorDefinition.setErrorReason(t.getClass().getSimpleName() + ":" + t.getMessage());
            errorDefinition.setErrorName(t.getClass().getSimpleName());
            errorDefinition.setErrorMessage(t.getMessage());
            return errorDefinition;
    }

    @Override
    public Exception mapToError(WErrorDefinition eDefinition, ContextSpan contextSpan) {
        if (eDefinition.getErrorType() != WErrorType.BUSINESS_ERROR) {
            return new WRuntimeException(eDefinition);
        }
        return null;
    }
}
