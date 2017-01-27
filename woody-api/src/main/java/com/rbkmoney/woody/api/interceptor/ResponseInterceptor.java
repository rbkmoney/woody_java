package com.rbkmoney.woody.api.interceptor;

import com.rbkmoney.woody.api.trace.TraceData;

/**
 * Created by vpankrashkin on 22.04.16.
 */
public interface ResponseInterceptor extends Interceptor {
    /**
     * @return true - if response is successfully intercepted and ready for further processing; false - if interception failed and processing must be switched to response err handling
     */
    default boolean interceptResponse(TraceData traceData, Object providerContext, Object... contextParams) {
        return intercept(traceData, providerContext, contextParams);
    }

}
