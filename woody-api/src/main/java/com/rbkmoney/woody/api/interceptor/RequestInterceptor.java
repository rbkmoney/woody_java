package com.rbkmoney.woody.api.interceptor;

import com.rbkmoney.woody.api.trace.TraceData;

public interface RequestInterceptor extends Interceptor {
    /**
     * @return true - if request is successfully intercepted and ready for further processing; false - if interception failed and processing must be switched to request err handling
     */
    default boolean interceptRequest(TraceData traceData, Object providerContext, Object... contextParams) {
        return intercept(traceData, providerContext, contextParams);
    }

}
