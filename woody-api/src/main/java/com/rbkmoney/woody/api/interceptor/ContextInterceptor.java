package com.rbkmoney.woody.api.interceptor;

import com.rbkmoney.woody.api.trace.ContextUtils;
import com.rbkmoney.woody.api.trace.TraceData;
import com.rbkmoney.woody.api.trace.context.TraceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class ContextInterceptor implements CommonInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(ContextInterceptor.class);

    private final TraceContext traceContext;
    private final CommonInterceptor interceptor;

    public ContextInterceptor(TraceContext traceContext, CommonInterceptor interceptor) {
        this.traceContext = Objects.requireNonNull(traceContext, "TraceContext can't be null");
        this.interceptor = interceptor != null ? interceptor : new EmptyCommonInterceptor();
    }

    @Override
    public boolean interceptRequest(TraceData traceData, Object providerContext, Object... contextParams) {
        LOG.trace("Intercept request context");
        if (!TraceContext.getCurrentTraceData().getServiceSpan().isFilled()) {
            throw new IllegalStateException("TraceContext service span must be filled");
        }
        traceContext.init();
        return interceptor.interceptRequest(traceData, providerContext, contextParams);
    }

    @Override
    public boolean interceptResponse(TraceData traceData, Object providerContext, Object... contextParams) {
        LOG.trace("Intercept response context");
        try {
            return interceptor.interceptResponse(traceData, providerContext, contextParams);
        } finally {
            traceContext.destroy(ContextUtils.hasCallErrors(traceData.getActiveSpan()));
        }
    }
}
