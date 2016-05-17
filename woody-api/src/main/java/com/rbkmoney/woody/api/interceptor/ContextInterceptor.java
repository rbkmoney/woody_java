package com.rbkmoney.woody.api.interceptor;

import com.rbkmoney.woody.api.trace.ContextUtils;
import com.rbkmoney.woody.api.trace.TraceData;
import com.rbkmoney.woody.api.trace.context.TraceContext;

/**
 * Created by vpankrashkin on 10.05.16.
 */
public class ContextInterceptor<ReqProvider, RespProvider> implements CommonInterceptor<ReqProvider, RespProvider> {
    private final TraceContext traceContext;
    private final CommonInterceptor interceptor;

    public ContextInterceptor(TraceContext traceContext, CommonInterceptor interceptor) {
        this.traceContext = traceContext;
        this.interceptor = interceptor;
    }

    @Override
    public boolean interceptRequest(TraceData traceData, ReqProvider providerContext, Object... contextParams) {
        traceContext.init();
        return interceptor.interceptRequest(traceData, providerContext, contextParams);
    }

    @Override
    public boolean interceptResponse(TraceData traceData, RespProvider providerContext, Object... contextParams) {
        try {
            interceptor.interceptResponse(traceData, providerContext, contextParams);
        } finally {
            traceContext.destroy(ContextUtils.hasCallErrors(traceData.getActiveSpan()));
        }
        return false;
    }
}
