package com.rbkmoney.woody.api.interceptor.ext;

import com.rbkmoney.woody.api.trace.TraceData;

/**
 * Created by vpankrashkin on 13.12.16.
 */
public class ExtensionContext {
    private final TraceData traceData;
    private final Object providerContext;
    private final Object[] contextParameters;

    public ExtensionContext(TraceData traceData, Object providerContext, Object[] contextParameters) {
        this.traceData = traceData;
        this.providerContext = providerContext;
        this.contextParameters = contextParameters;
    }

    public TraceData getTraceData() {
        return traceData;
    }

    public Object getProviderContext() {
        return providerContext;
    }

    public Object[] getContextParameters() {
        return contextParameters;
    }

}
