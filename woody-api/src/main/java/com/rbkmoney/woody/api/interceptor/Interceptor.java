package com.rbkmoney.woody.api.interceptor;

import com.rbkmoney.woody.api.trace.ContextSpan;
import com.rbkmoney.woody.api.trace.ContextUtils;
import com.rbkmoney.woody.api.trace.TraceData;

public interface Interceptor {
    /**
     * @return true - if flow is successfully intercepted and ready for further processing; false - if interception failed and processing must be switched to err handling
     */
    default boolean intercept(TraceData traceData, Object providerContext, Object... contextParams) {
        throw new UnsupportedOperationException("not implemented");
    }

    default boolean interceptError(TraceData traceData, Throwable t, boolean isClient) {
        return interceptError(traceData.getSpan(isClient), t);
    }

    default boolean interceptError(ContextSpan contextSpan, Throwable t) {
        ContextUtils.setInterceptionError(contextSpan, t);
        return false;
    }
}
