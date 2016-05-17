package com.rbkmoney.woody.api.interceptor;

import com.rbkmoney.woody.api.trace.TraceData;

/**
 * Created by vpankrashkin on 05.05.16.
 */
public class EmptyCommonInterceptor<ReqProvider, RespProvider> implements CommonInterceptor<ReqProvider, RespProvider> {

    @Override
    public boolean interceptRequest(TraceData traceData, ReqProvider providerContext, Object... contextParams) {
        return true;
    }

    @Override
    public boolean interceptResponse(TraceData traceData, RespProvider providerContext, Object... contextParams) {
        return true;
    }
}
