package com.rbkmoney.woody.api.interceptor;

import com.rbkmoney.woody.api.trace.TraceData;

/**
 * Created by vpankrashkin on 05.05.16.
 */
public class EmptyCommonInterceptor implements CommonInterceptor {

    @Override
    public boolean interceptRequest(TraceData traceData, Object providerContext, Object... contextParams) {
        return true;
    }

    @Override
    public boolean interceptResponse(TraceData traceData, Object providerContext, Object... contextParams) {
        return true;
    }
}
