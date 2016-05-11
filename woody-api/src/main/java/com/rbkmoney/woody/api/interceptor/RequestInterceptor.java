package com.rbkmoney.woody.api.interceptor;

import com.rbkmoney.woody.api.trace.TraceData;

/**
 * Created by vpankrashkin on 22.04.16.
 */
public interface RequestInterceptor<Provider> {
    /**
     * @return true - if request is successfully intercepted and ready for further processing; false - if interception failed and processing must be switched to request err handling
     */
    boolean interceptRequest(TraceData traceData, Provider providerContext, Object... contextParams);
}
