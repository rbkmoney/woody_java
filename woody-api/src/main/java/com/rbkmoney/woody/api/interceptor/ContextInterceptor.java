package com.rbkmoney.woody.api.interceptor;

import com.rbkmoney.woody.api.trace.ContextUtils;
import com.rbkmoney.woody.api.trace.TraceData;
import com.rbkmoney.woody.api.trace.context.TraceContext;

/**
 * Created by vpankrashkin on 10.05.16.
 */
public class ContextInterceptor implements CommonInterceptor {
    private final TraceContext traceContext;
    private final CommonInterceptor interceptor;

    public ContextInterceptor(TraceContext traceContext, CommonInterceptor interceptor) {
        this.traceContext = traceContext;
        this.interceptor = interceptor != null ? interceptor : new EmptyCommonInterceptor();
    }

    @Override
    public boolean interceptRequest(TraceData traceData, Object providerContext, Object... contextParams) {
        traceContext.init();
        return interceptor.interceptRequest(traceData, providerContext, contextParams);
    }

    @Override
    public boolean interceptResponse(TraceData traceData, Object providerContext, Object... contextParams) {
        try {
            interceptor.interceptResponse(traceData, providerContext, contextParams);
        } finally {
            traceContext.destroy(ContextUtils.hasCallErrors(traceData.getActiveSpan()));
        }
        return false;
    }
}
