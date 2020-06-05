package com.rbkmoney.woody.api.interceptor;

import com.rbkmoney.woody.api.trace.TraceData;

public class CompositeInterceptor implements CommonInterceptor {
    private final CommonInterceptor[] interceptors;
    private final boolean breakOnError;

    public CompositeInterceptor(boolean breakOnError, CommonInterceptor... interceptors) {
        this.breakOnError = breakOnError;
        this.interceptors = interceptors.clone();
    }

    public CompositeInterceptor(CommonInterceptor... interceptors) {
        this(true, interceptors);
    }

    @Override
    public boolean interceptRequest(TraceData traceData, Object providerContext, Object... contextParams) {
        boolean successful = true;
        for (int i = 0; i < interceptors.length; ++i) {
            successful &= interceptors[i].interceptRequest(traceData, providerContext, contextParams);
            if (!successful && breakOnError) {
                return false;
            }
        }
        return successful;
    }

    @Override
    public boolean interceptResponse(TraceData traceData, Object providerContext, Object... contextParams) {
        boolean successful = true;
        for (int i = 0; i < interceptors.length; ++i) {
            successful &= interceptors[i].interceptResponse(traceData, providerContext, contextParams);
            if (!successful && breakOnError) {
                return false;
            }
        }
        return true;
    }


}
