package com.rbkmoney.woody.api.interceptor;

import com.rbkmoney.woody.api.trace.TraceData;

/**
 * Created by vpankrashkin on 04.05.16.
 */
public class ContainerCommonInterceptor implements CommonInterceptor {
    private RequestInterceptor requestInterceptor;
    private ResponseInterceptor responseInterceptor;

    public ContainerCommonInterceptor(RequestInterceptor requestInterceptor, ResponseInterceptor responseInterceptor) {
        this.requestInterceptor = requestInterceptor != null ? requestInterceptor : new EmptyCommonInterceptor();
        this.responseInterceptor = responseInterceptor != null ? responseInterceptor : new EmptyCommonInterceptor();
    }

    @Override
    public boolean interceptRequest(TraceData traceData, Object providerContext, Object... contextParams) {
        return requestInterceptor.interceptRequest(traceData, providerContext, contextParams);
    }

    @Override
    public boolean interceptResponse(TraceData traceData, Object providerContext, Object... contextParams) {
        return responseInterceptor.interceptResponse(traceData, providerContext, contextParams);
    }

}
